package com.sparta.tdd.domain.ai.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.sparta.tdd.domain.ai.entity.Ai;
import com.sparta.tdd.domain.ai.repository.AiRepository;
import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.domain.user.repository.UserRepository;
import com.sparta.tdd.global.exception.BusinessException;
import com.sparta.tdd.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j(topic = "AI 코멘트 생성")
@Service
@RequiredArgsConstructor
public class AiService {

    private final AiRepository aiRepository;
    private final UserRepository userRepository;
    private final Client client;
    private final GenerateContentConfig config;

    public String createComment(String menuName, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
            new BusinessException(ErrorCode.USER_NOT_FOUND)
        );

        String response = generateText(menuName);
        Ai ai = Ai.of(menuName, response, user);

        aiRepository.save(ai);
        log.info(getLog(ai));
        return response;
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
