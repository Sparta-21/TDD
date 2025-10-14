package com.sparta.tdd.global.pageable;

import java.util.Set;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class CustomPageableResolver extends PageableHandlerMethodArgumentResolver {

    private static final Set<Integer> ALLOWED = Set.of(10, 30, 50);
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_SIZE = 50;

    public CustomPageableResolver(SortHandlerMethodArgumentResolver sortResolver) {
        super(sortResolver);
        this.setFallbackPageable(PageRequest.of(0, DEFAULT_PAGE_SIZE));
        this.setMaxPageSize(MAX_SIZE);
    }

    @Override
    public Pageable resolveArgument(MethodParameter methodParameter,
        ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
        WebDataBinderFactory binderFactory) {
        Pageable base = super.resolveArgument(methodParameter, mavContainer, webRequest,
            binderFactory);
        int capped = Math.min(base.getPageSize(), MAX_SIZE);
        int normalized = ALLOWED.contains(capped)
            ? capped
            : DEFAULT_PAGE_SIZE;
        return (normalized == base.getPageSize())
            ? base
            : PageRequest.of(base.getPageNumber(), normalized, base.getSort());
    }
}
