package com.ssafy.yumyum.domain.refrigerator.service.imageanalysis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.yumyum.global.exception.BusinessException;
import com.ssafy.yumyum.global.exception.ExceptionType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PororoOcrClient {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final PororoOcrProperties properties;
    private final OcrImageCompressor ocrImageCompressor;

    public PororoOcrText extractText(byte[] imageBytes, String contentType, String originalFilename) {
        long startedAt = System.nanoTime();
        if (!properties.isConfigured()) {
            throw new BusinessException(ExceptionType.OCR_ANALYSIS_FAILED);
        }

        OcrImageCompressor.CompressedImage compressedImage = ocrImageCompressor.compress(imageBytes, contentType);
        long compressionMs = elapsedMillis(startedAt);
        String filename = resolveFilename(originalFilename, compressedImage.contentType());
        HttpHeaders partHeaders = new HttpHeaders();
        partHeaders.setContentDispositionFormData("image", filename);
        partHeaders.setContentType(resolveMediaType(compressedImage.contentType()));

        HttpEntity<ByteArrayResource> imagePart = new HttpEntity<>(
                new NamedByteArrayResource(compressedImage.bytes(), filename),
                partHeaders
        );

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", imagePart);
        body.add("lang", properties.lang());

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        try {
            log.info(
                    "Sending image to Pororo OCR bytes={} -> {}, size={}x{} -> {}x{}, compressed={}",
                    imageBytes.length,
                    compressedImage.bytes().length,
                    compressedImage.originalWidth(),
                    compressedImage.originalHeight(),
                    compressedImage.width(),
                    compressedImage.height(),
                    compressedImage.compressed()
            );
            long requestStartedAt = System.nanoTime();
            ResponseEntity<String> response = restTemplate.postForEntity(
                    properties.url(),
                    new HttpEntity<>(body, headers),
                    String.class
            );
            PororoOcrText result = parseResponse(response.getBody());
            log.info(
                    "Pororo OCR completed in {} ms (compression={} ms, request={} ms, lines={})",
                    elapsedMillis(startedAt),
                    compressionMs,
                    elapsedMillis(requestStartedAt),
                    result.lines().size()
            );
            return result;
        } catch (RestClientException | IOException exception) {
            log.error("Failed to call Pororo OCR service", exception);
            throw new BusinessException(ExceptionType.OCR_ANALYSIS_FAILED);
        }
    }

    private PororoOcrText parseResponse(String responseBody) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);
        List<String> lines = new ArrayList<>();
        for (JsonNode lineNode : root.path("lines")) {
            String line = lineNode.asText("").trim();
            if (!line.isEmpty()) {
                lines.add(line);
            }
        }

        String text = root.path("text").asText("").trim();
        boolean detected = root.path("detected").asBoolean(!text.isBlank());
        return new PororoOcrText(detected && !text.isBlank(), text, List.copyOf(lines));
    }

    private MediaType resolveMediaType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }

        try {
            return MediaType.parseMediaType(contentType);
        } catch (IllegalArgumentException exception) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    private String resolveFilename(String originalFilename, String contentType) {
        if (originalFilename != null && !originalFilename.isBlank()) {
            return originalFilename;
        }

        String extension = switch (contentType == null ? "" : contentType.toLowerCase()) {
            case MediaType.IMAGE_PNG_VALUE -> ".png";
            case MediaType.IMAGE_GIF_VALUE -> ".gif";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
        return "upload" + extension;
    }

    public record PororoOcrText(
            boolean detected,
            String text,
            List<String> lines
    ) {
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }

    private static final class NamedByteArrayResource extends ByteArrayResource {
        private final String filename;

        private NamedByteArrayResource(byte[] byteArray, String filename) {
            super(byteArray);
            this.filename = filename;
        }

        @Override
        public String getFilename() {
            return filename;
        }
    }
}
