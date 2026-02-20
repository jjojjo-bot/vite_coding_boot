package com.example.vite_coding_boot.adapter.out.persistence;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.vite_coding_boot.application.port.out.AuditLogRepository;
import com.example.vite_coding_boot.domain.model.AuditLog;

@Component
public class AuditLogPersistenceAdapter implements AuditLogRepository {

    private final JpaAuditLogRepository jpaAuditLogRepository;

    public AuditLogPersistenceAdapter(JpaAuditLogRepository jpaAuditLogRepository) {
        this.jpaAuditLogRepository = jpaAuditLogRepository;
    }

    @Override
    public AuditLog save(AuditLog auditLog) {
        return jpaAuditLogRepository.save(auditLog);
    }

    @Override
    public List<AuditLog> findAll() {
        return jpaAuditLogRepository.findAllByOrderByCreatedAtDesc();
    }
}
