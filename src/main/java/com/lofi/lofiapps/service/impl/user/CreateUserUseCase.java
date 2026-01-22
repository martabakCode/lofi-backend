package com.lofi.lofiapps.service.impl.user;

import com.lofi.lofiapps.exception.ResourceNotFoundException;
import com.lofi.lofiapps.model.dto.request.CreateUserRequest;
import com.lofi.lofiapps.model.dto.response.UserSummaryResponse;
import com.lofi.lofiapps.model.entity.Branch;
import com.lofi.lofiapps.model.entity.User;
import com.lofi.lofiapps.model.enums.UserStatus;
import com.lofi.lofiapps.repository.BranchRepository;
import com.lofi.lofiapps.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateUserUseCase {
  private final UserRepository userRepository;
  private final BranchRepository branchRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public UserSummaryResponse execute(CreateUserRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new IllegalArgumentException("Email already exists");
    }

    Branch branch = null;
    if (request.getBranchId() != null) {
      branch =
          branchRepository
              .findById(request.getBranchId())
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException(
                          "Branch not found with id: " + request.getBranchId()));
    }

    User user =
        User.builder()
            .fullName(request.getFullName())
            .email(request.getEmail())
            .username(request.getEmail())
            .password(
                passwordEncoder.encode(
                    "password123")) // Default password, should be changed by user
            .status(UserStatus.ACTIVE)
            .branch(branch)
            .build();

    User savedUser = userRepository.save(user);

    return UserSummaryResponse.builder()
        .id(savedUser.getId())
        .fullName(savedUser.getFullName())
        .email(savedUser.getEmail())
        .username(savedUser.getUsername())
        .status(savedUser.getStatus())
        .branchName(savedUser.getBranch() != null ? savedUser.getBranch().getName() : null)
        .build();
  }
}
