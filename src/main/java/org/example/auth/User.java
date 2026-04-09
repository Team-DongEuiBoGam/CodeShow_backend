package org.example.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;

// 회원 정보를 user_mst 테이블에 매핑하는 엔티티다.
@Entity
@Table(
        name = "user_mst",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_mst_login_id", columnNames = "login_id")
        }
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "login_id", nullable = false, unique = true, length = 25)
    private String loginId;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "user_name", nullable = false, length = 10)
    private String userName;

    @Column(name = "create_date", nullable = false)
    private LocalDate createDate;

    protected User() {
    }

    // 회원가입 시 아이디, 암호화된 비밀번호, 유저명을 저장한다.
    public User(String loginId, String password, String userName) {
        this.loginId = loginId;
        this.password = password;
        this.userName = userName;
        this.createDate = LocalDate.now();
    }

    public Long getId() {
        return id;
    }

    public String getLoginId() {
        return loginId;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return userName;
    }

    // 현재 구조에서는 DB에 role 컬럼을 두지 않고 회원은 기본 USER로 처리한다.
    public UserRole getRole() {
        return UserRole.USER;
    }

    public LocalDate getCreateDate() {
        return createDate;
    }
}
