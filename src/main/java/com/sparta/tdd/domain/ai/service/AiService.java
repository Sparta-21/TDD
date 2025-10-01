package com.sparta.tdd.domain.ai.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.sparta.tdd.domain.ai.dto.AiRequestDto;
import com.sparta.tdd.domain.ai.dto.AiResponseDto;
import com.sparta.tdd.domain.ai.entity.Ai;
import com.sparta.tdd.domain.ai.repository.AiRepository;
import com.sparta.tdd.domain.auth.UserDetailsImpl;
import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiService {

    private final AiRepository aiRepository;
    private final UserRepository userRepository;

    @Value("${GOOGLE_API_KEY}")
    private String key;
    public AiResponseDto createComment(AiRequestDto requestDto, UserDetailsImpl userDetails) {
        Client client = Client.builder().apiKey(key).build();

        GenerateContentResponse contentResponse =
                client.models.generateContent("gemini-2.5-flash", "다음 요청에 대한 소개글을 20자를 넘지않게 작성해줘. " + requestDto.comment(), null);

        User user = userRepository.findById(userDetails.getUserId()).orElseThrow(() ->
                new IllegalArgumentException("존재하지 않는 회원입니다.")
        );

        Ai ai = Ai.builder()
                .inputText(requestDto.comment())
                .outputText(contentResponse.text())
                .user(user)
                .build();

        aiRepository.save(ai);
        return AiResponseDto.from(ai.getOutputText());
    }
}
