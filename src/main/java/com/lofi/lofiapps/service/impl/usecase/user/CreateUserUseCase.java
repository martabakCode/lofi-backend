package com.lofi.lofiapps.service.impl.usecase.user;

import com.lofi.lofiapps.dto.request.CreateUserRequest;
import com.lofi.lofiapps.dto.response.UserSummaryResponse;
import com.lofi.lofiapps.entity.Branch;
import com.lofi.lofiapps.entity.User;
import com.lofi.lofiapps.enums.UserStatus;
import com.lofi.lofiapps.exception.ResourceNotFoundException;
import com.lofi.lofiapps.repository.BranchRepository;
import com.lofi.lofiapps.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
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

    // Generate random 6-digit PIN
    java.util.Random random = new java.util.Random();
    String pin = String.valueOf(100000 + random.nextInt(900000));
    String encryptedPin = passwordEncoder.encode(pin);

    User user =
        User.builder()
            .fullName(request.getFullName())
            .email(request.getEmail())
            .username(request.getEmail())
            .password(passwordEncoder.encode("password123")) // Default password
            .pin(encryptedPin)
            .status(UserStatus.ACTIVE)
            .branch(branch)
            .build();

    log.info("Created user: {} with PIN: {}", user.getEmail(), pin);

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
