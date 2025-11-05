package com.lts5.user.config;

import com.primes.library.util.SnowflakeIdGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SnowflakeConfig {

    @Bean("snowflakeIdGenerator")
    public SnowflakeIdGenerator snowflakeIdGenerator() {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator();
        generator.init();
        return generator;
    }
} 