package org.example.auth;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(7);

        try {
            // 토큰에서 Claims를 파싱하여 필요한 정보를 안전하게 추출한다.
            Claims claims = jwtTokenProvider.parseClaims(token);
            Object userIdObj = claims.get("userId");
            Long userId = null;
            if (userIdObj instanceof Number) {
                userId = ((Number) userIdObj).longValue();
            } else if (userIdObj instanceof String) {
                try {
                    userId = Long.valueOf((String) userIdObj);
                } catch (NumberFormatException ignored) {
                }
            }
            String loginId = claims.get("loginId", String.class);
            String username = claims.get("username", String.class);
            String roleStr = claims.get("role", String.class);
            UserRole role = roleStr != null ? UserRole.valueOf(roleStr) : UserRole.GUEST;

            // CustomUserPrincipal을 만들어 인증 컨텍스트에 설정한다.
            CustomUserPrincipal userDetails = new CustomUserPrincipal(
                    userId,
                    loginId,
                    username,
                    "",
                    role
            );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception ex) {
            // 검증 실패 시 인증 컨텍스트를 초기화
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
