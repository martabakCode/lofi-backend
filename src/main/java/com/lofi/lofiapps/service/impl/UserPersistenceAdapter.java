package com.lofi.lofiapps.service.impl;

import com.lofi.lofiapps.mapper.UserMapper;
import com.lofi.lofiapps.model.dto.request.UserCriteria;
import com.lofi.lofiapps.model.entity.JpaUser;
import com.lofi.lofiapps.model.entity.User;
import com.lofi.lofiapps.repository.JpaUserRepository;
import com.lofi.lofiapps.repository.UserRepository;
import com.lofi.lofiapps.repository.UserSpecification;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public class UserPersistenceAdapter implements UserRepository {
  private final JpaUserRepository jpaUserRepository;
  private final UserMapper userMapper;

  @Override
  @org.springframework.transaction.annotation.Transactional
  public User save(User user) {
    JpaUser jpaUser = userMapper.toJpa(user);
    JpaUser saved = jpaUserRepository.save(jpaUser);
    return userMapper.toDomain(saved);
  }

  @Override
  public Optional<User> findById(UUID id) {
    return jpaUserRepository.findById(id).map(userMapper::toDomain);
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return jpaUserRepository.findByEmail(email).map(userMapper::toDomain);
  }

  @Override
  public Page<User> findAll(UserCriteria criteria, Pageable pageable) {
    return jpaUserRepository
        .findAll(UserSpecification.withCriteria(criteria), pageable)
        .map(userMapper::toDomain);
  }

  @Override
  public boolean existsByEmail(String email) {
    return jpaUserRepository.existsByEmail(email);
  }

  @Override
  public boolean existsByUsername(String username) {
    return jpaUserRepository.existsByUsername(username);
  }

  @Override
  public void deleteById(UUID id) {
    jpaUserRepository.deleteById(id);
  }
}
