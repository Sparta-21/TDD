package com.sparta.tdd.domain.ai.controller;

import com.sparta.tdd.domain.ai.dto.AiRequestDto;
import com.sparta.tdd.domain.ai.dto.AiResponseDto;
import com.sparta.tdd.domain.ai.service.AiService;
import com.sparta.tdd.domain.auth.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/ai")
public class AiController {

    private final AiService aiService;

    @PostMapping("/req")
    @Operation(summary = "AI 글 생성")
    public ResponseEntity<AiResponseDto> createComment(@RequestBody AiRequestDto requestDto,
                                                       @AuthenticationPrincipal UserDetailsImpl userDetails) {
        AiResponseDto createdComment = aiService.createComment(requestDto, userDetails);
        return ResponseEntity.ok(createdComment);
    }
}
