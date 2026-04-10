package org.example.animation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @Test
    void guestCanViewButCannotSaveAnimations() throws Exception {
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

        mockMvc.perform(get("/api/animations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + guestToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].animationName").value("공개 애니메이션"));

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
}
