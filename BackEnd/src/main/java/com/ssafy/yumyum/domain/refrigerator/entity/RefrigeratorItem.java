package com.ssafy.yumyum.domain.refrigerator.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefrigeratorItem {
    private Long id;
    private Long refrigeratorId;
    private String name;
    private FoodCategory category;
    private BigDecimal quantity;
    private String unit;
    private LocalDate expirationDate;
    private StorageLocation storageLocation;
    private String memo;
    private LocalDateTime createdAt;
}
