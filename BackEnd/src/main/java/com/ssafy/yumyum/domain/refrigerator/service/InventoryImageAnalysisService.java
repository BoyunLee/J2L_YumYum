package com.ssafy.yumyum.domain.refrigerator.service;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ssafy.yumyum.domain.refrigerator.dto.ImageAnalysisType;
import com.ssafy.yumyum.domain.refrigerator.dto.InventoryImageAnalysisResponse;
import com.ssafy.yumyum.domain.refrigerator.service.imageanalysis.InventoryImageAnalysisProvider;
import com.ssafy.yumyum.global.exception.BusinessException;
import com.ssafy.yumyum.global.exception.ExceptionType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryImageAnalysisService {
    private static final long MAX_IMAGE_SIZE_BYTES = 10L * 1024 * 1024;

    private final InventoryImageAnalysisProvider imageAnalysisProvider;

    public InventoryImageAnalysisResponse analyze(ImageAnalysisType analysisType, MultipartFile image) {
        validate(image);
        try {
            return imageAnalysisProvider.analyze(
                    analysisType,
                    image.getBytes(),
                    image.getContentType(),
                    image.getOriginalFilename()
            );
        } catch (IOException exception) {
            throw new BusinessException(ExceptionType.IMAGE_READ_FAILED);
        }
    }

    private void validate(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new BusinessException(ExceptionType.EMPTY_IMAGE_FILE);
        }
        if (image.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new BusinessException(ExceptionType.IMAGE_FILE_TOO_LARGE);
        }
        String contentType = image.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            throw new BusinessException(ExceptionType.UNSUPPORTED_IMAGE_TYPE);
        }
    }
}
