package com.lofi.lofiapps.service.impl.usecase.auth;

import com.lofi.lofiapps.dto.request.ForgotPasswordRequest;
import com.lofi.lofiapps.entity.User;
import com.lofi.lofiapps.repository.UserRepository;
import com.lofi.lofiapps.service.impl.NotificationServiceImpl;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ForgotPasswordUseCase {
  private final UserRepository userRepository;
  private final StringRedisTemplate redisTemplate;
  private final NotificationServiceImpl notificationService;

  public void execute(ForgotPasswordRequest request) {
    User user = userRepository.findByEmail(request.getEmail()).orElse(null);

    // As per spec: "If email exists, reset link will be sent"
    // We don't want to leak user existence, so we always return success.
    if (user != null) {
      String token = UUID.randomUUID().toString();

      // Store token in Redis with 1 hour TTL
      // KEY: reset_token:{token}, VALUE: {email}
      redisTemplate.opsForValue().set("reset_token:" + token, user.getEmail(), 1, TimeUnit.HOURS);

      notificationService.notifyForgotPassword(user.getEmail(), token);
    }
  }
}
