package com.example.vite_coding_boot.application.port.out;

import java.util.List;
import java.util.Optional;

import com.example.vite_coding_boot.domain.model.Assignment;
import com.example.vite_coding_boot.domain.model.User;

public interface AssignmentRepository {

    List<Assignment> findByUser(User user);

    List<Assignment> findAll();

    Optional<Assignment> findById(Long id);

    Assignment save(Assignment assignment);

    void deleteById(Long id);

    List<Assignment> findByCreatedByOrUser(User createdBy, User user);
}
