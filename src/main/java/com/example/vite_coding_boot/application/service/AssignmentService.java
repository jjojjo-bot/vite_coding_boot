package com.example.vite_coding_boot.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.vite_coding_boot.application.port.in.AssignmentUseCase;
import com.example.vite_coding_boot.application.port.out.AssignmentRepository;
import com.example.vite_coding_boot.application.port.out.UserRepository;
import com.example.vite_coding_boot.domain.model.ApprovalStatus;
import com.example.vite_coding_boot.domain.model.Assignment;
import com.example.vite_coding_boot.domain.model.User;

@Service
@Transactional
public class AssignmentService implements AssignmentUseCase {

    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;

    public AssignmentService(AssignmentRepository assignmentRepository, UserRepository userRepository) {
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Assignment createAssignment(String title, String description, Long createdByUserId, LocalDate startDate, LocalDate dueDate) {
        User createdBy = userRepository.findById(createdByUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + createdByUserId));
        Assignment assignment = new Assignment(title, description, createdBy, startDate, dueDate);
        return assignmentRepository.save(assignment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Assignment> findAllAssignments() {
        return assignmentRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Assignment> findById(Long id) {
        return assignmentRepository.findById(id);
    }

    @Override
    public void deleteAssignment(Long id) {
        assignmentRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Assignment> findAssignmentsByUser(User user) {
        return assignmentRepository.findByUser(user);
    }

    @Override
    public Assignment approveAssignment(Long id, Long assigneeUserId) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("과제를 찾을 수 없습니다: " + id));
        if (assignment.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new IllegalStateException("승인 대기 상태의 과제만 승인할 수 있습니다.");
        }
        User assignee = userRepository.findById(assigneeUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + assigneeUserId));
        assignment.setUser(assignee);
        assignment.setApprovalStatus(ApprovalStatus.APPROVED);
        assignment.setRejectionReason(null);
        return assignmentRepository.save(assignment);
    }

    @Override
    public Assignment rejectAssignment(Long id, String rejectionReason) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("과제를 찾을 수 없습니다: " + id));
        if (assignment.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new IllegalStateException("승인 대기 상태의 과제만 반려할 수 있습니다.");
        }
        assignment.setApprovalStatus(ApprovalStatus.REJECTED);
        assignment.setRejectionReason(rejectionReason);
        return assignmentRepository.save(assignment);
    }

    @Override
    public Assignment updateAssignment(Long id, String title, String description, LocalDate startDate, LocalDate dueDate) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("과제를 찾을 수 없습니다: " + id));
        assignment.setTitle(title);
        assignment.setDescription(description);
        assignment.setStartDate(startDate);
        assignment.setDueDate(dueDate);
        if (assignment.getApprovalStatus() == ApprovalStatus.APPROVED) {
            assignment.setApprovalStatus(ApprovalStatus.PENDING);
            assignment.setUser(null);
            assignment.setFinalResult(null);
            assignment.setResultRegisteredAt(null);
        }
        return assignmentRepository.save(assignment);
    }

    @Override
    public Assignment submitFinalResult(Long id, String finalResult, Long userId) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("과제를 찾을 수 없습니다: " + id));
        if (assignment.getApprovalStatus() != ApprovalStatus.APPROVED) {
            throw new IllegalStateException("승인된 과제만 최종결과를 입력할 수 있습니다.");
        }
        if (assignment.getUser() == null || !assignment.getUser().getId().equals(userId)) {
            throw new IllegalStateException("담당자만 최종결과를 입력할 수 있습니다.");
        }
        assignment.setFinalResult(finalResult);
        assignment.setResultRegisteredAt(LocalDateTime.now());
        return assignmentRepository.save(assignment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Assignment> findAssignmentsByCreatorOrAssignee(User user) {
        return assignmentRepository.findByCreatedByOrUser(user, user);
    }
}
