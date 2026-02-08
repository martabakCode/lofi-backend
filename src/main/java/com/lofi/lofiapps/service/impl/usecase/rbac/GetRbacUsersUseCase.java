package com.lofi.lofiapps.service.impl.usecase.rbac;

import com.lofi.lofiapps.dto.response.UserSummaryResponse;
import com.lofi.lofiapps.entity.User;
import com.lofi.lofiapps.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetRbacUsersUseCase {

  private final UserRepository userRepository;

  public List<UserSummaryResponse> execute() {
    return userRepository.findAll().stream()
        .map(this::mapToSummaryResponse)
        .collect(Collectors.toList());
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
