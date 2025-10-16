package com.sparta.tdd.domain.global;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/_probe/page")
public class PageProbeController {
    @GetMapping
    public Map<String, Object> probe(Pageable pageable) {
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("page", pageable.getPageNumber());
        resp.put("size", pageable.getPageSize());

        // 정렬을 "property:DIR,property2:DIR" 형태로 출력
        String sortStr = pageable.getSort().stream()
            .map(o -> o.getProperty() + ":" + o.getDirection().name())
            .reduce((a,b) -> a + "," + b)
            .orElse("");
        resp.put("sort", sortStr);
        return resp;
    }
}
