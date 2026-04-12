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
        // 1. 프론트엔드가 정확히 기대하는 JSON 형식을 프롬프트에 예시로 박아줍니다.
        String prompt = "다음 코드를 분석해서 1) 'explanation'과 2) 애니메이션 시각화를 위한 'jsonData'를 만들어줘. \n" +
                "단, explanation과 JSON 내부의 모든 텍스트는 반드시 **한국어(Korean)**로 작성해!\n" +
                "반드시 아래의 예시와 동일한 JSON 구조(Schema)를 지켜서 응답해줘. \n" +
                "특히 jsonData 안에는 반드시 'variables' 배열과 'operations' 배열이 있어야 해!\n\n" +
                "[JSON 응답 구조 예시]\n" +
                "{\n" +
                "  \"explanation\": \"코드에 대한 전체 설명\",\n" +
                "  \"jsonData\": {\n" +
                "    \"variables\": [\n" +
                "       { \"name\": \"변수명\", \"type\": \"자료형\", \"value\": \"값\", \"description\": \"변수에 대한 설명\" }\n" +
                "    ],\n" +
                "    \"operations\": [\n" +
                "       { \"target\": \"대상변수명\", \"source\": \"참조변수명(없으면 생략)\", \"description\": \"수행한 동작 설명\" }\n" +
                "    ]\n" +
                "  }\n" +
                "}\n\n" +
                "분석할 코드: \n" + code;

        Map<String, Object> requestBody = Map.of(
                "model", this.model,
                "messages", List.of(
                        Map.of("role", "system", "content", "너는 코드를 분석해서 지정된 JSON 형식으로만 완벽하게 응답하는 프로그래밍 전문 AI야."),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.1, // 2. 창의성을 낮추고 일관성(정확성)을 극대화합니다.
                "response_format", Map.of("type", "json_object")
        );

        Map<String, Object> response = restClient.post()
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        String content = (String) message.get("content");

        // 3. 정규식 대신 단순 replace로 안전하게 마크다운 잔여물을 제거합니다.
        if (content != null) {
            content = content.replace("```json", "")
                    .replace("```", "")
                    .trim();
        }

        return content;
    }
}