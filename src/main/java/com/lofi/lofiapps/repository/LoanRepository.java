package com.lofi.lofiapps.repository;

import com.lofi.lofiapps.model.dto.request.LoanCriteria;
import com.lofi.lofiapps.model.entity.Loan;
import com.lofi.lofiapps.model.enums.LoanStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LoanRepository {
  Loan save(Loan loan);

  Optional<Loan> findById(UUID id);

  List<Loan> findByCustomerId(UUID customerId);

  List<Loan> findByStatus(LoanStatus status);

  Page<Loan> findAll(LoanCriteria criteria, Pageable pageable);

  long countByStatus(LoanStatus status);

  List<Loan> findAll();
}
