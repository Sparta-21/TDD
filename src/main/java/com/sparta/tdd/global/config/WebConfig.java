package com.sparta.tdd.global.config;

import com.sparta.tdd.global.pageable.CustomPageableResolver;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final String DEFAULT_SORT_PROPERTY = "createdAt";

    @Bean
    public SortHandlerMethodArgumentResolver sortHandlerMethodArgumentResolver() {
        SortHandlerMethodArgumentResolver sortHandlerMethodArgumentResolver = new SortHandlerMethodArgumentResolver();

        sortHandlerMethodArgumentResolver.setFallbackSort(Sort.by(
            Direction.DESC,
            DEFAULT_SORT_PROPERTY
        ));
        return sortHandlerMethodArgumentResolver;
    }

    @Bean
    public CustomPageableResolver customPageableResolver(
        SortHandlerMethodArgumentResolver sortHandlerMethodArgumentResolver
    ) {
        return new CustomPageableResolver(sortHandlerMethodArgumentResolver);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(customPageableResolver(sortHandlerMethodArgumentResolver()));
    }
}
