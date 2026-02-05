package com.lofi.lofiapps.service.impl.usecase.user;

import com.lofi.lofiapps.dto.request.UpdatePinRequest;
import com.lofi.lofiapps.entity.User;
import com.lofi.lofiapps.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdatePinUseCase {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public void execute(UUID userId, UpdatePinRequest request) {
    User user =
        userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

    if (user.getPin() != null) {
      if (!passwordEncoder.matches(request.getOldPin(), user.getPin())) {
        throw new RuntimeException("Invalid old PIN");
      }
    }

    user.setPin(passwordEncoder.encode(request.getNewPin()));
    userRepository.save(user);
  }
}
