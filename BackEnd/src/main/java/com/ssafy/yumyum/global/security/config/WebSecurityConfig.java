package com.ssafy.yumyum.global.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.ssafy.yumyum.global.security.jwt.TokenAuthenticationFilter;
import com.ssafy.yumyum.global.security.jwt.TokenExceptionHandlerFilter;
import com.ssafy.yumyum.global.security.jwt.TokenProvider;
import com.ssafy.yumyum.global.security.service.CustomUserDetailsService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {
    private final TokenProvider tokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        TokenAuthenticationFilter tokenAuthenticationFilter = new TokenAuthenticationFilter(tokenProvider, customUserDetailsService);
        http
                .logout(AbstractHttpConfigurer::disable)  // 시큐리티에서 기본적으로 제공하는 로그아웃 무효화
                .csrf(AbstractHttpConfigurer::disable) //csrf 무시
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("").permitAll()
                        .anyRequest().denyAll()
                );
        // JWT 필터를 UsernamePasswordAuthenticationFilter 앞에 추가
        http.addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(new TokenExceptionHandlerFilter(), TokenAuthenticationFilter.class);
        http.sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        // FormLogin, BasicHttp 비활성화
        http.formLogin(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
