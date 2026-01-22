package com.lofi.lofiapps.repository;

import com.lofi.lofiapps.model.entity.JpaLoan;
import com.lofi.lofiapps.model.enums.LoanStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaLoanRepository
    extends JpaRepository<JpaLoan, UUID>, JpaSpecificationExecutor<JpaLoan> {
  List<JpaLoan> findByCustomerId(UUID customerId);

  List<JpaLoan> findByLoanStatus(LoanStatus loanStatus);

  long countByLoanStatus(LoanStatus loanStatus);
}
