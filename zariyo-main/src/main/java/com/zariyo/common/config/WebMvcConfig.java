package com.zariyo.common.config;

import com.zariyo.common.interceptor.TokenValidationInterceptor;
import com.zariyo.common.validation.TokenValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final TokenValidation tokenValidation;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new TokenValidationInterceptor(tokenValidation))
                .excludePathPatterns("/**"); // 현재는 전체 제외 (단순 조회 허용 목적)
        // TODO: 향후 보호 경로만 addPathPatterns()으로 명시
    }
}
