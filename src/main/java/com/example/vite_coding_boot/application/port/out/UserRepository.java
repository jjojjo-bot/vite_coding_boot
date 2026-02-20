package com.example.vite_coding_boot.application.port.out;

import java.util.List;
import java.util.Optional;

import com.example.vite_coding_boot.domain.model.User;

public interface UserRepository {

    Optional<User> findByUsername(String username);

    Optional<User> findById(Long id);

    List<User> findAll();

    User save(User user);

    long count();

    void deleteById(Long id);

    List<User> findByTeamId(Long teamId);
}
