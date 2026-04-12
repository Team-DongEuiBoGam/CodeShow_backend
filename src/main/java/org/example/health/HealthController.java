package org.example.health;

import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "health-controller")
@RestController
@RequestMapping("/api")
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Operation(summary = "서버 연결 및 DB 상태 확인", description = "서버와 DB가 깨어있는지 주기적으로 확인합니다.")
    @GetMapping("/health")
    public Map<String, String> health() {
        jdbcTemplate.execute("SELECT 1");

        return Map.of("status", "ok", "db", "awake");
    }
}