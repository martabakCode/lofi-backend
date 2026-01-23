package com.lofi.lofiapps.service.impl.rbac;

import com.lofi.lofiapps.dto.response.RoleResponse;
import com.lofi.lofiapps.entity.User;
import com.lofi.lofiapps.exception.ResourceNotFoundException;
import com.lofi.lofiapps.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetUserRolesUseCase {
  private final UserRepository userRepository;

  public List<RoleResponse> execute(UUID userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId.toString()));

    return user.getRoles().stream()
        .map(
            r ->
                RoleResponse.builder()
                    .id(r.getId())
                    .name(r.getName())
                    .description(r.getDescription())
                    .build())
        .collect(Collectors.toList());
  }
}
