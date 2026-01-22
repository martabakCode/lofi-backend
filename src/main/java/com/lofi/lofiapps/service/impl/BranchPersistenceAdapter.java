package com.lofi.lofiapps.service.impl;

import com.lofi.lofiapps.mapper.BranchMapper;
import com.lofi.lofiapps.model.entity.Branch;
import com.lofi.lofiapps.repository.BranchRepository;
import com.lofi.lofiapps.repository.JpaBranchRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BranchPersistenceAdapter implements BranchRepository {
  private final JpaBranchRepository jpaBranchRepository;
  private final BranchMapper branchMapper;

  @Override
  public Optional<Branch> findById(UUID id) {
    return jpaBranchRepository.findById(id).map(branchMapper::toDomain);
  }

  @Override
  public List<Branch> findAll() {
    return jpaBranchRepository.findAll().stream()
        .map(branchMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public Branch save(Branch branch) {
    return branchMapper.toDomain(jpaBranchRepository.save(branchMapper.toJpa(branch)));
  }

  @Override
  public void deleteById(UUID id) {
    jpaBranchRepository.deleteById(id);
  }
}
