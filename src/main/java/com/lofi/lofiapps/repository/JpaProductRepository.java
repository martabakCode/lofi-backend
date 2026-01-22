package com.lofi.lofiapps.repository;

import com.lofi.lofiapps.model.entity.JpaProduct;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaProductRepository extends JpaRepository<JpaProduct, UUID> {
  Optional<JpaProduct> findByProductCode(String productCode);

  List<JpaProduct> findByIsActiveTrue();

  Page<JpaProduct> findByIsActive(Boolean isActive, Pageable pageable);
}
