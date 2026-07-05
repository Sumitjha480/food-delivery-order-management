package com.sumit.fooddelivery.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .userDetailsService(customUserDetailsService)
                .authorizeHttpRequests(auth -> auth

                        // Public endpoints
                        .requestMatchers(HttpMethod.GET, "/", "/health").permitAll()

                        .requestMatchers(HttpMethod.GET, "/restaurants/*/menu-items")
                        .hasAnyRole("ADMIN", "RESTAURANT_OWNER", "CUSTOMER", "DELIVERY_PARTNER")

                        .requestMatchers(HttpMethod.POST, "/restaurants/*/menu-items")
                        .hasAnyRole("ADMIN", "RESTAURANT_OWNER")

                        .requestMatchers(HttpMethod.GET, "/menu-items/**")
                        .hasAnyRole("ADMIN", "RESTAURANT_OWNER", "CUSTOMER", "DELIVERY_PARTNER")

                        .requestMatchers(HttpMethod.PUT, "/menu-items/**")
                        .hasAnyRole("ADMIN", "RESTAURANT_OWNER")

                        .requestMatchers(HttpMethod.DELETE, "/menu-items/**")
                        .hasAnyRole("ADMIN", "RESTAURANT_OWNER")

                        // Restaurant browsing
                        .requestMatchers(HttpMethod.GET, "/restaurants/**")
                        .hasAnyRole("ADMIN", "RESTAURANT_OWNER", "CUSTOMER", "DELIVERY_PARTNER")

                        // Admin manages restaurants
                        .requestMatchers(HttpMethod.POST, "/restaurants/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/restaurants/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/restaurants/**").hasRole("ADMIN")

                        // Customers place orders
                        .requestMatchers(HttpMethod.POST, "/orders")
                        .hasRole("CUSTOMER")

                        // Order viewing
                        .requestMatchers(HttpMethod.GET, "/orders/**")
                        .hasAnyRole("ADMIN", "RESTAURANT_OWNER", "CUSTOMER", "DELIVERY_PARTNER")

                        .requestMatchers(HttpMethod.DELETE, "/orders/**")
                        .hasRole("ADMIN")

                        // Admin manages customers
                        .requestMatchers("/customers/**")
                        .hasRole("ADMIN")

                        // Admin manages delivery partners
                        .requestMatchers("/delivery-partners/**")
                        .hasRole("ADMIN")

                        // Reviews
                        .requestMatchers(HttpMethod.GET, "/reviews/**")
                        .hasAnyRole("ADMIN", "RESTAURANT_OWNER", "CUSTOMER", "DELIVERY_PARTNER")

                        .requestMatchers(HttpMethod.POST, "/reviews/**")
                        .hasAnyRole("CUSTOMER", "ADMIN")

                        .requestMatchers(HttpMethod.PUT, "/reviews/**")
                        .hasAnyRole("CUSTOMER", "ADMIN")

                        .requestMatchers(HttpMethod.DELETE, "/reviews/**")
                        .hasAnyRole("CUSTOMER", "ADMIN")

                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .formLogin(form -> form.disable());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}