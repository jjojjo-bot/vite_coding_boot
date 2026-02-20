package com.example.vite_coding_boot.adapter.in.web;

import java.util.Map;

import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.vite_coding_boot.adapter.in.web.dto.OtpSetupResponse;
import com.example.vite_coding_boot.adapter.in.web.dto.OtpVerifyRequest;
import com.example.vite_coding_boot.adapter.in.web.security.JwtUtil;
import com.example.vite_coding_boot.application.port.in.OtpUseCase;
import com.example.vite_coding_boot.application.port.out.UserRepository;
import com.example.vite_coding_boot.domain.model.User;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/otp")
public class ApiOtpController {

    private final OtpUseCase otpUseCase;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public ApiOtpController(OtpUseCase otpUseCase, UserRepository userRepository, JwtUtil jwtUtil) {
        this.otpUseCase = otpUseCase;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/status")
    public ResponseEntity<?> status(HttpServletRequest request) {
        User user = getUser(request);
        User fresh = userRepository.findById(user.getId()).orElse(user);
        return ResponseEntity.ok(Map.of("otpEnabled", fresh.isOtpEnabled()));
    }

    @PostMapping("/setup")
    public ResponseEntity<?> setup(HttpServletRequest request, HttpServletResponse response) {
        User user = getUser(request);
        User fresh = userRepository.findById(user.getId()).orElse(user);
        String secret = otpUseCase.generateSecret();
        String qrCodeDataUri = otpUseCase.getQrCodeDataUri(secret, fresh.getUsername());

        // Store temp secret in encrypted cookie
        String otpSetupToken = jwtUtil.generateOtpSetupToken(secret);
        ResponseCookie cookie = ResponseCookie.from("otp-setup", otpSetupToken)
                .httpOnly(true).path("/").maxAge(600).sameSite("Lax").build();
        response.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.ok(new OtpSetupResponse(qrCodeDataUri));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody OtpVerifyRequest req, HttpServletRequest request,
                                    HttpServletResponse response) {
        User user = getUser(request);
        String otpSetupToken = extractCookie(request, "otp-setup");
        if (otpSetupToken == null || !jwtUtil.isValid(otpSetupToken)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "BAD_REQUEST", "message", "OTP 설정을 다시 시작해주세요."));
        }

        String tempSecret = jwtUtil.extractOtpSetupSecret(otpSetupToken);
        if (!otpUseCase.verifyCode(tempSecret, req.otpCode())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "BAD_REQUEST", "message", "OTP 코드가 올바르지 않습니다."));
        }

        User fresh = userRepository.findById(user.getId()).orElse(user);
        fresh.setOtpSecret(tempSecret);
        userRepository.save(fresh);

        // Clear otp-setup cookie
        ResponseCookie clearCookie = ResponseCookie.from("otp-setup", "")
                .httpOnly(true).path("/").maxAge(0).sameSite("Lax").build();
        response.addHeader("Set-Cookie", clearCookie.toString());

        return ResponseEntity.ok(Map.of("message", "OTP가 활성화되었습니다.", "otpEnabled", true));
    }

    @PostMapping("/disable")
    public ResponseEntity<?> disable(HttpServletRequest request) {
        User user = getUser(request);
        User fresh = userRepository.findById(user.getId()).orElse(user);
        fresh.setOtpSecret(null);
        userRepository.save(fresh);
        return ResponseEntity.ok(Map.of("message", "OTP가 해제되었습니다.", "otpEnabled", false));
    }

    private User getUser(HttpServletRequest request) {
        return (User) request.getAttribute("loginUser");
    }

    private String extractCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        for (Cookie c : request.getCookies()) {
            if (name.equals(c.getName())) return c.getValue();
        }
        return null;
    }
}
