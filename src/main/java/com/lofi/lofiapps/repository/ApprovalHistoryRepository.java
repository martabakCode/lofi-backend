package com.lofi.lofiapps.repository;

import com.lofi.lofiapps.model.entity.ApprovalHistory;
import java.util.List;
import java.util.UUID;

public interface ApprovalHistoryRepository {
  ApprovalHistory save(ApprovalHistory approvalHistory);

  List<ApprovalHistory> findByLoanId(UUID loanId);
}
