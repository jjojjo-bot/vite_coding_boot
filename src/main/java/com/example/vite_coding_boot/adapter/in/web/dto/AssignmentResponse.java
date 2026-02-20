package com.example.vite_coding_boot.adapter.in.web.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.vite_coding_boot.domain.model.Assignment;

public record AssignmentResponse(
        Long id,
        String title,
        String description,
        Long createdById,
        String createdByName,
        Long assigneeId,
        String assigneeName,
        LocalDate startDate,
        LocalDate dueDate,
        String approvalStatus,
        String rejectionReason,
        String progressStatus,
        boolean hasFinalResult,
        String finalResult,
        LocalDateTime resultRegisteredAt
) {
    public static AssignmentResponse from(Assignment a) {
        return new AssignmentResponse(
                a.getId(),
                a.getTitle(),
                a.getDescription(),
                a.getCreatedBy() != null ? a.getCreatedBy().getId() : null,
                a.getCreatedBy() != null ? a.getCreatedBy().getName() : "-",
                a.getUser() != null ? a.getUser().getId() : null,
                a.getUser() != null ? a.getUser().getName() : "미할당",
                a.getStartDate(),
                a.getDueDate(),
                a.getApprovalStatus().name(),
                a.getRejectionReason(),
                a.calculateProgressStatus() != null ? a.calculateProgressStatus().name() : null,
                a.getFinalResult() != null,
                a.getFinalResult(),
                a.getResultRegisteredAt()
        );
    }
}
