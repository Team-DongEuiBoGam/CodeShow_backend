package org.example.ai;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/analyze")
    public String analyze(@RequestBody String code) {
        return aiService.analyzeCode(code);
    }
}