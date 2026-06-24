package com.ssafy.yumyum.domain.refrigerator.service.imageanalysis;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.ssafy.yumyum.global.exception.BusinessException;
import com.ssafy.yumyum.global.exception.ExceptionType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OcrImageCompressor {
    private final OcrImageCompressionProperties properties;

    public CompressedImage compress(byte[] imageBytes, String contentType) {
        BufferedImage sourceImage = readImage(imageBytes);
        if (sourceImage == null) {
            log.warn("Skipping OCR image compression because the image could not be decoded");
            return new CompressedImage(imageBytes, normalizeContentType(contentType), 0, 0, 0, 0, false);
        }

        int originalWidth = sourceImage.getWidth();
        int originalHeight = sourceImage.getHeight();
        BufferedImage resizedImage = resizeIfNeeded(sourceImage);
        byte[] compressedBytes = encodeJpeg(resizedImage, clampQuality(properties.jpegQuality()));

        float minQuality = clampQuality(properties.minJpegQuality());
        float quality = clampQuality(properties.jpegQuality());
        while (compressedBytes.length > properties.maxBytes() && quality > minQuality) {
            quality = Math.max(minQuality, quality - 0.1f);
            compressedBytes = encodeJpeg(resizedImage, quality);
        }

        if (compressedBytes.length > properties.maxBytes()) {
            BufferedImage scaledDownImage = resizedImage;
            while (compressedBytes.length > properties.maxBytes()
                    && scaledDownImage.getWidth() > 400
                    && scaledDownImage.getHeight() > 400) {
                scaledDownImage = resizeImage(
                        scaledDownImage,
                        Math.max(400, Math.round(scaledDownImage.getWidth() * 0.85f)),
                        Math.max(400, Math.round(scaledDownImage.getHeight() * 0.85f))
                );
                compressedBytes = encodeJpeg(scaledDownImage, minQuality);
            }
            resizedImage = scaledDownImage;
        }

        boolean compressed = !Objects.equals(normalizeContentType(contentType), MediaType.IMAGE_JPEG_VALUE)
                || originalWidth != resizedImage.getWidth()
                || originalHeight != resizedImage.getHeight()
                || compressedBytes.length != imageBytes.length;

        return new CompressedImage(
                compressedBytes,
                MediaType.IMAGE_JPEG_VALUE,
                originalWidth,
                originalHeight,
                resizedImage.getWidth(),
                resizedImage.getHeight(),
                compressed
        );
    }

    private BufferedImage readImage(byte[] imageBytes) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes)) {
            return ImageIO.read(inputStream);
        } catch (IOException exception) {
            log.warn("Failed to decode image before OCR compression", exception);
            return null;
        }
    }

    private BufferedImage resizeIfNeeded(BufferedImage sourceImage) {
        int maxWidth = Math.max(1, properties.maxWidth());
        int maxHeight = Math.max(1, properties.maxHeight());
        int width = sourceImage.getWidth();
        int height = sourceImage.getHeight();

        if (width <= maxWidth && height <= maxHeight) {
            return sourceImage;
        }

        double scale = Math.min((double) maxWidth / width, (double) maxHeight / height);
        int resizedWidth = Math.max(1, (int) Math.round(width * scale));
        int resizedHeight = Math.max(1, (int) Math.round(height * scale));
        return resizeImage(sourceImage, resizedWidth, resizedHeight);
    }

    private BufferedImage resizeImage(BufferedImage sourceImage, int width, int height) {
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = resizedImage.createGraphics();
        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, width, height);
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.drawImage(sourceImage, 0, 0, width, height, null);
        } finally {
            graphics.dispose();
        }
        return resizedImage;
    }

    private byte[] encodeJpeg(BufferedImage image, float quality) {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ImageWriteParam writeParam = writer.getDefaultWriteParam();
            if (writeParam.canWriteCompressed()) {
                writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                writeParam.setCompressionQuality(quality);
            }

            writer.setOutput(ImageIO.createImageOutputStream(outputStream));
            writer.write(null, new IIOImage(image, null, null), writeParam);
            writer.dispose();
            return outputStream.toByteArray();
        } catch (IOException exception) {
            writer.dispose();
            throw new BusinessException(ExceptionType.OCR_ANALYSIS_FAILED);
        }
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return MediaType.IMAGE_JPEG_VALUE;
        }
        return contentType;
    }

    private float clampQuality(float quality) {
        return Math.max(0.05f, Math.min(1.0f, quality));
    }

    public record CompressedImage(
            byte[] bytes,
            String contentType,
            int originalWidth,
            int originalHeight,
            int width,
            int height,
            boolean compressed
    ) {
    }
}
