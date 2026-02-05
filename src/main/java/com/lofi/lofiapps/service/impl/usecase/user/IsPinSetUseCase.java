package com.lofi.lofiapps.service.impl.usecase.user;

import com.lofi.lofiapps.entity.User;
import com.lofi.lofiapps.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IsPinSetUseCase {

  private final UserRepository userRepository;

  public boolean execute(UUID userId) {
    return userRepository.findById(userId).map(User::getPinSet).orElse(false);
  }
}
