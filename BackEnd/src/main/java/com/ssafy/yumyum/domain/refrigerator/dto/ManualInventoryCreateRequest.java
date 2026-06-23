package com.ssafy.yumyum.domain.refrigerator.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.ssafy.yumyum.domain.refrigerator.entity.FoodCategory;
import com.ssafy.yumyum.domain.refrigerator.entity.StorageLocation;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ManualInventoryCreateRequest(
        @NotBlank(message = "상품명을 입력해 주세요.")
        @Size(max = 200, message = "상품명은 200자 이하로 입력해 주세요.")
        String name,
        @NotNull(message = "카테고리를 선택해 주세요.") FoodCategory category,
        @NotNull(message = "수량을 입력해 주세요.")
        @DecimalMin(value = "0.01", message = "수량은 0보다 커야 합니다.") BigDecimal quantity,
        @NotBlank(message = "수량 단위를 선택해 주세요.")
        @Size(max = 30, message = "수량 단위는 30자 이하로 입력해 주세요.") String unit,
        @NotNull(message = "유통기한을 입력해 주세요.") LocalDate expirationDate,
        @NotNull(message = "보관 위치를 선택해 주세요.") StorageLocation storageLocation,
        @Size(max = 500, message = "메모는 500자 이하로 입력해 주세요.") String memo
) {
}
