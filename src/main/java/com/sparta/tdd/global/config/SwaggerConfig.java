package com.sparta.tdd.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI toTastyApi() {
        return new OpenAPI()
            .info(new Info()
                .title("TDD API")
                .description("TDD API 명세서")
                .version("v1"))
            .servers(List.of(new Server().url("http://localhost:8080").description("로컬 서버")))
            .components(new Components()
                .addSecuritySchemes("AccessToken",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("Bearer")
                        .bearerFormat("JWT")))
            .addSecurityItem(new SecurityRequirement().addList("AccessToken"));
    }
}
