# YumYum Docker Setup

`docker-compose.yml` has been added so the project can be started with `frontend`, `backend`, `mysql`, and the `pororo-ocr` service together.

## Quick Start

1. Copy `.env.docker.example` to `.env`.
2. Fill in at least:
   - `JWT_SECRET_KEY`
   - `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`
   - `NAVER_CLIENT_ID`, `NAVER_CLIENT_SECRET`
   - `KAKAO_CLIENT_ID`, `KAKAO_CLIENT_SECRET`
   - `GMS_API_KEY` and `GMS_KEY` if you use recipe recommendation or OCR JSON structuring
   - `FOOD_SAFETY_API_KEY` if you use barcode product lookup
3. Start containers:

```bash
docker compose up --build
```

## Ports

- Frontend: `http://localhost:5173`
- Backend: `http://localhost:8080`
- Pororo OCR: `http://localhost:8001`
- MySQL: `localhost:3307`

## OAuth2 Redirect URIs

Register these callback URLs in each OAuth provider console:

- `http://localhost:8080/login/oauth2/code/google`
- `http://localhost:8080/login/oauth2/code/naver`
- `http://localhost:8080/login/oauth2/code/kakao`

The frontend starts login from `http://localhost:5173` and the backend OAuth2 callback stays on port `8080`, so both ports need to stay open.

## Notes

- The DB schema is initialized from `BackEnd/src/main/resources/sql/yumyum.sql` on the first MySQL container startup.
- OCR requests now flow through `Pororo OCR -> GPT-5-nano JSON structuring`.
- The first OCR request can take longer because the Pororo model may need to be downloaded inside the `pororo-ocr` container.
- If you need to recreate the database from scratch, run `docker compose down -v` and start again.
