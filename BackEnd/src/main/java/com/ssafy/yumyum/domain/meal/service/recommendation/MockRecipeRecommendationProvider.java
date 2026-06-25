package com.ssafy.yumyum.domain.meal.service.recommendation;

import java.util.List;

import org.springframework.stereotype.Component;

import com.ssafy.yumyum.domain.meal.dto.RecipeRecommendationItemRequest;
import com.ssafy.yumyum.domain.refrigerator.entity.RefrigeratorItem;

@Component
public class MockRecipeRecommendationProvider {
    public List<RecipeRecommendationItemRequest> recommend(List<RefrigeratorItem> inventory) {
        List<String> ingredients = inventory.stream()
                .map(RefrigeratorItem::getName)
                .filter(name -> name != null && !name.isBlank())
                .map(String::trim)
                .limit(6)
                .toList();
        List<String> availableIngredients = ingredients.isEmpty()
                ? List.of("데모 재료")
                : ingredients;
        String firstIngredient = availableIngredients.get(0);

        RecipeRecommendationItemRequest quickRice = new RecipeRecommendationItemRequest(
                firstIngredient + " 냉장고 볶음밥",
                "남은 재료를 빠르게 정리할 수 있는 데모 추천 요리입니다.",
                92,
                15,
                1,
                "쉬움",
                520,
                availableIngredients,
                List.of("밥", "간장"),
                List.of(
                        "재료를 먹기 좋은 크기로 손질합니다.",
                        "팬에 기름을 두르고 보유 재료를 먼저 볶습니다.",
                        "밥과 간장을 넣고 고르게 볶아 마무리합니다."
                ),
                List.of("소비기한이 가까운 재료부터 먼저 사용하세요.")
        );

        RecipeRecommendationItemRequest warmSalad = new RecipeRecommendationItemRequest(
                firstIngredient + " 따뜻한 샐러드",
                "가볍게 먹기 좋은 데모 추천 메뉴입니다.",
                84,
                12,
                1,
                "쉬움",
                360,
                availableIngredients,
                List.of("올리브오일"),
                List.of(
                        "재료를 씻고 물기를 제거합니다.",
                        "단단한 재료는 살짝 굽거나 데웁니다.",
                        "소스와 함께 가볍게 버무려 접시에 담습니다."
                ),
                List.of("냉장 재료는 조리 직전에 꺼내면 식감이 좋습니다.")
        );

        return List.of(quickRice, warmSalad);
    }
}
