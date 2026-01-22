package com.lofi.lofiapps.mapper;

import com.lofi.lofiapps.model.entity.ApprovalHistory;
import com.lofi.lofiapps.model.entity.JpaApprovalHistory;
import org.springframework.stereotype.Component;

@Component
public class ApprovalHistoryMapper {

  public ApprovalHistory toDomainEntity(JpaApprovalHistory entity) {
    if (entity == null) return null;
    return ApprovalHistory.builder()
        .id(entity.getId())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .createdBy(entity.getCreatedBy())
        .lastModifiedBy(entity.getLastModifiedBy())
        .deletedAt(entity.getDeletedAt())
        .loanId(entity.getLoanId())
        .fromStatus(entity.getFromStatus())
        .toStatus(entity.getToStatus())
        .actionBy(entity.getActionBy())
        .notes(entity.getNotes())
        .build();
  }

  public JpaApprovalHistory toJpaEntity(ApprovalHistory domain) {
    if (domain == null) return null;
    return JpaApprovalHistory.builder()
        .id(domain.getId())
        .createdAt(domain.getCreatedAt())
        .updatedAt(domain.getUpdatedAt())
        .createdBy(domain.getCreatedBy())
        .lastModifiedBy(domain.getLastModifiedBy())
        .deletedAt(domain.getDeletedAt())
        .loanId(domain.getLoanId())
        .fromStatus(domain.getFromStatus())
        .toStatus(domain.getToStatus())
        .actionBy(domain.getActionBy())
        .notes(domain.getNotes())
        .build();
  }
}
