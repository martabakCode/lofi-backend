package com.lofi.lofiapps.service.impl;

import com.lofi.lofiapps.mapper.LoanMapper;
import com.lofi.lofiapps.model.dto.request.LoanCriteria;
import com.lofi.lofiapps.model.entity.JpaLoan;
import com.lofi.lofiapps.model.entity.Loan;
import com.lofi.lofiapps.model.enums.LoanStatus;
import com.lofi.lofiapps.repository.JpaLoanRepository;
import com.lofi.lofiapps.repository.LoanRepository;
import com.lofi.lofiapps.repository.LoanSpecification;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public class LoanPersistenceAdapter implements LoanRepository {
  private final JpaLoanRepository jpaLoanRepository;
  private final LoanMapper loanMapper;

  @Override
  @org.springframework.transaction.annotation.Transactional
  public Loan save(Loan loan) {
    JpaLoan jpaLoan = loanMapper.toJpa(loan);
    JpaLoan saved = jpaLoanRepository.save(jpaLoan);
    return loanMapper.toDomain(saved);
  }

  @Override
  public Optional<Loan> findById(UUID id) {
    return jpaLoanRepository.findById(id).map(loanMapper::toDomain);
  }

  @Override
  public List<Loan> findByCustomerId(UUID customerId) {
    return jpaLoanRepository.findByCustomerId(customerId).stream()
        .map(loanMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public List<Loan> findByStatus(LoanStatus status) {
    return jpaLoanRepository.findByLoanStatus(status).stream()
        .map(loanMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public Page<Loan> findAll(LoanCriteria criteria, Pageable pageable) {
    return jpaLoanRepository
        .findAll(LoanSpecification.withCriteria(criteria), pageable)
        .map(loanMapper::toDomain);
  }

  @Override
  public long countByStatus(LoanStatus status) {
    return jpaLoanRepository.countByLoanStatus(status);
  }

  @Override
  public List<Loan> findAll() {
    return jpaLoanRepository.findAll().stream()
        .map(loanMapper::toDomain)
        .collect(Collectors.toList());
  }
}
