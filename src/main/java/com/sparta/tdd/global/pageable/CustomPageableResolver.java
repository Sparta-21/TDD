package com.sparta.tdd.global.pageable;

import java.util.Set;
import javax.annotation.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

public class CustomPageableResolver extends PageableHandlerMethodArgumentResolver {

    private static final Set<Integer> ALLOWED = Set.of(10, 30, 50);
    private static final int DEFAULT_PAGE_SIZE = 10;

    public CustomPageableResolver(SortHandlerMethodArgumentResolver sortResolver) {
        super(sortResolver);
        this.setFallbackPageable(PageRequest.of(0, DEFAULT_PAGE_SIZE));
    }

    /**
     * 요청 파라미터로부터 Pageable을 해석합니다.
     * <p>
     * 스프링 기본 해석 결과를 바탕으로 page/size/sort를 가져오며,
     * size가 10, 30, 50이 아니라면 10으로 교정합니다.
     *
     * @param methodParameter
     *      현재 해석 중인 컨트롤러 메서드의 파라미터 메타데이터입니다.
     *      여기서는 주로 {@code Pageable} 파라미터의 위치/애노테이션 정보를 담고 있습니다.
     *
     * @param mavContainer
     *      (nullable) 현재 요청 처리 동안 사용되는 Model과 View 정보를 담는 컨테이너입니다.
     *      스프링 MVC의 일반적인 컨트롤러 호출에서는 대개 non-null이지만,
     *      계약(시그니처)상 null일 수 있으므로 @Nullable로 표기해야 합니다.
     *      이 리졸버에서는 직접 사용하지 않습니다.
     *
     * @param webRequest
     *      HTTP 요청/응답을 추상화한 객체입니다.
     *      쿼리 파라미터(예: {@code page}, {@code size}, {@code sort})를 읽을 때 사용됩니다.
     *      스펙상 non-null입니다.
     *
     * @param binderFactory
     *      (nullable) 파라미터 바인딩/검증을 위한 {@code WebDataBinder}를 생성하는 팩토리입니다.
     *      특정 상황에서는 전달되지 않을 수 있어 null일 수 있습니다(@Nullable 필요).
     *      이 리졸버에서는 직접 사용하지 않습니다.
     *
     * @return
     *      해석/교정된 {@link Pageable}. page와 sort는 그대로 유지하고,
     *      size는 {10, 30, 50} 외 입력 시 10으로 교정된 값이 반환됩니다.
     */
    @Override
    public Pageable resolveArgument(
        MethodParameter methodParameter,
        @Nullable ModelAndViewContainer mavContainer,
        NativeWebRequest webRequest,
        @Nullable WebDataBinderFactory binderFactory) {
        Pageable base = super.resolveArgument(methodParameter, mavContainer, webRequest,
            binderFactory);
        int normalized = ALLOWED.contains(base.getPageSize())
            ? base.getPageSize()
            : DEFAULT_PAGE_SIZE;
        if (normalized == base.getPageSize()) {
            return base;
        }
        return PageRequest.of(base.getPageNumber(), normalized, base.getSort());
    }
}
