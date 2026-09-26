package com.example.ems.security;

import com.example.ems.security.provider.SessionAuthenticationProvider;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.maintenance.service.MaintenanceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import com.example.ems.audit.service.AuditLogService;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.security.service.JwtService;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

        private final ObjectMapper objectMapper = new ObjectMapper();

        @Bean
        public AuthenticationEntryPoint authenticationEntryPoint() {
                return (request, response, authException) -> {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        ErrorResponse errorResponse = ErrorResponse.error("Unauthorized", "AUTH_014");
                        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
                };
        }

        @Bean
        public AuthenticationManager authenticationManager(
                        SessionAuthenticationProvider sessionAuthenticationProvider) {
                return new ProviderManager(List.of(sessionAuthenticationProvider));
        }

        @Bean
        @org.springframework.context.annotation.Primary
        public CorsConfigurationSource corsConfigurationSource(
                        @Value("${app.frontend-url:http://localhost:3000}") String frontendUrl) {
                CorsConfiguration configuration = new CorsConfiguration();
                List<String> origins = new ArrayList<>(List.of(
                                "http://localhost:3000",
                                "http://127.0.0.1:3000",
                                "http://192.168.1.35:3000",
                                "http://255.255.255.0:3000"));
                if (frontendUrl != null && !frontendUrl.isBlank() && !origins.contains(frontendUrl)) {
                        origins.add(frontendUrl);
                }
                configuration.setAllowedOrigins(origins);
                configuration.setAllowedOriginPatterns(List.of("*"));
                configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(
                                List.of("Authorization", "Content-Type", "X-DEV-TOKEN", "X-Organization-Id",
                                                "X-Tenant-Id", "organization-id"));
                configuration.setAllowCredentials(true);
                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http,
                        AuthenticationManager authenticationManager,
                        AuthenticationEntryPoint entryPoint,
                        Environment environment,
                        CorsConfigurationSource corsConfigurationSource,
                        JwtService jwtService,
                        UserRepository userRepository,
                        MaintenanceService maintenanceService,
                        @Autowired(required = false) AuditLogService auditLogService) throws Exception {
                JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(
                                authenticationManager, entryPoint, environment, jwtService, userRepository);

                http
                                .csrf(AbstractHttpConfigurer::disable)
                                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                "/api/v1/auth/login",
                                                                "/api/v1/auth/signup",
                                                                "/api/v1/admin/users",
                                                                "/api/v1/admin/users/**",
                                                                "/api/v1/auth/email/verify",
                                                                "/api/v1/auth/check-organization",
                                                                "/api/v1/auth/forgot-password",
                                                                "/api/v1/auth/verify-otp",
                                                                "/api/v1/auth/resend-otp",
                                                                "/api/v1/auth/reset-password",
                                                                "/api/v1/auth/accept-invitation",
                                                                "/api/v1/auth/activate",
                                                                "/api/v1/auth/refresh",
                                                                "/api/v1/auth/logout",
                                                                "/api/v1/auth/check-email",
                                                                "/api/v1/auth/check-phone",
                                                                "/api/files/*/download",
                                                                "/api/v1/permissions/catalog",
                                                                "/api/v1/assets/verify/**",
                                                                "/api/v1/public/recruitment/**",
                                                                "/api/v1/public/maintenance",
                                                                "/api/v1/public/maintenance/**",
                                                                "/v3/api-docs/**",
                                                                "/swagger-ui/**",
                                                                "/swagger-ui.html",
                                                                "/swagger-resources/**",
                                                                "/webjars/**",
                                                                "/actuator/**")
                                                .permitAll()
                                                .anyRequest().authenticated())
                                .exceptionHandling(ex -> ex.authenticationEntryPoint(entryPoint))
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                MaintenanceModeFilter maintenanceModeFilter = new MaintenanceModeFilter(maintenanceService,
                                auditLogService);
                http.addFilterAfter(maintenanceModeFilter, JwtAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistrationBean(
                        AuthenticationManager authenticationManager,
                        AuthenticationEntryPoint entryPoint,
                        Environment environment,
                        JwtService jwtService,
                        UserRepository userRepository) {
                JwtAuthenticationFilter filter = new JwtAuthenticationFilter(authenticationManager, entryPoint,
                                environment,
                                jwtService, userRepository);
                FilterRegistrationBean<JwtAuthenticationFilter> registrationBean = new FilterRegistrationBean<>(filter);
                registrationBean.setEnabled(false); // Prevents Spring Boot from registering it in global servlet filter
                                                    // chain
                return registrationBean;
        }

        @Bean
        public FilterRegistrationBean<MaintenanceModeFilter> maintenanceFilterRegistrationBean(
                        MaintenanceService maintenanceService,
                        @Autowired(required = false) AuditLogService auditLogService) {
                MaintenanceModeFilter filter = new MaintenanceModeFilter(maintenanceService, auditLogService);
                FilterRegistrationBean<MaintenanceModeFilter> registrationBean = new FilterRegistrationBean<>(filter);
                registrationBean.setEnabled(false);
                return registrationBean;
        }
}
