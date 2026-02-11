package com.example.vite_coding_boot.application.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.vite_coding_boot.application.port.in.UserQueryUseCase;
import com.example.vite_coding_boot.application.port.out.UserRepository;
import com.example.vite_coding_boot.domain.model.Role;
import com.example.vite_coding_boot.domain.model.User;

@Service
public class UserQueryService implements UserQueryUseCase {

    private final UserRepository userRepository;

    public UserQueryService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> authenticate(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(user -> user.getPassword().equals(password));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public List<User> findAllMembers() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.MEMBER)
                .toList();
    }
}
