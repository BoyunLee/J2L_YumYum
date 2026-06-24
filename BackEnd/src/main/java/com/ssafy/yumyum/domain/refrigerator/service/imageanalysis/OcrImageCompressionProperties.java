package com.ssafy.yumyum.domain.refrigerator.service.imageanalysis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public record OcrImageCompressionProperties(
        @Value("${ocr.image-compression.max-bytes:3145728}") long maxBytes,
        @Value("${ocr.image-compression.max-width:1600}") int maxWidth,
        @Value("${ocr.image-compression.max-height:1600}") int maxHeight,
        @Value("${ocr.image-compression.jpeg-quality:0.8}") float jpegQuality,
        @Value("${ocr.image-compression.min-jpeg-quality:0.5}") float minJpegQuality
) {
}
