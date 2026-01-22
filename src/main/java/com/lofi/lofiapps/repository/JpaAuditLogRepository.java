package com.lofi.lofiapps.repository;

import com.lofi.lofiapps.model.entity.JpaAuditLog;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface JpaAuditLogRepository
    extends JpaRepository<JpaAuditLog, UUID>, JpaSpecificationExecutor<JpaAuditLog> {}
