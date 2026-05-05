package org.zeroagent.api.config.security;

import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.*;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.zeroagent.api.config.security.error.GlobalAuthenticationEntryPoint;
import org.zeroagent.api.config.security.filter.AuthTokenFilter;
import org.zeroagent.api.config.security.filter.PublicAuthTokenIfExistFilter;
import org.zeroagent.api.config.security.filter.WebJwtTokenAuthManager;
import org.zeroagent.domain.support.notification.app.AppAlertHelper;

/**
 *
 * @author Nuk3m1
 * @version 2026年03月16日  13时17分
 */
@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {
    private final WebJwtTokenAuthManager         webJwtTokenAuthManager;
    private final AppAlertHelper                 appAlertHelper;
    private final GlobalAuthenticationEntryPoint globalAuthenticationEntryPoint;

    @Bean
    @Order(SecurityProperties.BASIC_AUTH_ORDER - 20)
    public SecurityFilterChain permitAllSecurityFilterChain(HttpSecurity http) throws Exception {
        PublicAuthTokenIfExistFilter publicAuthTokenIfExistFilter = new PublicAuthTokenIfExistFilter(webJwtTokenAuthManager, appAlertHelper);
        return http.cors(CorsConfigurer::disable)
                .csrf(CsrfConfigurer::disable)
                .httpBasic(HttpBasicConfigurer::disable)
                .formLogin(FormLoginConfigurer::disable)
                .logout(LogoutConfigurer::disable)
                .securityMatchers(requestMatcherConfigurer -> requestMatcherConfigurer
                        .dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.ASYNC)
                        .requestMatchers("/api/**")
                )
                .authorizeHttpRequests(requestMatcherRegistry -> requestMatcherRegistry.anyRequest().permitAll())
                .sessionManagement(configurer -> configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(configurer -> configurer.authenticationEntryPoint(globalAuthenticationEntryPoint))
                .addFilterAfter(publicAuthTokenIfExistFilter, ExceptionTranslationFilter.class)
                .build();
    }

    @Bean
    @Order(SecurityProperties.BASIC_AUTH_ORDER - 10)
    public SecurityFilterChain authenticatedSecurityFilterChain(HttpSecurity http) throws Exception {
        AuthTokenFilter authTokenFilter = new AuthTokenFilter(webJwtTokenAuthManager, appAlertHelper);
        return http.cors(CorsConfigurer::disable)
                .csrf(CsrfConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(FormLoginConfigurer::disable)
                .logout(LogoutConfigurer::disable)
                .authorizeHttpRequests(requestMatchersRegistry -> requestMatchersRegistry
//                        .requestMatchers("").authenticated()
                        .anyRequest().authenticated()
                )
                .sessionManagement(configurer -> configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(configurer -> configurer.authenticationEntryPoint(globalAuthenticationEntryPoint))
                .addFilterAfter(authTokenFilter, ExceptionTranslationFilter.class)
                .build();
    }
}
