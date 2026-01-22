package com.lofi.lofiapps.repository;

import com.lofi.lofiapps.model.dto.request.UserCriteria;
import com.lofi.lofiapps.model.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepository {
  User save(User user);

  Optional<User> findById(UUID id);

  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);

  boolean existsByUsername(String username);

  Page<User> findAll(UserCriteria criteria, Pageable pageable);

  void deleteById(UUID id);
}
