package com.ssafy.yumyum.domain.refrigerator.service.imageanalysis;

public record FoodSafetyProduct(
        String barcode,
        String productName,
        String shelfLife,
        String foodType,
        String manufacturer,
        String productReportNumber
) {
}
