package com.lofi.lofiapps.service.impl.auth;

import com.lofi.lofiapps.model.dto.request.ChangePasswordRequest;
import com.lofi.lofiapps.model.entity.User;
import com.lofi.lofiapps.repository.UserRepository;
import com.lofi.lofiapps.security.service.UserPrincipal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChangePasswordUseCase {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public void execute(ChangePasswordRequest request) {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (!(principal instanceof UserPrincipal)) {
      throw new RuntimeException("Unauthenticated");
    }

    UUID userId = ((UserPrincipal) principal).getId();
    User user =
        userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

    if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
      throw new BadCredentialsException("Invalid old password");
    }

    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);
  }
}
