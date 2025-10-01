package com.sparta.tdd.domain.ai.dto;

public record AiResponseDto(
        String comment
) {
    public static AiResponseDto from(String text) {
        return new AiResponseDto(text);
    }
}
