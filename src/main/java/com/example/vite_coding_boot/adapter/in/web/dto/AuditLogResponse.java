package com.example.vite_coding_boot.adapter.in.web.dto;

import java.time.LocalDateTime;

import com.example.vite_coding_boot.domain.model.AuditLog;

public record AuditLogResponse(
        Long id,
        String action,
        String targetType,
        Long targetId,
        String details,
        String performedByName,
        LocalDateTime createdAt
) {
    public static AuditLogResponse from(AuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getAction().name(),
                log.getTargetType().name(),
                log.getTargetId(),
                log.getDetails(),
                log.getPerformedBy() != null ? log.getPerformedBy().getName() : "-",
                log.getCreatedAt()
        );
    }
}
