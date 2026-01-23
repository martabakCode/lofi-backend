package com.lofi.lofiapps.repository;

import com.lofi.lofiapps.entity.Loan;
import com.lofi.lofiapps.enums.LoanStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanRepository extends JpaRepository<Loan, UUID>, JpaSpecificationExecutor<Loan> {
  List<Loan> findByCustomerId(UUID customerId);

  List<Loan> findByLoanStatus(LoanStatus loanStatus);

  long countByLoanStatus(LoanStatus loanStatus);
}
