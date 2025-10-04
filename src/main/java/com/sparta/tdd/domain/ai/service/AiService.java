package com.sparta.tdd.domain.ai.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.sparta.tdd.domain.ai.dto.AiRequestDto;
import com.sparta.tdd.domain.ai.dto.AiResponseDto;
import com.sparta.tdd.domain.ai.entity.Ai;
import com.sparta.tdd.domain.ai.repository.AiRepository;
import com.sparta.tdd.domain.auth.UserDetailsImpl;
import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j(topic = "AI 코멘트 생성")
@Service
@RequiredArgsConstructor
public class AiService {

    private final AiRepository aiRepository;
    private final UserRepository userRepository;
    private final Client client;
    private final GenerateContentConfig config;

    public AiResponseDto createComment(AiRequestDto requestDto, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new IllegalArgumentException("존재하지 않는 회원입니다.")
        );

        String response = generateText(requestDto.comment());
        Ai ai = Ai.of(requestDto.comment(), response, user);

        aiRepository.save(ai);
        log.info(getLog(ai));
        return AiResponseDto.from(ai);
    }
    private String generateText(String comment) {
        return client.models.generateContent("gemini-2.5-flash", comment, config).text();
    }
    private String getLog(Ai ai) {
        return String.format(
                "AI Comment Log => User: %d | Input: \"%s\" | Output: \"%s\" | CreatedAt: %s",
                ai.getUser().getId(),
                ai.getInputText(),
                ai.getOutputText(),
                ai.getCreatedAt()
        );
    }
}
