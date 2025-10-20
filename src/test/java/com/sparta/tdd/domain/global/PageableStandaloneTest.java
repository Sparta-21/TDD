package com.sparta.tdd.domain.global;


import com.sparta.tdd.global.pageable.CustomPageableResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PageableStandaloneTest {

    MockMvc mvc;

    @BeforeEach
    void setUp() {
        // 1) 정렬 리졸버 (기본 DESC: createdAt)
        var sortResolver = new SortHandlerMethodArgumentResolver();
        sortResolver.setFallbackSort(Sort.by(Sort.Direction.DESC, "createdAt"));

        // 2) 커스텀 Pageable 리졸버
        var pageableResolver = new CustomPageableResolver(sortResolver);

        // 3) 컨트롤러 인스턴스 직접 생성 (의존성 있으면 Mockito로 목 생성자 주입)
        var controller = new PageProbeController();

        // 4) Standalone MockMvc 구성
        mvc = MockMvcBuilders
            .standaloneSetup(controller)
            .setCustomArgumentResolvers(pageableResolver /*, sortResolver - Sort를 직접 파라미터로 받는 엔드포인트가 있을 때만 */)
            .setMessageConverters(new MappingJackson2HttpMessageConverter()) // JSON 직렬화
            // .setControllerAdvice(new GlobalExceptionHandler()) // 필요 시 예외 핸들러도 수동 추가
            .build();
    }

    @Test
    @DisplayName("허용값: size=30 그대로")
    void allowedSize30() throws Exception {
        mvc.perform(get("/_probe/page").param("size", "30"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size", is(30)))
            .andExpect(jsonPath("$.sort", containsString("createdAt:DESC"))); // sort 미지정 → 기본 DESC
    }

    @Test
    @DisplayName("화이트리스트 교정: size=15 → 10")
    void normalizeTo10() throws Exception {
        mvc.perform(get("/_probe/page").param("size", "15"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size", is(10)));
    }

    @Test
    @DisplayName("상한 후 허용: size=100 → 10")
    void capTo50() throws Exception {
        mvc.perform(get("/_probe/page").param("size", "100"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size", is(10)));
    }

    @Test
    @DisplayName("정렬 기본 DESC 적용")
    void sortFallbackDesc() throws Exception {
        mvc.perform(get("/_probe/page"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sort", containsString("createdAt:DESC")));
    }

    @Test
    @DisplayName("정렬 지정시 클라이언트 우선: name,asc")
    void sortOverrideAsc() throws Exception {
        mvc.perform(get("/_probe/page").param("sort", "name,asc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sort", containsString("name:ASC")));
    }
}