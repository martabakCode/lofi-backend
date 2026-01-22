package com.lofi.lofiapps.service.impl.auth;

import com.lofi.lofiapps.model.dto.request.ResetPasswordRequest;
import com.lofi.lofiapps.model.entity.User;
import com.lofi.lofiapps.repository.UserRepository;
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
  private final com.lofi.lofiapps.service.NotificationService notificationService;

  @Transactional
  public void execute(ResetPasswordRequest request) {
    String redisKey = "reset_token:" + request.getToken();
    String email = redisTemplate.opsForValue().get(redisKey);

    if (email == null) {
      throw new IllegalArgumentException("Invalid or expired reset token");
    }

    User user =
        userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);

    // Remove token after successful reset
    redisTemplate.delete(redisKey);

    notificationService.notifyPasswordResetSuccess(user.getEmail());
  }
}
