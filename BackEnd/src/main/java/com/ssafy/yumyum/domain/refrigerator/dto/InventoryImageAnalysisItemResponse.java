package com.ssafy.yumyum.domain.refrigerator.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.ssafy.yumyum.domain.refrigerator.entity.FoodCategory;
import com.ssafy.yumyum.domain.refrigerator.entity.StorageLocation;

public record InventoryImageAnalysisItemResponse(
        String name,
        FoodCategory category,
        BigDecimal quantity,
        String unit,
        LocalDate expirationDate,
        StorageLocation storageLocation,
        String memo,
        String barcode,
        String rawText,
        Double confidence
) {
}
