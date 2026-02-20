package com.example.vite_coding_boot.application.port.in;

import java.util.List;
import java.util.Optional;

import com.example.vite_coding_boot.domain.model.Role;
import com.example.vite_coding_boot.domain.model.User;

public interface UserQueryUseCase {

    Optional<User> authenticate(String username, String password);

    Optional<User> findByUsername(String username);

    Optional<User> findById(Long id);

    List<User> findAllMembers();

    User createUser(String username, String password, String name, Role role);

    void clearOtpResetRequired(Long userId);
}
