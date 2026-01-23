package com.lofi.lofiapps.repository;

import com.lofi.lofiapps.dto.request.LoanCriteria;
import com.lofi.lofiapps.entity.Loan;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class LoanSpecification {
  public static Specification<Loan> withCriteria(LoanCriteria criteria) {
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (criteria.getStatus() != null) {
        predicates.add(criteriaBuilder.equal(root.get("loanStatus"), criteria.getStatus()));
      }

      if (criteria.getCustomerId() != null) {
        predicates.add(
            criteriaBuilder.equal(root.get("customer").get("id"), criteria.getCustomerId()));
      }

      if (criteria.getBranchId() != null) {
        predicates.add(criteriaBuilder.equal(root.get("branch").get("id"), criteria.getBranchId()));
      }

      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }
}
