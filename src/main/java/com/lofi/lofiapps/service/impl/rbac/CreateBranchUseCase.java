package com.lofi.lofiapps.service.impl.rbac;

import com.lofi.lofiapps.dto.request.CreateBranchRequest;
import com.lofi.lofiapps.dto.response.BranchResponse;
import com.lofi.lofiapps.entity.Branch;
import com.lofi.lofiapps.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateBranchUseCase {
  private final BranchRepository branchRepository;

  @Transactional
  public BranchResponse execute(CreateBranchRequest request) {
    Branch branch =
        Branch.builder()
            .name(request.getName())
            .address(request.getAddress())
            .city(request.getCity())
            .state(request.getState())
            .zipCode(request.getZipCode())
            .phone(request.getPhone())
            .longitude(request.getLongitude())
            .latitude(request.getLatitude())
            .build();

    Branch savedBranch = branchRepository.save(branch);

    return BranchResponse.builder()
        .id(savedBranch.getId())
        .name(savedBranch.getName())
        .address(savedBranch.getAddress())
        .city(savedBranch.getCity())
        .state(savedBranch.getState())
        .zipCode(savedBranch.getZipCode())
        .phone(savedBranch.getPhone())
        .longitude(savedBranch.getLongitude())
        .latitude(savedBranch.getLatitude())
        .build();
  }
}
