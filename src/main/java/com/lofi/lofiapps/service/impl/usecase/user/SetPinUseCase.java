package com.lofi.lofiapps.service.impl.usecase.user;

import com.lofi.lofiapps.dto.request.SetPinRequest;
import com.lofi.lofiapps.entity.User;
import com.lofi.lofiapps.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SetPinUseCase {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public void execute(UUID userId, SetPinRequest request) {
    User user =
        userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new RuntimeException("Invalid password");
    }

    user.setPin(passwordEncoder.encode(request.getPin()));
    user.setPinSet(true);
    userRepository.save(user);
  }
}
