package org.example.animation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.example.auth.dto.SignupRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // 🚨 이 한 줄이 추가되어야 진짜 DB가 아닌 깨끗한 테스트 DB를 씁니다!
@Transactional
class AnimationExceptionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void throwExceptionWhenImageUploaded() throws Exception {
        // given: 매번 겹치지 않는 새로운 아이디 생성 (안전장치 추가)
        String uniqueLoginId = "testuser_" + System.currentTimeMillis();
        String userToken = signupAndGetAccessToken(uniqueLoginId, "테스터");

        // when: 코드 대신 이미지 파일명(.png)이 포함된 요청
        Map<String, Object> invalidRequest = Map.of(
                "animationName", "에러 테스트",
                "originalCode", "my_code_screenshot.png", // 이미지 확장자 포함
                "languageId", 1,
                "jsonData", "{}"
        );

        // then: 400 Bad Request와 함께 우리가 설정한 에러 메시지가 반환되어야 함
        mockMvc.perform(post("/api/animations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print()) // 결과를 콘솔에 출력
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("이미지 파일은 지원하지 않습니다. 텍스트 형태의 코드를 직접 입력해 주세요."));
    }

    // 토큰 발급용 헬퍼 메서드
    private String signupAndGetAccessToken(String loginId, String username) throws Exception {
        SignupRequest signupRequest = new SignupRequest(loginId, "password123", username);
        String signupResponse = mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(signupResponse).get("accessToken").asText();
    }
}