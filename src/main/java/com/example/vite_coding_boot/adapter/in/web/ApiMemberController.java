package com.example.vite_coding_boot.adapter.in.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.vite_coding_boot.adapter.in.web.dto.UserResponse;
import com.example.vite_coding_boot.application.port.in.UserQueryUseCase;

@RestController
@RequestMapping("/api/members")
public class ApiMemberController {

    private final UserQueryUseCase userQueryUseCase;

    public ApiMemberController(UserQueryUseCase userQueryUseCase) {
        this.userQueryUseCase = userQueryUseCase;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> list() {
        return ResponseEntity.ok(
                userQueryUseCase.findAllMembers().stream().map(UserResponse::from).toList()
        );
    }
}
