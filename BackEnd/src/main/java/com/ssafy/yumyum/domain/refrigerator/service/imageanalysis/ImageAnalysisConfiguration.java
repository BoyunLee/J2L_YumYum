package com.ssafy.yumyum.domain.refrigerator.service.imageanalysis;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ImageAnalysisConfiguration {
    @Bean
    @ConditionalOnMissingBean(InventoryImageAnalysisProvider.class)
    InventoryImageAnalysisProvider inventoryImageAnalysisProvider() {
        return new PlaceholderInventoryImageAnalysisProvider();
    }
}
