package com.lofi.lofiapps.service.impl.usecase.user;

import com.lofi.lofiapps.dto.request.UserCriteria;
import com.lofi.lofiapps.dto.response.PagedResponse;
import com.lofi.lofiapps.dto.response.UserSummaryResponse;
import com.lofi.lofiapps.entity.Role;
import com.lofi.lofiapps.entity.User;
import com.lofi.lofiapps.repository.UserRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetUsersUseCase {

  private final UserRepository userRepository;

  public PagedResponse<UserSummaryResponse> execute(UserCriteria criteria, Pageable pageable) {
    Specification<User> spec =
        (root, query, cb) -> {
          List<Predicate> predicates = new ArrayList<>();
          if (criteria.getStatus() != null) {
            predicates.add(cb.equal(root.get("status"), criteria.getStatus()));
          }
          if (criteria.getBranchId() != null) {
            predicates.add(cb.equal(root.get("branch").get("id"), criteria.getBranchId()));
          }
          if (criteria.getRoleName() != null) {
            Join<User, Role> roles = root.join("roles");
            predicates.add(cb.equal(roles.get("name"), criteria.getRoleName()));
          }
          return cb.and(predicates.toArray(new Predicate[0]));
        };

    Page<User> page = userRepository.findAll(spec, pageable);

    List<UserSummaryResponse> items =
        page.getContent().stream().map(this::mapToSummaryResponse).collect(Collectors.toList());

    return PagedResponse.of(
        items, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
  }

  private UserSummaryResponse mapToSummaryResponse(User user) {
    return UserSummaryResponse.builder()
        .id(user.getId())
        .fullName(user.getFullName())
        .email(user.getEmail())
        .status(user.getStatus())
        .roles(
            user.getRoles().stream().map(role -> role.getName().name()).collect(Collectors.toSet()))
        .build();
  }
}
