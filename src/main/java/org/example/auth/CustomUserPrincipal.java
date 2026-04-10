package org.example.auth;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

// 사용자 인증 정보를 담는 UserDetails 구현입니다.
// 주로 Security 컨텍스트에 현재 사용자의 정보를 제공하는 용도로 사용됩니다.
public class CustomUserPrincipal implements UserDetails {

    private final Long userId;
    private final String loginId;
    private final String username;
    private final String passwordHash;
    private final UserRole role;

    public CustomUserPrincipal(Long userId, String loginId, String username, String passwordHash, UserRole role) {
        this.userId = userId;
        this.loginId = loginId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public UserRole getRole() {
        return role;
    }

    public String getLoginId() {
        return loginId;
    }

    public String getDisplayName() {
        return username;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return loginId != null ? loginId : username;
    }
}
