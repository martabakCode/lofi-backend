package com.lofi.lofiapps.repository;

import com.lofi.lofiapps.model.entity.Product;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepository {
  Product save(Product product);

  Optional<Product> findById(UUID id);

  Optional<Product> findByProductCode(String productCode);

  List<Product> findAllActive();

  Page<Product> findAll(Boolean isActive, Pageable pageable);
}
