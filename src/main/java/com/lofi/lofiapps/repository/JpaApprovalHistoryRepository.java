package com.lofi.lofiapps.repository;

import com.lofi.lofiapps.model.entity.JpaApprovalHistory;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaApprovalHistoryRepository extends JpaRepository<JpaApprovalHistory, UUID> {
  List<JpaApprovalHistory> findByLoanId(UUID loanId);
}
