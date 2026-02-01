package com.lofi.lofiapps.service.impl.mapper;

import com.lofi.lofiapps.dto.response.BranchResponse;
import com.lofi.lofiapps.entity.Branch;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class BranchMapper {

  public BranchResponse toResponse(Branch branch) {
    if (branch == null) return null;
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

  public List<BranchResponse> toResponseList(List<Branch> branches) {
    if (branches == null) return List.of();
    return branches.stream().map(this::toResponse).collect(Collectors.toList());
  }
}
