package com.gatekeeper.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/",
                                "/flags/**",
                                "/evaluate",
                                "/metrics",
                                "/audit-logs",
                                "/sdk",
                                "/api/evaluate",
                                "/api/flags",
                                "/api/flags/*",
                                "/api/metrics/**",
                                "/api/audit-logs/**",
                                "/api/sdk/status",
                                "/api/sdk/available-flags",
                                "/api/sdk/evaluate")
                        .hasAnyRole("ADMIN", "VIEWER")
                        .requestMatchers(HttpMethod.POST, "/evaluate", "/sdk/evaluate")
                        .hasAnyRole("ADMIN", "VIEWER")
                        .requestMatchers("/flags/create",
                                "/flags/*/edit",
                                "/flags/*/delete",
                                "/flags/*/rules",
                                "/rules/**",
                                "/sdk/targets/**",
                                "/sdk/cache/**",
                                "/sdk/refresh-configured",
                                "/api/**")
                        .hasRole("ADMIN")
                        .anyRequest()
                        .authenticated())
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    UserDetailsService userDetailsService() {
        UserDetails admin = User.withUsername("admin")
                .password("{noop}admin123")
                .roles("ADMIN", "VIEWER")
                .build();

        UserDetails viewer = User.withUsername("viewer")
                .password("{noop}viewer123")
                .roles("VIEWER")
                .build();

        return new InMemoryUserDetailsManager(admin, viewer);
    }
}
