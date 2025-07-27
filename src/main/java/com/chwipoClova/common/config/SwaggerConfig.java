package com.chwipoClova.common.config;

import com.chwipoClova.common.service.JwtProviderService;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        String key = JwtProviderService.AUTHORIZATION;

        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement()
                        .addList(key)
                )
                .info(apiInfo())
                .components(new Components()
                        .addSecuritySchemes(key, new SecurityScheme()
                                .name(key)
                                .scheme(JwtProviderService.BEARER)
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .bearerFormat("JWT"))
                );
    }

    private Info apiInfo() {
        return new Info()
                .title("Springdoc")
                .description("Springdoc을 사용한 Swagger UI")
                .version("1.0.0");
    }
}
