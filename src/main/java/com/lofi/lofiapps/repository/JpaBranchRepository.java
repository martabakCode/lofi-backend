package com.lofi.lofiapps.repository;

import com.lofi.lofiapps.model.entity.JpaBranch;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaBranchRepository extends JpaRepository<JpaBranch, UUID> {}
