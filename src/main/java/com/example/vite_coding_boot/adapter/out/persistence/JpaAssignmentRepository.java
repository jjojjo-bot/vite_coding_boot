package com.example.vite_coding_boot.adapter.out.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.vite_coding_boot.domain.model.Assignment;
import com.example.vite_coding_boot.domain.model.User;

public interface JpaAssignmentRepository extends JpaRepository<Assignment, Long> {

    List<Assignment> findByUser(User user);

    List<Assignment> findByCreatedByOrUser(User createdBy, User user);
}
