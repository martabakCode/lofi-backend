package com.lofi.lofiapps.repository;

import com.lofi.lofiapps.entity.Loan;
import com.lofi.lofiapps.enums.LoanStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanRepository extends JpaRepository<Loan, UUID>, JpaSpecificationExecutor<Loan> {
  List<Loan> findByCustomerId(UUID customerId);

  List<Loan> findByLoanStatus(LoanStatus loanStatus);

  long countByLoanStatus(LoanStatus loanStatus);

  // Get all approved/disbursed loans for a customer (active loans)
  List<Loan> findByCustomerIdAndLoanStatusIn(UUID customerId, List<LoanStatus> statuses);

  // Sum of approved loan amounts for active loans
  @Query(
      "SELECT COALESCE(SUM(l.loanAmount), 0) FROM Loan l WHERE l.customer.id = :customerId AND l.loanStatus IN :statuses")
  BigDecimal sumLoanAmountByCustomerIdAndStatusIn(
      @Param("customerId") UUID customerId, @Param("statuses") List<LoanStatus> statuses);
}
