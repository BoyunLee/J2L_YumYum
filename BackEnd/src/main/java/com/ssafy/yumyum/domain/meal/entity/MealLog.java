package com.ssafy.yumyum.domain.meal.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MealLog {
    private Long id;
    private Long userId;
    private MealType mealType;
    private LocalDateTime eatenAt;
    private String memo;
    private LocalDateTime createdAt;
    private List<MealLogItem> items = new ArrayList<>();
}
