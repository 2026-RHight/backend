package com.reverse.core.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SecurityHashPropertiesValidator {

    @Value("${security.hash.pepper:}")
    private String pepper;

    @PostConstruct
    public void validate() {
        if (pepper == null || pepper.trim().isEmpty()) {
            throw new IllegalStateException("Missing required env var: SECURITY_HASH_PEPPER");
        }
    }
}
