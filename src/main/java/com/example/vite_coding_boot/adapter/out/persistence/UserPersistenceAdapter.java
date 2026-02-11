package com.example.vite_coding_boot.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.example.vite_coding_boot.application.port.out.UserRepository;
import com.example.vite_coding_boot.domain.model.User;

@Component
public class UserPersistenceAdapter implements UserRepository {

    private final JpaUserRepository jpaUserRepository;

    public UserPersistenceAdapter(JpaUserRepository jpaUserRepository) {
        this.jpaUserRepository = jpaUserRepository;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaUserRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpaUserRepository.findById(id);
    }

    @Override
    public List<User> findAll() {
        return jpaUserRepository.findAll();
    }

    @Override
    public User save(User user) {
        return jpaUserRepository.save(user);
    }

    @Override
    public long count() {
        return jpaUserRepository.count();
    }
}
