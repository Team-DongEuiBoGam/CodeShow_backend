package org.example.auth;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class CustomUserPrincipal implements UserDetails {

    private final Integer userId;
    private final String loginId;
    private final String username;
    private final String passwordHash;
    private final UserRole role;

    public CustomUserPrincipal(Integer userId, String loginId, String username, String passwordHash, UserRole role) {
        this.userId = userId;
        this.loginId = loginId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public Integer getUserId() {
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
