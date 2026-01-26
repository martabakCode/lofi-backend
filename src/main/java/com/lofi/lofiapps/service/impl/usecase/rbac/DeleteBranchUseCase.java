package com.lofi.lofiapps.service.impl.usecase.rbac;

import com.lofi.lofiapps.repository.BranchRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteBranchUseCase {
  private final BranchRepository branchRepository;

  @Transactional
  public void execute(UUID id) {
    branchRepository.deleteById(id);
  }
}
