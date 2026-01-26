package com.lofi.lofiapps.usecase.loan;

import com.lofi.lofiapps.dto.request.LoanRequest;
import com.lofi.lofiapps.dto.response.LoanResponse;
import com.lofi.lofiapps.entity.ApprovalHistory;
import com.lofi.lofiapps.entity.Loan;
import com.lofi.lofiapps.entity.Product;
import com.lofi.lofiapps.entity.User;
import com.lofi.lofiapps.enums.ApprovalStage;
import com.lofi.lofiapps.enums.LoanStatus;
import com.lofi.lofiapps.enums.UserStatus;
import com.lofi.lofiapps.mapper.LoanDtoMapper;
import com.lofi.lofiapps.repository.ApprovalHistoryRepository;
import com.lofi.lofiapps.repository.LoanRepository;
import com.lofi.lofiapps.repository.ProductRepository;
import com.lofi.lofiapps.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplyLoanUseCase {

    private final LoanRepository loanRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ApprovalHistoryRepository approvalHistoryRepository;
    private final LoanDtoMapper loanDtoMapper;

    @Transactional
    public LoanResponse execute(LoanRequest request, UUID userId, String username) {
        if (request.getLoanAmount() == null || request.getTenor() == null) {
            throw new IllegalArgumentException("Loan amount and tenor are required");
        }

        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalStateException("User is not active");
        }

        if (!Boolean.TRUE.equals(user.getProfileCompleted())) {
            throw new IllegalStateException(
                    "User profile is incomplete. Please complete your profile first.");
        }

        Product product = productRepository
                .findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        // Validation
        if (request.getLoanAmount().compareTo(product.getMinLoanAmount()) < 0) {
            throw new IllegalArgumentException(
                    "Loan amount is less than minimum: " + product.getMinLoanAmount());
        }
        if (request.getLoanAmount().compareTo(product.getMaxLoanAmount()) > 0) {
            throw new IllegalArgumentException(
                    "Loan amount exceeds maximum: " + product.getMaxLoanAmount());
        }
        if (request.getTenor() > product.getMaxTenor()) {
            throw new IllegalArgumentException("Tenor exceeds maximum: " + product.getMaxTenor());
        }

        // Create Draft Loan
        Loan loan = Loan.builder()
                .loanAmount(request.getLoanAmount())
                .tenor(request.getTenor())
                .loanStatus(LoanStatus.DRAFT)
                .currentStage(ApprovalStage.CUSTOMER)
                .customer(user)
                .product(product)
                .branch(user.getBranch())
                .submittedAt(null) // Not submitted yet
                .build();

        Loan savedLoan = loanRepository.save(loan);

        // Save history for DRAFT creation (Tracking)
        approvalHistoryRepository.save(
                ApprovalHistory.builder()
                        .loanId(savedLoan.getId())
                        .fromStatus(null)
                        .toStatus(LoanStatus.DRAFT)
                        .actionBy(username)
                        .notes("Loan Draft Created")
                        .build());

        return loanDtoMapper.toResponse(savedLoan);
    }
}
