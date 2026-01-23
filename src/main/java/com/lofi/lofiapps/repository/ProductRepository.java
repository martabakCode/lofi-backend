package com.lofi.lofiapps.repository;

import com.lofi.lofiapps.entity.Product;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
  Optional<Product> findByProductCode(String productCode);

  List<Product> findByIsActiveTrue();

  Page<Product> findByIsActive(Boolean isActive, Pageable pageable);
}
