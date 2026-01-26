package com.lofi.lofiapps.service.impl.usecase.admin;

import com.lofi.lofiapps.entity.User;
import com.lofi.lofiapps.exception.ResourceNotFoundException;
import com.lofi.lofiapps.repository.UserRepository;
import com.lofi.lofiapps.security.service.TokenBlacklistService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminForceLogoutUseCase {

  private final UserRepository userRepository;
  private final TokenBlacklistService tokenBlacklistService;

  @Transactional
  public void execute(UUID userId) {
    // 1. Validate that user exists
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId.toString()));

    log.info("Admin forcing logout for user: {} ({})", user.getEmail(), userId);

    // 2. Force logout by invalidating all tokens for this user
    tokenBlacklistService.forceLogoutUser(userId);

    log.info("Successfully forced logout for user: {}", userId);
  }
}
