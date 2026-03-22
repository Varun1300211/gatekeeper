package com.gatekeeper.config;

import com.gatekeeper.model.Environment;
import com.gatekeeper.repository.EnvironmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final EnvironmentRepository environmentRepository;

    @Bean
    CommandLineRunner seedEnvironments() {
        return args -> {
            seedEnvironment("test");
            seedEnvironment("uat");
            seedEnvironment("prod");
        };
    }

    private void seedEnvironment(String name) {
        environmentRepository.findByName(name)
                .orElseGet(() -> environmentRepository.save(Environment.builder().name(name).build()));
    }
}
