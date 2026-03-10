package com.reverse.api.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ConditionalOnProperty(
        name = "spring.task.scheduling.enabled",
        havingValue = "true",
        matchIfMissing = false) // default
// false,
// must be
// explicitly
// enabled
public class SchedulingConfig {}
