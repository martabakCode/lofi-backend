package com.lofi.lofiapps.service.impl.usecase.rbac;

import com.lofi.lofiapps.dto.response.BranchResponse;
import com.lofi.lofiapps.entity.Branch;
import com.lofi.lofiapps.exception.ResourceNotFoundException;
import com.lofi.lofiapps.repository.BranchRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetBranchByIdUseCase {
  private final BranchRepository branchRepository;

  @Transactional(readOnly = true)
  public BranchResponse execute(UUID id) {
    Branch branch =
        branchRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + id));

    return BranchResponse.builder()
        .id(branch.getId())
        .name(branch.getName())
        .address(branch.getAddress())
        .city(branch.getCity())
        .state(branch.getState())
        .zipCode(branch.getZipCode())
        .phone(branch.getPhone())
        .longitude(branch.getLongitude())
        .latitude(branch.getLatitude())
        .build();
  }
}
