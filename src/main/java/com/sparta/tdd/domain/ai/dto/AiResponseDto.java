package com.sparta.tdd.domain.ai.dto;

import com.sparta.tdd.domain.ai.entity.Ai;

import java.time.LocalDateTime;
import java.util.UUID;

public record AiResponseDto(
        UUID id,
        String inputText,
        String outputText,
        LocalDateTime createdAt
) {
    public static AiResponseDto from(Ai ai) {
        return new AiResponseDto(
                ai.getId(),
                ai.getInputText(),
                ai.getOutputText(),
                ai.getCreatedAt()
        );
    }
}
