package org.example.ai;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @Operation(summary = "코드 AI 분석", description = "AI가 사용자의 코드를 분석 후, JSON 데이터를 반환합니다.")
    @PostMapping("/analyze")
    public String analyze(@RequestBody String code) {
        return aiService.analyzeCode(code);
    }
}