package com.lofi.lofiapps.service.impl.usecase.report;

import com.lofi.lofiapps.dto.response.SlaReportResponse;
import com.lofi.lofiapps.entity.ApprovalHistory;
import com.lofi.lofiapps.entity.Loan;
import com.lofi.lofiapps.exception.ResourceNotFoundException;
import com.lofi.lofiapps.repository.ApprovalHistoryRepository;
import com.lofi.lofiapps.repository.LoanRepository;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetSlaReportUseCase {
  private final LoanRepository loanRepository;
  private final ApprovalHistoryRepository approvalHistoryRepository;

  public SlaReportResponse execute(UUID loanId) {
    Loan loan =
        loanRepository
            .findById(loanId)
            .orElseThrow(() -> new ResourceNotFoundException("Loan", "id", loanId.toString()));

    List<ApprovalHistory> history = approvalHistoryRepository.findByLoanId(loanId);
    List<SlaReportResponse.StageSlaInfo> stages = new ArrayList<>();

    ApprovalHistory previous = null;
    long totalMinutes = 0;

    for (ApprovalHistory h : history) {
      long duration = 0;
      if (previous != null && previous.getCreatedAt() != null && h.getCreatedAt() != null) {
        duration = Duration.between(previous.getCreatedAt(), h.getCreatedAt()).toMinutes();
      } else if (loan.getCreatedAt() != null && h.getCreatedAt() != null) {
        // From application creation to first status change (e.g., submit)
        duration = Duration.between(loan.getCreatedAt(), h.getCreatedAt()).toMinutes();
      }

      stages.add(
          SlaReportResponse.StageSlaInfo.builder()
              .stage(h.getToStatus().name())
              .status(h.getToStatus().name())
              .actionBy(h.getActionBy())
              .durationMinutes(duration)
              .build());

      totalMinutes += duration;
      previous = h;
    }

    return SlaReportResponse.builder()
        .loanId(loan.getId())
        .customerName(
            loan.getCustomer() != null ? loan.getCustomer().getFullName() : "Unknown Customer")
        .stages(stages)
        .totalDurationMinutes(totalMinutes)
        .build();
  }
}
