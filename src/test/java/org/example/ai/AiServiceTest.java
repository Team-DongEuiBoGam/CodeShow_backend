package org.example.ai;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AiServiceTest {

    @Autowired
    private AiService aiService;

    @Test
    void realOpenAiApiTest() {
        // given: 분석할 간단한 코드
        String testCode = "int a = 10;\nint b = 20;\nint sum = a + b;";

        // when: AI 서비스 호출
        System.out.println("요청 전송 중... (약 2~5초 소요)");
        String response = aiService.analyzeCode(testCode);

        // then: 결과 출력
        System.out.println("========== [AI 응답 결과] ==========");
        System.out.println(response);
        System.out.println("====================================");
    }
}