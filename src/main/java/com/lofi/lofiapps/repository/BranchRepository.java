package com.lofi.lofiapps.repository;

import com.lofi.lofiapps.model.entity.Branch;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BranchRepository {
  Optional<Branch> findById(UUID id);

  List<Branch> findAll();

  Branch save(Branch branch);

  void deleteById(UUID id);
}
