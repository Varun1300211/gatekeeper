package com.gatekeeper.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class DataSourceConfig {

    @Configuration
    @Profile("h2")
    static class H2ProfileConfig {
    }

    @Configuration
    @Profile("postgres")
    static class PostgresProfileConfig {
    }
}
