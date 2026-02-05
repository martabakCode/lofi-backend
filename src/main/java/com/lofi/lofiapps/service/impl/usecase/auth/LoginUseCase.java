package com.lofi.lofiapps.service.impl.usecase.auth;

import com.lofi.lofiapps.dto.request.LoginRequest;
import com.lofi.lofiapps.dto.response.LoginResponse;
import com.lofi.lofiapps.repository.RefreshTokenRepository;
import com.lofi.lofiapps.repository.UserRepository;
import com.lofi.lofiapps.security.jwt.JwtUtils;
import com.lofi.lofiapps.security.service.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginUseCase {
  private final AuthenticationManager authenticationManager;
  private final JwtUtils jwtUtils;
  private final RefreshTokenRepository refreshTokenRepository;
  private final UserRepository userRepository;
  private final com.lofi.lofiapps.service.ProductCalculationService productCalculationService;

  public LoginResponse execute(LoginRequest request) {
    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

    SecurityContextHolder.getContext().setAuthentication(authentication);
    String jwt = jwtUtils.generateJwtToken(authentication);
    long expiration = jwtUtils.getExpirationFromJwtToken(jwt);

    String refreshTokenStr = jwtUtils.generateRefreshToken(authentication);
    long refreshExpiration = jwtUtils.getExpirationFromJwtToken(refreshTokenStr);
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

    // Revoke existing tokens
    refreshTokenRepository.deleteByUserId(userPrincipal.getId());

    com.lofi.lofiapps.entity.RefreshToken refreshToken =
        com.lofi.lofiapps.entity.RefreshToken.builder()
            .user(com.lofi.lofiapps.entity.User.builder().id(userPrincipal.getId()).build())
            .token(refreshTokenStr)
            .expiryDate(java.time.Instant.now().plusMillis(refreshExpiration))
            .revoked(false)
            .build();

    refreshTokenRepository.save(refreshToken);

    // Update FCM Token if present
    com.lofi.lofiapps.entity.User user =
        userRepository.findById(userPrincipal.getId()).orElse(null);
    if (request.getFcmToken() != null && !request.getFcmToken().isEmpty()) {
      if (user != null) {
        user.setFirebaseToken(request.getFcmToken());
        userRepository.save(user);
      }
    }

    // Calculate product availability and loan status
    java.math.BigDecimal availableLimit = java.math.BigDecimal.ZERO;
    java.math.BigDecimal activeLoanAmount = java.math.BigDecimal.ZERO;
    com.lofi.lofiapps.enums.LoanStatus activeLoanStatus = null;
    boolean hasSubmittedLoan = false;

    if (user != null && user.getProduct() != null) {
      availableLimit =
          productCalculationService.calculateAvailableAmount(
              user.getId(), user.getProduct().getId());
      hasSubmittedLoan = productCalculationService.hasActiveLoan(user.getId());

      java.util.List<com.lofi.lofiapps.entity.Loan> activeLoans =
          productCalculationService.getActiveLoans(user.getId());
      if (!activeLoans.isEmpty()) {
        activeLoanStatus =
            activeLoans.get(0).getLoanStatus(); // Assuming most relevant is first or only one
        activeLoanAmount =
            activeLoans.stream()
                .map(com.lofi.lofiapps.entity.Loan::getLoanAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
      }
    }

    return LoginResponse.builder()
        .accessToken(jwt)
        .refreshToken(refreshTokenStr)
        .expiresIn(expiration / 1000) // Seconds
        .tokenType("Bearer")
        .pinSet(user != null ? user.getPinSet() : false)
        .profileCompleted(user != null ? user.getProfileCompleted() : false)
        .hasSubmittedLoan(hasSubmittedLoan)
        .activeLoanStatus(activeLoanStatus)
        .activeLoanAmount(activeLoanAmount)
        .availableProductLimit(availableLimit)
        .build();
  }
}
