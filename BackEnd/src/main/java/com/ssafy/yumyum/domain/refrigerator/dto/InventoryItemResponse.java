package com.ssafy.yumyum.domain.refrigerator.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.ssafy.yumyum.domain.refrigerator.entity.FoodCategory;
import com.ssafy.yumyum.domain.refrigerator.entity.RefrigeratorItem;
import com.ssafy.yumyum.domain.refrigerator.entity.StorageLocation;

public record InventoryItemResponse(
        Long id,
        String name,
        FoodCategory category,
        BigDecimal quantity,
        String unit,
        LocalDate expirationDate,
        StorageLocation storageLocation,
        String memo,
        LocalDate addedDate
) {
    public static InventoryItemResponse from(RefrigeratorItem item) {
        return new InventoryItemResponse(item.getId(), item.getName(), item.getCategory(), item.getQuantity(),
                item.getUnit(), item.getExpirationDate(), item.getStorageLocation(), item.getMemo(),
                item.getCreatedAt().toLocalDate());
    }
}
