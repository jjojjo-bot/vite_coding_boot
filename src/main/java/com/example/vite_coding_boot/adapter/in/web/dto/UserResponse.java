package com.example.vite_coding_boot.adapter.in.web.dto;

import com.example.vite_coding_boot.domain.model.User;

public record UserResponse(
        Long id,
        String username,
        String name,
        String role,
        Long teamId,
        String teamFullName,
        boolean otpEnabled
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getRole().name(),
                user.getTeam() != null ? user.getTeam().getId() : null,
                user.getTeam() != null ? user.getTeam().getFullName() : null,
                user.isOtpEnabled()
        );
    }
}
