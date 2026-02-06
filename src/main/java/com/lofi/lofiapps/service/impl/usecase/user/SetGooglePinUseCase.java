package com.lofi.lofiapps.service.impl.usecase.user;

import com.lofi.lofiapps.dto.request.SetGooglePinRequest;
import com.lofi.lofiapps.entity.User;
import com.lofi.lofiapps.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SetGooglePinUseCase {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public void execute(UUID userId, SetGooglePinRequest request) {
    User user =
        userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

    // Validation: hanya untuk Google users (firebaseUid != null && password ==
    // null)
    if (user.getPassword() != null) {
      throw new RuntimeException("Use SetPinRequest for non-Google users");
    }

    if (user.getFirebaseUid() == null) {
      throw new RuntimeException("This endpoint is only for Google authenticated users");
    }

    if (Boolean.TRUE.equals(user.getPinSet())) {
      throw new RuntimeException("PIN is already set. Use update PIN endpoint");
    }

    user.setPin(passwordEncoder.encode(request.getPin()));
    user.setPinSet(true);
    userRepository.save(user);
  }
}
