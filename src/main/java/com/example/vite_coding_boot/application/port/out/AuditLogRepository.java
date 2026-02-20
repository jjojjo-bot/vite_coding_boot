package com.example.vite_coding_boot.application.port.out;

import java.util.List;

import com.example.vite_coding_boot.domain.model.AuditLog;

public interface AuditLogRepository {

    AuditLog save(AuditLog auditLog);

    List<AuditLog> findAll();
}
