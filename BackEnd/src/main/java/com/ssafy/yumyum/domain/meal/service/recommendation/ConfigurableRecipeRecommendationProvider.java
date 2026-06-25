package com.ssafy.yumyum.domain.meal.service.recommendation;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.ssafy.yumyum.domain.meal.dto.RecipeRecommendationItemRequest;
import com.ssafy.yumyum.domain.refrigerator.entity.RefrigeratorItem;
import com.ssafy.yumyum.global.config.IntegrationModeProperties;
import com.ssafy.yumyum.global.exception.BusinessException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Primary
@Component
@RequiredArgsConstructor
public class ConfigurableRecipeRecommendationProvider implements RecipeRecommendationProvider {
    private final IntegrationModeProperties integrationModeProperties;
    private final MockRecipeRecommendationProvider mockProvider;
    private final GmsRecipeRecommendationProvider liveProvider;

    @Override
    public List<RecipeRecommendationItemRequest> recommend(List<RefrigeratorItem> inventory) {
        if (integrationModeProperties.isMock()) {
            return mockProvider.recommend(inventory);
        }
        if (integrationModeProperties.isLive()) {
            return liveProvider.recommend(inventory);
        }

        try {
            return liveProvider.recommend(inventory);
        } catch (BusinessException exception) {
            log.warn("Recipe recommendation failed in fallback mode: code={}",
                    exception.getExceptionType().getCode());
            return mockProvider.recommend(inventory);
        } catch (RuntimeException exception) {
            log.warn("Recipe recommendation failed in fallback mode", exception);
            return mockProvider.recommend(inventory);
        }
    }
}
