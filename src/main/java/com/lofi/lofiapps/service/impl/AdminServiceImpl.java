package com.lofi.lofiapps.service.impl;

import com.lofi.lofiapps.service.AdminService;
import com.lofi.lofiapps.service.impl.usecase.admin.AdminForceLogoutUseCase;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

  private final AdminForceLogoutUseCase adminForceLogoutUseCase;

  @Override
  public void forceLogoutUser(UUID userId) {
    adminForceLogoutUseCase.execute(userId);
  }
}
