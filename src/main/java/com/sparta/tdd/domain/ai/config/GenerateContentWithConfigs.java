package com.sparta.tdd.domain.ai.config;

import autovalue.shaded.com.google.common.collect.ImmutableList;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GoogleSearch;
import com.google.genai.types.HarmBlockThreshold;
import com.google.genai.types.HarmCategory;
import com.google.genai.types.Part;
import com.google.genai.types.SafetySetting;
import com.google.genai.types.ThinkingConfig;
import com.google.genai.types.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GenerateContentWithConfigs {

    @Value("${ai.google.api-key}")
    private String apiKey;

    @Bean
    public Client client() {
        return Client.builder().apiKey(apiKey).build();
    }

    @Bean
    public GenerateContentConfig generateContentConfig() {
        ImmutableList<SafetySetting> safetySettings =
            ImmutableList.of(
                SafetySetting.builder()
                    .category(HarmCategory.Known.HARM_CATEGORY_HATE_SPEECH)
                    .threshold(HarmBlockThreshold.Known.BLOCK_ONLY_HIGH)
                    .build(),
                SafetySetting.builder()
                    .category(HarmCategory.Known.HARM_CATEGORY_DANGEROUS_CONTENT)
                    .threshold(HarmBlockThreshold.Known.BLOCK_LOW_AND_ABOVE)
                    .build());

        Content systemInstruction = Content.fromParts(
            Part.fromText(
                "{사용자 입력}을 바탕으로 음식점에서 손님의 이목을 끌만한 20자 이하의 짧은 소개글을 만들어주세요. 문장은 한 줄로 작성합니다."));

        Tool googleSearchTool = Tool.builder().googleSearch(GoogleSearch.builder()).build();

        return GenerateContentConfig.builder()
            .thinkingConfig(ThinkingConfig.builder().thinkingBudget(0))
            .candidateCount(1)
            .maxOutputTokens(1024)
            .safetySettings(safetySettings)
            .systemInstruction(systemInstruction)
            .tools(googleSearchTool)
            .build();
    }
}
