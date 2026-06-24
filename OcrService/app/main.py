import logging
import os
import tempfile
from functools import lru_cache
from pathlib import Path
from typing import Any, Dict, List, Tuple

from fastapi import FastAPI, File, Form, HTTPException, UploadFile
from PIL import Image
from pororo import Pororo
from pydantic import BaseModel


logging.basicConfig(level=os.getenv("LOG_LEVEL", "INFO"))
logger = logging.getLogger("pororo-ocr")

DEFAULT_LANG = os.getenv("PORORO_OCR_LANG", "ko")

app = FastAPI(title="Pororo OCR Service")


def apply_pillow_compatibility_patch() -> None:
    # Pororo still references deprecated Pillow constants such as Image.ANTIALIAS.
    if not hasattr(Image, "ANTIALIAS"):
        Image.ANTIALIAS = Image.Resampling.LANCZOS


apply_pillow_compatibility_patch()


class HealthResponse(BaseModel):
    status: str


class OcrResponse(BaseModel):
    detected: bool
    text: str
    lines: List[str]
    blocks: List[Dict[str, Any]]
    lang: str
    engine: str


@lru_cache(maxsize=4)
def get_ocr_model(lang: str):
    logger.info("Loading Pororo OCR model for lang=%s", lang)
    return Pororo(task="ocr", lang=lang)


@app.get("/health", response_model=HealthResponse)
def health() -> HealthResponse:
    return HealthResponse(status="ok")


@app.post("/ocr", response_model=OcrResponse)
async def ocr(
        image: UploadFile = File(...),
        lang: str = Form(DEFAULT_LANG),
) -> OcrResponse:
    image_bytes = await image.read()
    if not image_bytes:
        raise HTTPException(status_code=400, detail="Empty image")

    suffix = resolve_suffix(image.filename, image.content_type)
    temp_path = write_temp_file(image_bytes, suffix)

    try:
        result = get_ocr_model(lang)(temp_path, detail=True)
    except Exception as exception:  # pragma: no cover - external model failure path
        logger.exception("Pororo OCR failed")
        raise HTTPException(status_code=502, detail="Pororo OCR failed") from exception
    finally:
        Path(temp_path).unlink(missing_ok=True)

    lines, blocks = normalize_result(result)
    text = "\n".join(lines).strip()

    return OcrResponse(
        detected=bool(text),
        text=text,
        lines=lines,
        blocks=blocks,
        lang=lang,
        engine="pororo",
    )


def write_temp_file(image_bytes: bytes, suffix: str) -> str:
    with tempfile.NamedTemporaryFile(delete=False, suffix=suffix) as temp_file:
        temp_file.write(image_bytes)
        return temp_file.name


def resolve_suffix(filename: str, content_type: str) -> str:
    suffix = Path(filename or "").suffix
    if suffix:
        return suffix

    mapping = {
        "image/png": ".png",
        "image/gif": ".gif",
        "image/webp": ".webp",
    }
    return mapping.get((content_type or "").lower(), ".jpg")


def normalize_result(result: Any) -> Tuple[List[str], List[Dict[str, Any]]]:
    if isinstance(result, dict):
        descriptions = result.get("description") or []
        bounding_polys = result.get("bounding_poly") or []
        lines = [str(item).strip() for item in descriptions if str(item).strip()]

        blocks: List[Dict[str, Any]] = []
        for block in bounding_polys:
            if not isinstance(block, dict):
                continue
            description = str(block.get("description", "")).strip()
            if not description:
                continue
            blocks.append({
                "description": description,
                "vertices": block.get("vertices") or [],
            })

        if not blocks:
            blocks = [{"description": line, "vertices": []} for line in lines]

        return lines, blocks

    if isinstance(result, list):
        lines = [str(item).strip() for item in result if str(item).strip()]
        return lines, [{"description": line, "vertices": []} for line in lines]

    return [], []
