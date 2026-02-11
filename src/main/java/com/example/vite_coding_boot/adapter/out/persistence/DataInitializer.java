package com.example.vite_coding_boot.adapter.out.persistence;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.vite_coding_boot.application.port.out.UserRepository;
import com.example.vite_coding_boot.domain.model.Role;
import com.example.vite_coding_boot.domain.model.User;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    public DataInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            userRepository.save(new User("leader", "1234", "조장", Role.LEADER));
            userRepository.save(new User("member1", "1234", "조원1", Role.MEMBER));
            userRepository.save(new User("member2", "1234", "조원2", Role.MEMBER));
        }
    }
}
