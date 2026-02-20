package com.example.vite_coding_boot.adapter.in.web;

import java.util.Map;

import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.vite_coding_boot.adapter.in.web.dto.LoginRequest;
import com.example.vite_coding_boot.adapter.in.web.dto.OtpVerifyRequest;
import com.example.vite_coding_boot.adapter.in.web.dto.UserResponse;
import com.example.vite_coding_boot.adapter.in.web.security.JwtUtil;
import com.example.vite_coding_boot.application.port.in.OtpUseCase;
import com.example.vite_coding_boot.application.port.in.UserQueryUseCase;
import com.example.vite_coding_boot.domain.model.User;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/auth")
public class ApiAuthController {

    private final UserQueryUseCase userQueryUseCase;
    private final OtpUseCase otpUseCase;
    private final JwtUtil jwtUtil;

    public ApiAuthController(UserQueryUseCase userQueryUseCase, OtpUseCase otpUseCase, JwtUtil jwtUtil) {
        this.userQueryUseCase = userQueryUseCase;
        this.otpUseCase = otpUseCase;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req, HttpServletResponse response) {
        var userOpt = userQueryUseCase.authenticate(req.username(), req.password());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "UNAUTHORIZED", "message", "아이디 또는 비밀번호가 올바르지 않습니다."));
        }

        User user = userOpt.get();

        if (user.isOtpEnabled()) {
            if (user.isOtpResetRequired()) {
                // OTP reset required -> send QR code + otp-temp cookie
                String qrCodeDataUri = otpUseCase.getQrCodeDataUri(user.getOtpSecret(), user.getUsername());
                String otpTempToken = jwtUtil.generateOtpTempToken(user.getId());
                addCookie(response, "otp-temp", otpTempToken, 600); // 10 min
                return ResponseEntity.ok(Map.of(
                        "otpRequired", true,
                        "otpResetRequired", true,
                        "qrCodeDataUri", qrCodeDataUri
                ));
            }

            if (req.otpCode() == null || req.otpCode().isBlank()) {
                return ResponseEntity.ok(Map.of("otpRequired", true));
            }

            if (!otpUseCase.verifyCode(user.getOtpSecret(), req.otpCode())) {
                return ResponseEntity.status(401)
                        .body(Map.of("error", "UNAUTHORIZED", "message", "OTP 코드가 올바르지 않습니다."));
            }
        }

        String token = jwtUtil.generateToken(user);
        addCookie(response, "jwt", token, 8 * 60 * 60);
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @PostMapping("/otp-setup-verify")
    public ResponseEntity<?> otpSetupVerify(@RequestBody OtpVerifyRequest req, HttpServletRequest request,
                                            HttpServletResponse response) {
        String otpTempToken = extractCookie(request, "otp-temp");
        if (otpTempToken == null || !jwtUtil.isValid(otpTempToken)) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "UNAUTHORIZED", "message", "OTP 설정 세션이 만료되었습니다."));
        }

        Long userId = jwtUtil.extractUserId(otpTempToken);
        User user = userQueryUseCase.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "UNAUTHORIZED", "message", "사용자를 찾을 수 없습니다."));
        }

        if (!otpUseCase.verifyCode(user.getOtpSecret(), req.otpCode())) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "UNAUTHORIZED", "message", "OTP 코드가 올바르지 않습니다."));
        }

        userQueryUseCase.clearOtpResetRequired(userId);
        User updatedUser = userQueryUseCase.findById(userId).orElse(user);

        // Clear otp-temp cookie, set jwt cookie
        deleteCookie(response, "otp-temp");
        String token = jwtUtil.generateToken(updatedUser);
        addCookie(response, "jwt", token, 8 * 60 * 60);
        return ResponseEntity.ok(UserResponse.from(updatedUser));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        deleteCookie(response, "jwt");
        deleteCookie(response, "otp-temp");
        deleteCookie(response, "otp-setup");
        return ResponseEntity.ok(Map.of("message", "로그아웃 되었습니다."));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest request) {
        User user = (User) request.getAttribute("loginUser");
        if (user == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "UNAUTHORIZED", "message", "인증이 필요합니다."));
        }
        return ResponseEntity.ok(UserResponse.from(user));
    }

    private void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .path("/")
                .maxAge(maxAge)
                .sameSite("Lax")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    private void deleteCookie(HttpServletResponse response, String name) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    private String extractCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        for (Cookie c : request.getCookies()) {
            if (name.equals(c.getName())) return c.getValue();
        }
        return null;
    }
}
