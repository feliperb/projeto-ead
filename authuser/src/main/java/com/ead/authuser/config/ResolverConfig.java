package com.ead.authuser.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class ResolverConfig implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        PageableHandlerMethodArgumentResolver resolver = new PageableHandlerMethodArgumentResolver();
        resolver.setFallbackPageable(PageRequest.of(0, 10)); // Define a página padrão como 0 e o tamanho como 10
        resolvers.add(resolver);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/users/**").allowedOrigins("http://example.com").maxAge(3600);
//        registry.addMapping("/auth/**").allowedOrigins("http://abc.com").maxAge(3600);
        registry.addMapping("/**").maxAge(3600);
    }
}
