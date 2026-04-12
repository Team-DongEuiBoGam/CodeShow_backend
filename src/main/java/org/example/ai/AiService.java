package org.example.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.List;
import java.util.Map;

@Service
public class AiService {

    private final RestClient restClient;
    private final String model;

    public AiService(
            @Value("${openai.api-key}") String apiKey,
            @Value("${openai.model}") String model
    ) {
        this.model = model;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.openai.com/v1/chat/completions")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public String analyzeCode(String code) {
        String prompt = "다음 코드를 분석해서 1) 'explanation'과 2) 애니메이션 시각화를 위한 'jsonData'를 만들어줘. " +
                "단, explanation과 JSON 내부의 모든 description 등 텍스트 값은 반드시 **한국어(Korean)**로 작성해줘! " +
                "반드시 JSON 객체 하나로만 응답해줘.\n코드: \n" + code; //

        Map<String, Object> requestBody = Map.of(
                "model", this.model,
                "messages", List.of(
                        Map.of("role", "system", "content", "너는 코드를 분석해서 JSON 형식으로만 완벽하게 응답하는 프로그래밍 전문 AI야. 모든 설명은 한국어로 해."), //
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.7,
                // OpenAI에게 무조건 순수 JSON 객체만 반환하도록 강제
                "response_format", Map.of("type", "json_object")
        );

        Map<String, Object> response = restClient.post()
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        String content = (String) message.get("content");

        // 마크다운(```json)이 붙어 오면 제거
        if (content != null && content.trim().startsWith("```json")) {
            content = content.replace("```json", "").replace("```", "").trim();
        }

        return content;
    }
}