package com.lofi.lofiapps.service.impl;

import com.lofi.lofiapps.dto.request.*;
import com.lofi.lofiapps.dto.response.*;
import com.lofi.lofiapps.service.AuthService;
import com.lofi.lofiapps.service.impl.usecase.auth.ChangePasswordUseCase;
import com.lofi.lofiapps.service.impl.usecase.auth.ForgotPasswordUseCase;
import com.lofi.lofiapps.service.impl.usecase.auth.GoogleLoginUseCase;
import com.lofi.lofiapps.service.impl.usecase.auth.LoginUseCase;
import com.lofi.lofiapps.service.impl.usecase.auth.LogoutUseCase;
import com.lofi.lofiapps.service.impl.usecase.auth.RefreshTokenUseCase;
import com.lofi.lofiapps.service.impl.usecase.auth.RegisterUseCase;
import com.lofi.lofiapps.service.impl.usecase.auth.ResetPasswordUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final LoginUseCase loginUseCase;
  private final com.lofi.lofiapps.service.impl.usecase.auth.PinLoginUseCase pinLoginUseCase;
  private final LogoutUseCase logoutUseCase;
  private final GoogleLoginUseCase googleLoginUseCase;
  private final ForgotPasswordUseCase forgotPasswordUseCase;
  private final ResetPasswordUseCase resetPasswordUseCase;
  private final ChangePasswordUseCase changePasswordUseCase;
  private final RefreshTokenUseCase refreshTokenUseCase;
  private final RegisterUseCase registerUseCase;
  private final com.lofi.lofiapps.service.impl.usecase.auth.PinResetUseCase pinResetUseCase;

  @Override
  @Transactional
  public LoginResponse login(LoginRequest request) {
    return loginUseCase.execute(request);
  }

  @Override
  @Transactional
  public LoginResponse pinLogin(PinLoginRequest request) {
    return pinLoginUseCase.execute(request);
  }

  @Override
  @Transactional
  public LoginResponse googleLogin(GoogleLoginRequest request) {
    return googleLoginUseCase.execute(request);
  }

  @Override
  @Transactional
  public LoginResponse refresh(String refreshToken) {
    return refreshTokenUseCase.execute(refreshToken);
  }

  @Override
  @Transactional
  public void logout(String token) {
    logoutUseCase.execute(token);
  }

  @Override
  @Transactional
  public void forgotPassword(ForgotPasswordRequest request) {
    forgotPasswordUseCase.execute(request);
  }

  @Override
  @Transactional
  public void resetPassword(ResetPasswordRequest request) {
    resetPasswordUseCase.execute(request);
  }

  @Override
  @Transactional
  public void changePassword(ChangePasswordRequest request) {
    changePasswordUseCase.execute(request);
  }

  @Override
  @Transactional
  public void pinReset(PinResetRequest request) {
    pinResetUseCase.execute(request);
  }

  @Override
  @Transactional
  public LoginResponse register(RegisterRequest request) {
    return registerUseCase.execute(request);
  }
}
