package com.example.vite_coding_boot.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.example.vite_coding_boot.application.port.out.AssignmentRepository;
import com.example.vite_coding_boot.domain.model.Assignment;
import com.example.vite_coding_boot.domain.model.User;

@Component
public class AssignmentPersistenceAdapter implements AssignmentRepository {

    private final JpaAssignmentRepository jpaAssignmentRepository;

    public AssignmentPersistenceAdapter(JpaAssignmentRepository jpaAssignmentRepository) {
        this.jpaAssignmentRepository = jpaAssignmentRepository;
    }

    @Override
    public List<Assignment> findByUser(User user) {
        return jpaAssignmentRepository.findByUser(user);
    }

    @Override
    public List<Assignment> findAll() {
        return jpaAssignmentRepository.findAll();
    }

    @Override
    public Optional<Assignment> findById(Long id) {
        return jpaAssignmentRepository.findById(id);
    }

    @Override
    public Assignment save(Assignment assignment) {
        return jpaAssignmentRepository.save(assignment);
    }

    @Override
    public void deleteById(Long id) {
        jpaAssignmentRepository.deleteById(id);
    }

    @Override
    public List<Assignment> findByCreatedByOrUser(User createdBy, User user) {
        return jpaAssignmentRepository.findByCreatedByOrUser(createdBy, user);
    }
}
