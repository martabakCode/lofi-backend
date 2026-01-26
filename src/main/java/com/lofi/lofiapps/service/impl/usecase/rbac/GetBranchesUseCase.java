package com.lofi.lofiapps.service.impl.usecase.rbac;

import com.lofi.lofiapps.dto.response.BranchResponse;
import com.lofi.lofiapps.repository.BranchRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetBranchesUseCase {
  private final BranchRepository branchRepository;

  @Transactional(readOnly = true)
  public List<BranchResponse> execute() {
    return branchRepository.findAll().stream()
        .map(
            branch ->
                BranchResponse.builder()
                    .id(branch.getId())
                    .name(branch.getName())
                    .address(branch.getAddress())
                    .city(branch.getCity())
                    .state(branch.getState())
                    .zipCode(branch.getZipCode())
                    .phone(branch.getPhone())
                    .longitude(branch.getLongitude())
                    .latitude(branch.getLatitude())
                    .build())
        .collect(Collectors.toList());
  }
}
