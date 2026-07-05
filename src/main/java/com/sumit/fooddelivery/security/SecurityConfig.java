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

                        // public health endpoints
                        .requestMatchers(HttpMethod.GET, "/", "/health").permitAll()

                        // restaurant browsing is allowed to all authenticated roles
                        .requestMatchers(HttpMethod.GET, "/restaurants/**")
                        .hasAnyRole("ADMIN", "RESTAURANT_OWNER", "CUSTOMER", "DELIVERY_PARTNER")

                        // admin manages restaurants
                        .requestMatchers(HttpMethod.POST, "/restaurants/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/restaurants/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/restaurants/**").hasRole("ADMIN")

                        // menu browsing is allowed to all authenticated roles
                        .requestMatchers(HttpMethod.GET, "/menu-items/**")
                        .hasAnyRole("ADMIN", "RESTAURANT_OWNER", "CUSTOMER", "DELIVERY_PARTNER")

                        .requestMatchers(HttpMethod.GET, "/restaurants/*/menu-items")
                        .hasAnyRole("ADMIN", "RESTAURANT_OWNER", "CUSTOMER", "DELIVERY_PARTNER")

                        // menu management by admin or restaurant owner
                        .requestMatchers(HttpMethod.POST, "/restaurants/*/menu-items")
                        .hasAnyRole("ADMIN", "RESTAURANT_OWNER")

                        .requestMatchers(HttpMethod.PUT, "/menu-items/**")
                        .hasAnyRole("ADMIN", "RESTAURANT_OWNER")

                        .requestMatchers(HttpMethod.DELETE, "/menu-items/**")
                        .hasAnyRole("ADMIN", "RESTAURANT_OWNER")

                        // customers place orders
                        .requestMatchers(HttpMethod.POST, "/orders")
                        .hasRole("CUSTOMER")

                        // order viewing for now; ownership filtering comes in later section
                        .requestMatchers(HttpMethod.GET, "/orders/**")
                        .hasAnyRole("ADMIN", "RESTAURANT_OWNER", "CUSTOMER", "DELIVERY_PARTNER")

                        // deleting orders should not be open to customers
                        .requestMatchers(HttpMethod.DELETE, "/orders/**")
                        .hasRole("ADMIN")

                        // admin manages customer records for now
                        .requestMatchers("/customers/**")
                        .hasRole("ADMIN")

                        // admin manages delivery partners
                        .requestMatchers("/delivery-partners/**")
                        .hasRole("ADMIN")

                        // customers create/update reviews, all authenticated users can read reviews
                        .requestMatchers(HttpMethod.GET, "/reviews/**")
                        .hasAnyRole("ADMIN", "RESTAURANT_OWNER", "CUSTOMER", "DELIVERY_PARTNER")

                        .requestMatchers(HttpMethod.POST, "/reviews/**")
                        .hasAnyRole("CUSTOMER", "ADMIN")

                        .requestMatchers(HttpMethod.PUT, "/reviews/**")
                        .hasAnyRole("CUSTOMER", "ADMIN")

                        .requestMatchers(HttpMethod.DELETE, "/reviews/**")
                        .hasAnyRole("CUSTOMER", "ADMIN")

                        // everything else requires authentication
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