package com.readingtracker.server.security;

import java.io.IOException;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.readingtracker.server.common.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JWT 인증 필터
 * 모든 요청에서 JWT 토큰을 검증하고 인증 정보를 설정합니다
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, 
                                    @NonNull HttpServletResponse response, 
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // 1. Authorization 헤더에서 JWT 토큰 추출
            String token = extractTokenFromRequest(request);
            
            if (token != null) {
                // 2. 토큰에서 사용자 정보 추출
                String loginId = jwtUtil.extractLoginId(token);
                Long userId = jwtUtil.extractUserId(token);
                
                // 3. 토큰 유효성 검증
                if (loginId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    
                    // 토큰이 유효한지 확인
                    if (jwtUtil.validateToken(token, loginId)) {
                        
                        // 4. Refresh Token이 아닌 Access Token인지 확인
                        if (!jwtUtil.isRefreshToken(token)) {
                            
                            // 5. 권한(Role) 추출
                            String role = extractRole(token);
                            SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
                            
                            // 6. Spring Security 인증 객체 생성
                            UsernamePasswordAuthenticationToken authentication = 
                                new UsernamePasswordAuthenticationToken(
                                    loginId,  // Principal (주체)
                                    null,     // Credentials (비밀번호는 불필요)
                                    Collections.singletonList(authority)  // Authorities (권한)
                                );
                            
                            // 7. 요청 세부 정보 추가
                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            
                            // 8. SecurityContext에 인증 정보 저장
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 토큰 검증 실패 시 로그 출력 (인증 실패로 처리)
            logger.error("JWT 토큰 검증 실패: " + e.getMessage());
        }
        
        // 9. 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }
    
    /**
     * Authorization 헤더에서 JWT 토큰 추출
     * "Bearer {token}" 형식에서 토큰 부분만 추출
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);  // "Bearer " 이후 부분
        }

        // 브라우저의 WebSocket/SockJS 연결 시에는 Authorization 헤더를 전달하지 못하는 경우가 있음.
        // 따라서 쿼리 파라미터로 전달된 토큰도 허용하도록 한다. (예: /ws-sharedsync/info?token=...)

        String p = request.getParameter("token");
        if (p != null && !p.isBlank()) {
            p = p.trim();
            if (p.startsWith("Bearer ")) {
                p = p.substring(7).trim();
            }
            return p;
        }
        

        // 일부 구현에서는 짧은 타임스탬프나 다른 파라미터명(`t`)을 함께 보냄 — 확인용
        return null;
    }
    
    /**
     * JWT 토큰에서 Role 추출
     */
    private String extractRole(String token) {
        try {
            return jwtUtil.extractClaim(token, claims -> claims.get("role", String.class));
        } catch (Exception e) {
            return "USER";  // 기본값
        }
    }
}



