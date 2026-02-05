package com.lofi.lofiapps.service.impl.usecase.auth;

import com.lofi.lofiapps.dto.request.PinResetRequest;
import com.lofi.lofiapps.entity.User;
import com.lofi.lofiapps.repository.UserRepository;
import com.lofi.lofiapps.service.NotificationService;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PinResetUseCase {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final NotificationService notificationService;

  @Transactional
  public void execute(PinResetRequest request) {
    User user =
        userRepository
            .findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("User not found"));

    // Generate new random 6-digit PIN
    String newPin = generateRandomPin();
    user.setPin(passwordEncoder.encode(newPin));
    userRepository.save(user);

    // Send PIN via email
    notificationService.notifyPinReset(user.getEmail(), newPin);
  }

  private String generateRandomPin() {
    Random random = new Random();
    int pin = 100000 + random.nextInt(900000);
    return String.valueOf(pin);
  }
}
