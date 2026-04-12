package org.example.ai;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

// 1. 프론트엔드의 JSON 요청을 안전하게 받을 DTO를 레코드로 생성합니다.
record AiAnalyzeRequest(String code) {}

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @Operation(summary = "코드 AI 분석", description = "AI가 사용자의 코드를 분석 후, JSON 데이터를 반환합니다.")
    @PostMapping(value = "/analyze", produces = MediaType.APPLICATION_JSON_VALUE)
    // 2. String 단일 변수 대신 AiAnalyzeRequest DTO 객체로 받도록 수정합니다.
    public String analyze(@RequestBody AiAnalyzeRequest request) {
        return aiService.analyzeCode(request.code());
    }
}