package com.lofi.lofiapps.service.impl.usecase.auth;

import com.lofi.lofiapps.dto.request.LoginRequest;
import com.lofi.lofiapps.dto.request.RegisterRequest;
import com.lofi.lofiapps.dto.response.LoginResponse;
import com.lofi.lofiapps.entity.Role;
import com.lofi.lofiapps.entity.User;
import com.lofi.lofiapps.enums.RoleName;
import com.lofi.lofiapps.enums.UserStatus;
import com.lofi.lofiapps.repository.RoleRepository;
import com.lofi.lofiapps.repository.UserRepository;
import java.util.Collections;
import java.util.HashSet;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegisterUseCase {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;
  private final LoginUseCase loginUseCase;

  public LoginResponse execute(RegisterRequest request) {
    if (userRepository.existsByUsername(request.getUsername())) {
      throw new IllegalArgumentException("Username is already taken");
    }

    if (userRepository.existsByEmail(request.getEmail())) {
      throw new IllegalArgumentException("Email is already in use");
    }

    Role customerRole =
        roleRepository
            .findByName(RoleName.ROLE_CUSTOMER)
            .orElseGet(
                () ->
                    roleRepository.save(
                        Role.builder()
                            .name(RoleName.ROLE_CUSTOMER)
                            .description("Default customer role")
                            .build()));

    User user =
        User.builder()
            .fullName(request.getFullName())
            .username(request.getUsername())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .phoneNumber(request.getPhoneNumber())
            .status(UserStatus.ACTIVE)
            .roles(new HashSet<>(Collections.singletonList(customerRole)))
            .build();

    userRepository.save(user);

    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setEmail(request.getEmail());
    loginRequest.setPassword(request.getPassword());

    return loginUseCase.execute(loginRequest);
  }
}
