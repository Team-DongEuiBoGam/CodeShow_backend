package org.example.animation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDate;
import org.example.auth.User;

// 애니메이션 기본 정보와 JSON 데이터를 animation_mst 한 테이블에 함께 저장한다.
@Entity
@Table(name = "animation_mst")
public class AnimationMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "animation_code")
    private Long id;

    @Column(name = "animation_name", nullable = false, length = 25)
    private String name;

    @Column(name = "original_code", nullable = false, columnDefinition = "TEXT")
    private String originalCode;

    @Column(name = "json_data", nullable = false, columnDefinition = "JSON")
    private String jsonData;

    @Column(name = "create_date", nullable = false)
    private LocalDate createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User creator;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "language_id", nullable = false)
    private Language language;

    protected AnimationMetadata() {
    }

    // 애니메이션 생성 시 회원, 언어, 원본 코드, JSON 데이터를 함께 묶어서 저장한다.
    public AnimationMetadata(String name, String originalCode, String jsonData, User creator, Language language) {
        this.name = name;
        this.originalCode = originalCode;
        this.jsonData = jsonData;
        this.creator = creator;
        this.language = language;
    }

    // 생성일은 저장 직전에 오늘 날짜로 자동 기록한다.
    @PrePersist
    void onCreate() {
        createdAt = LocalDate.now();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getOriginalCode() {
        return originalCode;
    }

    public String getJsonData() {
        return jsonData;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public User getCreator() {
        return creator;
    }

    public Language getLanguage() {
        return language;
    }

    public void updateName(String newName) {
        this.name = newName;
    }
}
