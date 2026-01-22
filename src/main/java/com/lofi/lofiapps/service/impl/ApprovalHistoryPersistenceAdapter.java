package com.lofi.lofiapps.service.impl;

import com.lofi.lofiapps.mapper.ApprovalHistoryMapper;
import com.lofi.lofiapps.model.entity.ApprovalHistory;
import com.lofi.lofiapps.model.entity.JpaApprovalHistory;
import com.lofi.lofiapps.repository.ApprovalHistoryRepository;
import com.lofi.lofiapps.repository.JpaApprovalHistoryRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApprovalHistoryPersistenceAdapter implements ApprovalHistoryRepository {

  private final JpaApprovalHistoryRepository repository;
  private final ApprovalHistoryMapper mapper;

  @Override
  public ApprovalHistory save(ApprovalHistory approvalHistory) {
    JpaApprovalHistory jpaEntity = mapper.toJpaEntity(approvalHistory);
    JpaApprovalHistory savedEntity = repository.save(jpaEntity);
    return mapper.toDomainEntity(savedEntity);
  }

  @Override
  public List<ApprovalHistory> findByLoanId(UUID loanId) {
    return repository.findByLoanId(loanId).stream()
        .map(mapper::toDomainEntity)
        .collect(Collectors.toList());
  }
}
