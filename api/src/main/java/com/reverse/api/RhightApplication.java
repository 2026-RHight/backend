package com.reverse.api;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "com.reverse")
@MapperScan(basePackages = "com.reverse")
@EnableJpaRepositories(basePackages = "com.reverse")
@EntityScan(basePackages = "com.reverse")
public class RhightApplication {

    public static void main(String[] args) {
        SpringApplication.run(RhightApplication.class, args);
    }
}
