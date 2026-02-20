package com.example.vite_coding_boot.application.port.in;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.example.vite_coding_boot.domain.model.Assignment;
import com.example.vite_coding_boot.domain.model.User;

public interface AssignmentUseCase {

    Assignment createAssignment(String title, String description, Long createdByUserId, LocalDate startDate, LocalDate dueDate);

    List<Assignment> findAllAssignments();

    Optional<Assignment> findById(Long id);

    void deleteAssignment(Long id, Long performerUserId);

    List<Assignment> findAssignmentsByUser(User user);

    Assignment approveAssignment(Long id, Long assigneeUserId, Long performerUserId);

    Assignment rejectAssignment(Long id, String rejectionReason, Long performerUserId);

    Assignment updateAssignment(Long id, String title, String description, LocalDate startDate, LocalDate dueDate);

    Assignment submitFinalResult(Long id, String finalResult, Long userId);

    List<Assignment> findAssignmentsByCreatorOrAssignee(User user);
}
