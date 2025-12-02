package com.readingtracker.server.service;

import com.readingtracker.server.common.constant.ErrorCode;
import com.readingtracker.dbms.entity.PasswordResetToken;
import com.readingtracker.dbms.entity.User;
import com.readingtracker.dbms.repository.PasswordResetTokenRepository;
import com.readingtracker.dbms.repository.UserRepository;
import com.readingtracker.server.common.util.PasswordValidator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private PasswordValidator passwordValidator;
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;
    
    /**
     * 회원가입 - Controller에서 호출
     * @param user 사용자 Entity (Mapper를 통해 DTO에서 변환됨)
     * @param password 평문 비밀번호 (암호화 전)
     * @return 생성된 사용자 Entity
     */
    public User register(User user, String password) {
        return executeRegister(user, password);
    }
    
    /**
     * 회원가입 실행
     * @param user 사용자 Entity
     * @param password 평문 비밀번호
     * @return 생성된 사용자 엔티티
     */
    private User executeRegister(User user, String password) {
        // 1. 중복 확인
        if (userRepository.existsByLoginId(user.getLoginId())) {
            throw new IllegalArgumentException(ErrorCode.DUPLICATE_LOGIN_ID.getMessage());
        }
        
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException(ErrorCode.DUPLICATE_EMAIL.getMessage());
        }
        
        // 2. 비밀번호 검증
        passwordValidator.validate(password);
        
        // 3. 비밀번호 암호화 및 설정
        String encodedPassword = passwordEncoder.encode(password);
        user.setPasswordHash(encodedPassword);
        
        // 4. 저장
        return userRepository.save(user);
    }
    
    /**
     * 로그인 - Controller에서 호출
     * @param loginId 로그인 ID
     * @param password 평문 비밀번호
     * @return 로그인 결과 (토큰 정보 및 사용자 Entity 포함)
     */
    public LoginResult login(String loginId, String password) {
        return executeLogin(loginId, password);
    }
    
    /**
     * 로그인 실행
     * @param loginId 로그인 ID
     * @param password 평문 비밀번호
     * @return 로그인 결과 (토큰 정보 및 사용자 Entity 포함)
     */
    private LoginResult executeLogin(String loginId, String password) {
        // 1. 사용자 조회 (loginId만 허용)
        User user = userRepository.findByLoginId(loginId)
            .orElseThrow(() -> new IllegalArgumentException(ErrorCode.USER_NOT_FOUND.getMessage()));
        
        // 2. 계정 상태 확인
        if (user.getStatus() == User.Status.LOCKED) {
            throw new IllegalArgumentException(ErrorCode.ACCOUNT_LOCKED.getMessage());
        }
        
        if (user.getStatus() == User.Status.DELETED) {
            throw new IllegalArgumentException(ErrorCode.ACCOUNT_DELETED.getMessage());
        }
        
        // 3. 비밀번호 확인
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            // 로그인 실패 횟수 증가
            user.setFailedLoginCount(user.getFailedLoginCount() + 1);
            
            // 5회 실패 시 계정 잠금
            if (user.getFailedLoginCount() >= 5) {
                user.setStatus(User.Status.LOCKED);
            }
            
            userRepository.save(user);
            throw new IllegalArgumentException(ErrorCode.INVALID_PASSWORD.getMessage());
        }
        
        // 4. 로그인 성공 처리
        user.setFailedLoginCount(0); // 실패 횟수 초기화
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        
        // 5. 토큰 생성 (디바이스 정보는 자동 생성)
        JwtService.TokenResult tokenResult = jwtService.generateTokens(
            user,
            null,  // deviceId - 자동 생성
            null,  // deviceName - "Unknown Device"
            null   // platform - "WEB"
        );
        
        return new LoginResult(tokenResult.getAccessToken(), tokenResult.getRefreshToken(), user);
    }
    
    /**
     * 아이디 찾기 - Controller에서 호출
     * @param email 이메일
     * @param name 이름
     * @return 사용자 Entity
     */
    public User findLoginIdByEmailAndName(String email, String name) {
        return executeFindLoginId(email, name);
    }
    
    /**
     * 아이디 찾기 실행
     * @param email 이메일
     * @param name 이름
     * @return 사용자 엔티티
     */
    private User executeFindLoginId(String email, String name) {
        // 이메일 + 이름으로 활성 사용자 조회 (둘 다 일치해야 함)
        User user = userRepository.findActiveUserByEmailAndName(email, name)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정입니다."));
        
        return user;
    }
    
    /**
     * Step 1: 계정 확인 및 재설정 토큰 발급 - Controller에서 호출
     * @param loginId 로그인 ID
     * @param email 이메일
     * @return 재설정 토큰
     */
    public String verifyAccountForPasswordReset(String loginId, String email) {
        return executeVerifyAccount(loginId, email);
    }
    
    /**
     * 계정 확인 실행
     * @param loginId 로그인 ID
     * @param email 이메일
     * @return 재설정 토큰
     */
    private String executeVerifyAccount(String loginId, String email) {
        // 1. loginId + email로 활성 사용자 조회 (둘 다 일치해야 함)
        User user = userRepository.findActiveUserByLoginIdAndEmail(loginId, email)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정입니다."));
        
        // 2. 기존 토큰 삭제 (같은 사용자의 이전 재설정 토큰)
        passwordResetTokenRepository.deleteAllByUserId(user.getId());
        
        // 3. 새 토큰 생성 (UUID 기반 랜덤 토큰)
        String resetToken = UUID.randomUUID().toString();
        
        // 4. 토큰 만료 시간 설정 (5분)
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);
        
        // 5. 토큰 저장
        PasswordResetToken tokenEntity = new PasswordResetToken(user.getId(), resetToken, expiresAt);
        passwordResetTokenRepository.save(tokenEntity);
        
        return resetToken;
    }
    
    /**
     * Step 2: 비밀번호 변경 - Controller에서 호출
     * @param resetToken 재설정 토큰
     * @param newPassword 새 비밀번호
     * @param confirmPassword 확인 비밀번호
     * @return 변경된 사용자 Entity
     */
    public User resetPassword(String resetToken, String newPassword, String confirmPassword) {
        return executeResetPassword(resetToken, newPassword, confirmPassword);
    }
    
    /**
     * 비밀번호 변경 실행
     * @param resetToken 재설정 토큰
     * @param newPassword 새 비밀번호
     * @param confirmPassword 확인 비밀번호
     * @return 변경된 사용자 Entity
     */
    private User executeResetPassword(String resetToken, String newPassword, String confirmPassword) {
        // 1. 토큰 검증
        PasswordResetToken tokenEntity = passwordResetTokenRepository
            .findValidToken(resetToken, LocalDateTime.now())
            .orElseThrow(() -> new IllegalArgumentException("유효하지 않거나 만료된 토큰입니다."));
        
        // 2. 토큰으로 사용자 조회
        Long userId = tokenEntity.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("토큰에 사용자 ID가 없습니다.");
        }
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // 3. 사용자 상태 확인 (ACTIVE만 허용)
        if (!"ACTIVE".equals(user.getStatus().name())) {
            throw new IllegalArgumentException("비활성화된 계정입니다.");
        }
        
        // 4. 새 비밀번호와 확인 비밀번호 일치 검증
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
        }
        
        // 5. 새 비밀번호 강도 검증
        passwordValidator.validate(newPassword);
        
        // 6. 기존 비밀번호와 동일한지 확인
        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("기존 비밀번호와 동일합니다.");
        }
        
        // 7. 새 비밀번호 암호화 및 저장
        String newPasswordHash = passwordEncoder.encode(newPassword);
        user.setPasswordHash(newPasswordHash);
        
        // 8. 토큰 사용 처리 (재사용 방지)
        tokenEntity.setUsed(true);
        passwordResetTokenRepository.save(tokenEntity);
        
        // 9. 사용자 저장
        return userRepository.save(user);
    }
    
    /**
     * Refresh Token으로 Access Token 갱신 (Token Rotation)
     * @param refreshToken 기존 Refresh Token
     * @return 새로운 Access Token 및 Refresh Token
     */
    public JwtService.TokenResult refreshAccessToken(String refreshToken) {
        return jwtService.refreshTokens(refreshToken);
    }
    
    /**
     * 로그인 결과 클래스
     */
    public static class LoginResult {
        private String accessToken;
        private String refreshToken;
        private User user;
        
        public LoginResult(String accessToken, String refreshToken, User user) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.user = user;
        }
        
        public String getAccessToken() { return accessToken; }
        public String getRefreshToken() { return refreshToken; }
        public User getUser() { return user; }
    }

}

