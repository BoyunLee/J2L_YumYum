package com.ssafy.yumyum.global.config;

import java.util.List;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

//Swagger-UI 확인
//http://localhost/swagger-ui.html
//http://localhost:8080/swagger-ui.html
@Configuration
public class SwaggerConfiguration {
	@Value("${company.logo-url}")
	private String companyLogoUrl;
	@Value("${company.temp-url-port}")
	private String companyLogoUrlPort;
	
//http://localhost:8080/image/chos.png

	@Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("YumYUm API Documentation")
                .version("v1.0")
//                .contact(new Contact().name("Support Team").email("support@company.com").url("https://company.com"))
//                .license(new License().name("Company License").url("https://company.com/license"))
			)
//            .externalDocs(new ExternalDocumentation()
//                .description("Company Documentation")
//                .url("https://company.com/docs")
//			)
//            .externalDocs(new ExternalDocumentation()
//                    .description("Company Logo")
//                    .url(companyLogoUrl)
//			)
            .servers(List.of(
//                new Server().url(companyLogoUrlPort).description("Production server"),
                new Server().url(companyLogoUrlPort).description("Staging server")
			));
            /*.components(new Components()
                    .addSecuritySchemes("bearer-token", new SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .name("Authorization")));
            .components(new Components()
                    .addSecuritySchemes("bearer-token", 
                        new SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .name("Authorization")
                    )
            )
            .addSecurityItem(new SecurityRequirement().addList("bearer-token"));
            */
    }
	@Bean
	public GroupedOpenApi publicApi() {
		return GroupedOpenApi.builder().group("com.ssafy.yumyum").pathsToMatch("/api/**").build();
	}
	/*
	@Bean
	public GroupedOpenApi public2Api() {
		return GroupedOpenApi.builder().group("com.ssafy.yumyum.jwt").pathsToMatch("/api/auth/**").build();
	}
	@Bean
	public GroupedOpenApi public3Api() {
		return GroupedOpenApi.builder().group("com.ssafy.yumyum.image").pathsToMatch("/api/boards/**").build();
	}
	*/
}