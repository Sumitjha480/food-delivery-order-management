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

                        // Restaurant owner handles restaurant-side order lifecycle
                        .requestMatchers(HttpMethod.PATCH, "/orders/*/accept")
                        .hasAnyRole("ADMIN", "RESTAURANT_OWNER")

                        .requestMatchers(HttpMethod.PATCH, "/orders/*/reject")
                        .hasAnyRole("ADMIN", "RESTAURANT_OWNER")

                        .requestMatchers(HttpMethod.PATCH, "/orders/*/preparing")
                        .hasAnyRole("ADMIN", "RESTAURANT_OWNER")

                        //Delivery Partner Assignment
                        .requestMatchers(HttpMethod.PATCH, "/orders/*/assign-partner")
                        .hasAnyRole("ADMIN", "RESTAURANT_OWNER")

                        .requestMatchers(HttpMethod.PATCH, "/orders/*/claim")
                        .hasAnyRole("ADMIN", "DELIVERY_PARTNER")

                        // Delivery partner handles delivery-side lifecycle
                        .requestMatchers(HttpMethod.PATCH, "/orders/*/out-for-delivery")
                        .hasAnyRole("ADMIN", "DELIVERY_PARTNER")

                        .requestMatchers(HttpMethod.PATCH, "/orders/*/delivered")
                        .hasAnyRole("ADMIN", "DELIVERY_PARTNER")


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

                        // Notifications
                        .requestMatchers(HttpMethod.GET, "/notifications/**")
                        .hasAnyRole("ADMIN", "RESTAURANT_OWNER", "CUSTOMER", "DELIVERY_PARTNER")

                        .requestMatchers(HttpMethod.PATCH, "/notifications/*/read")
                        .hasAnyRole("ADMIN", "RESTAURANT_OWNER", "CUSTOMER", "DELIVERY_PARTNER")

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