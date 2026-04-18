package org.zeroagent.api.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 *
 * @author Nuk3m1
 * @version 2026年03月16日  13时17分
 */
@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // 禁用 CSRF
                .authorizeHttpRequests(auth -> auth
                        // 🚀 放行我们的测试接口
                        .requestMatchers("/api/**").permitAll()
                        // 其他全部拦截
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
