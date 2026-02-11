package com.lofi.lofiapps.service.impl.usecase.rbac;

import com.lofi.lofiapps.dto.response.UserSummaryResponse;
import com.lofi.lofiapps.entity.User;
import com.lofi.lofiapps.exception.ResourceNotFoundException;
import com.lofi.lofiapps.repository.UserRepository;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetRbacUserByIdUseCase {

  private final UserRepository userRepository;

  public UserSummaryResponse execute(UUID userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId.toString()));

    return mapToSummaryResponse(user);
  }

  private UserSummaryResponse mapToSummaryResponse(User user) {
    return UserSummaryResponse.builder()
        .id(user.getId())
        .fullName(user.getFullName())
        .username(user.getUsername())
        .email(user.getEmail())
        .status(user.getStatus())
        .roles(
            user.getRoles().stream().map(role -> role.getName().name()).collect(Collectors.toSet()))
        .branchName(user.getBranch() != null ? user.getBranch().getName() : null)
        .build();
  }
}
