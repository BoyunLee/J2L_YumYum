package com.ssafy.yumyum.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {
	@Value("${company.origin-url}")
	private String allowOriginUrl;

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
		        .allowedOrigins(allowOriginUrl,"http://localhost:8090","http://localhost:5173")
				.allowedMethods(
						HttpMethod.GET.name(), 
						HttpMethod.POST.name(), 
						HttpMethod.PUT.name(),
						HttpMethod.DELETE.name(), 
						HttpMethod.HEAD.name(), 
						HttpMethod.OPTIONS.name(),
						HttpMethod.PATCH.name())
				.allowCredentials(true)
				.maxAge(1800); // 1800초 동안 preflight 결과를 캐시에 저장
	}

}
