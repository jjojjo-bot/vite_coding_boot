package com.example.vite_coding_boot.application.port.in;

public interface OtpUseCase {

    String generateSecret();

    String getQrCodeDataUri(String secret, String username);

    boolean verifyCode(String secret, String code);
}
