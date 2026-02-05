package com.lofi.lofiapps.service;

import com.lofi.lofiapps.dto.request.*;
import com.lofi.lofiapps.dto.response.*;

public interface AuthService {
  LoginResponse login(LoginRequest request);

  LoginResponse pinLogin(PinLoginRequest request);

  LoginResponse googleLogin(GoogleLoginRequest request);

  LoginResponse refresh(String refreshToken);

  void logout(String token);

  void forgotPassword(ForgotPasswordRequest request);

  void resetPassword(ResetPasswordRequest request);

  void changePassword(ChangePasswordRequest request);

  void pinReset(PinResetRequest request);

  LoginResponse register(RegisterRequest request);
}
