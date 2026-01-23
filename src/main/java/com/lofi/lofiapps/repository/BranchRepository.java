package com.lofi.lofiapps.repository;

import com.lofi.lofiapps.entity.Branch;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BranchRepository extends JpaRepository<Branch, UUID> {}
