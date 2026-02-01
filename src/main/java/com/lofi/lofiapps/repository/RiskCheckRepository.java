package com.lofi.lofiapps.repository;

import com.lofi.lofiapps.dto.response.RiskItem;
import com.lofi.lofiapps.entity.RiskCheck;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RiskCheckRepository extends JpaRepository<RiskCheck, UUID> {

  List<RiskCheck> findByLoanId(UUID loanId);

  List<RiskCheck> findByLoanIdAndStatus(UUID loanId, RiskItem.RiskStatus status);

  Optional<RiskCheck> findByLoanIdAndCheckType(UUID loanId, String checkType);

  boolean existsByLoanIdAndCheckType(UUID loanId, String checkType);

  List<RiskCheck> findByLoanIdAndStatusIn(UUID loanId, List<RiskItem.RiskStatus> statuses);
}
