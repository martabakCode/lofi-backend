package com.lofi.lofiapps.service.impl.usecase.auth;

import com.lofi.lofiapps.dto.request.ResetPasswordRequest;
import com.lofi.lofiapps.entity.User;
import com.lofi.lofiapps.exception.ResourceNotFoundException;
import com.lofi.lofiapps.repository.UserRepository;
import com.lofi.lofiapps.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResetPasswordUseCase {

  private final UserRepository userRepository;
  private final StringRedisTemplate redisTemplate;
  private final PasswordEncoder passwordEncoder;
  private final NotificationService notificationService;

  @Transactional
  public void execute(ResetPasswordRequest request) {
    String token = request.getToken();
    String email = redisTemplate.opsForValue().get("reset_token:" + token);

    if (email == null) {
      throw new IllegalArgumentException("Invalid or expired reset token");
    }

    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);

    // Delete token
    redisTemplate.delete("reset_token:" + token);

    // Notify
    notificationService.notifyPasswordResetSuccess(user.getEmail());
  }
}
