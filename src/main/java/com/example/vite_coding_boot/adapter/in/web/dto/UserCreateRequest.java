package com.example.vite_coding_boot.adapter.in.web.dto;

public record UserCreateRequest(String username, String password, String name, String role, Long teamId) {
}
