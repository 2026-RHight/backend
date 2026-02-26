package com.reverse.core.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Rhight API")
                        .description("Rhight backend API documentation")
                        .version("v1")
                        .contact(new Contact()
                                .name("Rhight Team")
                                .email("dev@rhight.local"))
                        .license(new License()
                                .name("Proprietary")));
    }
}
