package com.lofi.lofiapps.service.impl.usecase.rbac;

import com.lofi.lofiapps.dto.request.CreateBranchRequest;
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
public class UpdateBranchUseCase {
  private final BranchRepository branchRepository;

  @Transactional
  public BranchResponse execute(UUID id, CreateBranchRequest request) {
    Branch branch =
        branchRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + id));

    branch.setName(request.getName());
    branch.setAddress(request.getAddress());
    branch.setCity(request.getCity());
    branch.setState(request.getState());
    branch.setZipCode(request.getZipCode());
    branch.setPhone(request.getPhone());
    branch.setLongitude(request.getLongitude());
    branch.setLatitude(request.getLatitude());

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
