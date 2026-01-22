package com.lofi.lofiapps.repository;

import com.lofi.lofiapps.model.dto.request.UserCriteria;
import com.lofi.lofiapps.model.entity.JpaUser;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {
  public static Specification<JpaUser> withCriteria(UserCriteria criteria) {
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (criteria.getStatus() != null) {
        predicates.add(criteriaBuilder.equal(root.get("status"), criteria.getStatus()));
      }

      if (criteria.getRoleName() != null) {
        predicates.add(
            criteriaBuilder.equal(root.join("roles").get("roleName"), criteria.getRoleName()));
      }

      if (criteria.getBranchId() != null) {
        predicates.add(criteriaBuilder.equal(root.get("branch").get("id"), criteria.getBranchId()));
      }

      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }
}
