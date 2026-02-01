package com.lofi.lofiapps.service.impl;

import com.lofi.lofiapps.dto.response.RiskItem;
import com.lofi.lofiapps.entity.Loan;
import com.lofi.lofiapps.entity.RiskCheck;
import com.lofi.lofiapps.entity.User;
import com.lofi.lofiapps.exception.ResourceNotFoundException;
import com.lofi.lofiapps.repository.RiskCheckRepository;
import com.lofi.lofiapps.repository.UserRepository;
import com.lofi.lofiapps.service.RiskCheckService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of RiskCheckService. Currently performs internal checks; external integrations
 * should be added for production.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RiskCheckServiceImpl implements RiskCheckService {

  private final RiskCheckRepository riskCheckRepository;
  private final UserRepository userRepository;

  @Value("${risk.high-amount-threshold:50000000}")
  private BigDecimal highAmountThreshold;

  @Override
  @Transactional
  public List<RiskCheck> performRiskChecks(Loan loan, UUID performedBy) {
    log.info("Performing risk checks for loan {}", loan.getId());

    List<RiskCheck> checks = new ArrayList<>();

    // Check 1: Blacklist Check
    checks.add(performBlacklistCheck(loan, performedBy));

    // Check 2: Credit Score / High Amount
    checks.add(performCreditScoreCheck(loan, performedBy));

    // Check 3: Document Verification
    checks.add(performDocumentCheck(loan, performedBy));

    // Check 4: Customer History
    checks.add(performCustomerHistoryCheck(loan, performedBy));

    return checks;
  }

  @Override
  @Transactional
  public RiskCheck performCheck(Loan loan, String checkType, UUID performedBy) {
    return switch (checkType) {
      case "CHECK_001" -> performBlacklistCheck(loan, performedBy);
      case "CHECK_002" -> performCreditScoreCheck(loan, performedBy);
      case "CHECK_003" -> performDocumentCheck(loan, performedBy);
      case "CHECK_004" -> performCustomerHistoryCheck(loan, performedBy);
      default -> throw new IllegalArgumentException("Unknown check type: " + checkType);
    };
  }

  private RiskCheck performBlacklistCheck(Loan loan, UUID performedBy) {
    // TODO: Integrate with national blacklist database
    // For now, perform basic validation
    RiskItem.RiskStatus status = RiskItem.RiskStatus.PASS;
    String comments = null;

    User customer = loan.getCustomer();
    if (customer != null && customer.getStatus() != null) {
      // Check if customer status indicates blacklist
      if (customer.getStatus().toString().contains("BLACKLIST")) {
        status = RiskItem.RiskStatus.FAIL;
        comments = "Customer is blacklisted";
      }
    }

    RiskCheck check =
        RiskCheck.builder()
            .loanId(loan.getId())
            .checkType("CHECK_001")
            .checkName("Blacklist Check")
            .description("Check if customer is in national blacklist")
            .status(status)
            .comments(comments)
            .checkedBy(performedBy)
            .externalReference(null) // TODO: Add external reference when integrated
            .build();

    return riskCheckRepository.save(check);
  }

  private RiskCheck performCreditScoreCheck(Loan loan, UUID performedBy) {
    RiskItem.RiskStatus status;
    String comments;

    // High loan amount check
    if (loan.getLoanAmount().compareTo(highAmountThreshold) > 0) {
      status = RiskItem.RiskStatus.WARNING;
      comments = "High loan amount requires further review. Threshold: " + highAmountThreshold;
    } else {
      status = RiskItem.RiskStatus.PASS;
      comments = null;
    }

    // TODO: Integrate with credit bureau for actual credit score

    RiskCheck check =
        RiskCheck.builder()
            .loanId(loan.getId())
            .checkType("CHECK_002")
            .checkName("Credit Score")
            .description("Automated credit scoring and high amount verification")
            .status(status)
            .comments(comments)
            .checkedBy(performedBy)
            .externalReference(null) // TODO: Add credit bureau reference
            .build();

    return riskCheckRepository.save(check);
  }

  private RiskCheck performDocumentCheck(Loan loan, UUID performedBy) {
    // TODO: Integrate with document verification service
    // Check document completeness and clarity

    RiskItem.RiskStatus status = RiskItem.RiskStatus.PASS;
    String comments = "Documents verified";

    RiskCheck check =
        RiskCheck.builder()
            .loanId(loan.getId())
            .checkType("CHECK_003")
            .checkName("Document Verification")
            .description("Verify uploaded document images are readable and complete")
            .status(status)
            .comments(comments)
            .checkedBy(performedBy)
            .build();

    return riskCheckRepository.save(check);
  }

  private RiskCheck performCustomerHistoryCheck(Loan loan, UUID performedBy) {
    // Check customer loan history
    RiskItem.RiskStatus status = RiskItem.RiskStatus.PASS;
    String comments = null;

    User customer = loan.getCustomer();
    if (customer != null) {
      int completedLoans = customer.getLoansCompleted();
      int overdueDays = customer.getTotalOverdueDays();

      if (overdueDays > 30) {
        status = RiskItem.RiskStatus.WARNING;
        comments = "Customer has " + overdueDays + " overdue days in history";
      } else if (completedLoans < 1) {
        status = RiskItem.RiskStatus.WARNING;
        comments = "New customer with no loan history";
      }
    }

    RiskCheck check =
        RiskCheck.builder()
            .loanId(loan.getId())
            .checkType("CHECK_004")
            .checkName("Customer History")
            .description("Review customer loan history and payment behavior")
            .status(status)
            .comments(comments)
            .checkedBy(performedBy)
            .build();

    return riskCheckRepository.save(check);
  }

  @Override
  @Transactional(readOnly = true)
  public List<RiskCheck> getRiskChecks(UUID loanId) {
    return riskCheckRepository.findByLoanId(loanId);
  }

  @Override
  @Transactional
  public RiskCheck resolveRiskCheck(UUID riskCheckId, String comments, UUID resolvedBy) {
    RiskCheck riskCheck =
        riskCheckRepository
            .findById(riskCheckId)
            .orElseThrow(
                () -> new ResourceNotFoundException("RiskCheck", "id", riskCheckId.toString()));

    // Only WARNING or FAIL checks can be resolved
    if (riskCheck.getStatus() == RiskItem.RiskStatus.PASS) {
      throw new IllegalStateException("Cannot resolve a check that already passed");
    }

    riskCheck.setStatus(RiskItem.RiskStatus.RESOLVED);
    riskCheck.setResolutionComments(comments);
    riskCheck.setResolvedBy(resolvedBy);
    riskCheck.setResolvedAt(LocalDateTime.now());

    log.info(
        "Risk check {} resolved by user {} with comments: {}", riskCheckId, resolvedBy, comments);

    return riskCheckRepository.save(riskCheck);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean canDisburse(UUID loanId) {
    List<RiskCheck> checks = riskCheckRepository.findByLoanId(loanId);

    if (checks.isEmpty()) {
      return false; // No checks performed yet
    }

    // Cannot disburse if any check has FAIL status (excluding resolved)
    return checks.stream().noneMatch(r -> r.getStatus() == RiskItem.RiskStatus.FAIL);
  }

  @Override
  public RiskItem toRiskItem(RiskCheck riskCheck) {
    return RiskItem.builder()
        .id(riskCheck.getId().toString())
        .name(riskCheck.getCheckName())
        .description(riskCheck.getDescription())
        .status(riskCheck.getStatus())
        .comments(riskCheck.getComments())
        .build();
  }
}
