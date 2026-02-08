package com.lofi.lofiapps.service.impl.usecase.user;

import com.lofi.lofiapps.dto.request.UpdateGooglePinRequest;
import com.lofi.lofiapps.entity.User;
import com.lofi.lofiapps.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateGooglePinUseCase {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public void execute(UUID userId, UpdateGooglePinRequest request) {
    User user =
        userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

    // Validation: hanya untuk Google users (firebaseUid != null && password ==
    // null)
    if (user.getPassword() != null) {
      throw new RuntimeException("Use UpdatePinRequest for non-Google users");
    }

    if (user.getFirebaseUid() == null) {
      throw new RuntimeException("This endpoint is only for Google authenticated users");
    }

    if (Boolean.FALSE.equals(user.getPinSet())) {
      throw new RuntimeException("PIN is not set. Use set-google-pin endpoint");
    }

    // Validate old PIN
    if (user.getPin() == null || !passwordEncoder.matches(request.getOldPin(), user.getPin())) {
      throw new RuntimeException("Old PIN is incorrect");
    }

    // Update PIN
    user.setPin(passwordEncoder.encode(request.getNewPin()));
    userRepository.save(user);
  }
}
