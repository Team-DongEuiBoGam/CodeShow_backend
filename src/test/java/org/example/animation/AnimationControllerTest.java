package org.example.animation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;  // 추가된 부분
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete; // 추가된 부분
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.example.auth.UserRepository;
import org.example.auth.dto.SignupRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AnimationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AnimationMetadataRepository animationMetadataRepository;

    @Autowired
    private LanguageRepository languageRepository;

    @BeforeEach
    void setUp() {
        animationMetadataRepository.deleteAll();
        userRepository.deleteAll();

        if (!languageRepository.existsById(1L)) {
            languageRepository.save(new Language("Java"));
        }
    }

    @Test
    void userCanSaveAndViewAnimations() throws Exception {
        String accessToken = signupAndGetAccessToken("creator01", "크리에이터");

        mockMvc.perform(post("/api/animations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "animationName", "로딩 애니메이션",
                                "originalCode", "console.log('loading');",
                                "languageId", 1,
                                "jsonData", "{\"frames\":10}"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.animationName").value("로딩 애니메이션"))
                .andExpect(jsonPath("$.languageId").value(1))
                .andExpect(jsonPath("$.creatorUsername").value("크리에이터"))
                .andExpect(jsonPath("$.jsonData").value("{\"frames\":10}"));

        mockMvc.perform(get("/api/animations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].animationName").value("로딩 애니메이션"));
    }

    // 🌟 수정된 부분 1: 이제 게스트는 조회도 불가하므로 메서드명을 상황에 맞게 변경
    @Test
    void guestCannotViewAndCannotSaveAnimations() throws Exception {
        String userToken = signupAndGetAccessToken("owner01", "오너");

        mockMvc.perform(post("/api/animations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "animationName", "공개 애니메이션",
                                "originalCode", "print('public')",
                                "languageId", 1,
                                "jsonData", "{\"frames\":20}"
                        ))))
                .andExpect(status().isCreated());

        String guestLoginResponse = mockMvc.perform(post("/api/auth/guest-login"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String guestToken = objectMapper.readTree(guestLoginResponse).get("accessToken").asText();

        // 🌟 수정된 부분 2: 비회원(게스트)은 이제 목록 조회가 불가능하므로 isForbidden()을 기대해야 합니다!
        mockMvc.perform(get("/api/animations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + guestToken))
                .andExpect(status().isForbidden()); // 원래 isOk()였던 부분을 isForbidden()으로 변경

        mockMvc.perform(post("/api/animations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + guestToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "animationName", "차단됨",
                                "originalCode", "print('blocked')",
                                "languageId", 1,
                                "jsonData", "{\"frames\":30}"
                        ))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    private String signupAndGetAccessToken(String loginId, String username) throws Exception {
        SignupRequest signupRequest = new SignupRequest(loginId, "password123", username);
        String signupResponse = mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(signupResponse).get("accessToken").asText();
    }

    @Test
    void userCanUpdateAndDeleteAnimation() throws Exception {
        // 1. 회원가입 및 토큰 발급
        String accessToken = signupAndGetAccessToken("updatetest", "수정테스터");

        // 2. 테스트용 애니메이션 먼저 생성 (POST)
        String createResponse = mockMvc.perform(post("/api/animations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "animationName", "원래 이름",
                                "originalCode", "print('hello')",
                                "languageId", 2, // Python
                                "jsonData", "{}"
                        ))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // 생성된 애니메이션의 ID 추출
        Integer animationId = objectMapper.readTree(createResponse).get("animationId").asInt();

        // 3. 이름 수정 테스트 (PATCH)
        mockMvc.perform(patch("/api/animations/" + animationId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "animationName", "수정된 멋진 이름"
                        ))))
                .andExpect(status().isOk()) // 200 OK 기대
                .andExpect(jsonPath("$.animationName").value("수정된 멋진 이름")); // 이름이 잘 바뀌었는지 검증

        // 4. 애니메이션 삭제 테스트 (DELETE)
        mockMvc.perform(delete("/api/animations/" + animationId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isNoContent()); // 204 No Content 기대

        // 5. 삭제가 잘 되었는지 목록 조회로 최종 확인 (빈 배열이 나와야 함)
        mockMvc.perform(get("/api/animations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
