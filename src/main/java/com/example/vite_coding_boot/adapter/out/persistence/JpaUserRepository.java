package com.example.vite_coding_boot.adapter.out.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.vite_coding_boot.domain.model.User;

public interface JpaUserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
}
