package com.lofi.lofiapps.service.impl.user;

import com.lofi.lofiapps.model.dto.request.UserCriteria;
import com.lofi.lofiapps.model.dto.response.PagedResponse;
import com.lofi.lofiapps.model.dto.response.UserSummaryResponse;
import com.lofi.lofiapps.model.entity.User;
import com.lofi.lofiapps.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetUsersUseCase {
  private final UserRepository userRepository;

  public PagedResponse<UserSummaryResponse> execute(UserCriteria criteria, Pageable pageable) {
    Page<User> page = userRepository.findAll(criteria, pageable);

    List<UserSummaryResponse> items =
        page.getContent().stream().map(this::mapToResponse).collect(Collectors.toList());

    return PagedResponse.of(
        items, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
  }

  private UserSummaryResponse mapToResponse(User user) {
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
