package com.lofi.lofiapps.repository;

import com.lofi.lofiapps.entity.User;
import com.lofi.lofiapps.entity.UserBiodata;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserBiodataRepository extends JpaRepository<UserBiodata, UUID> {
  Optional<UserBiodata> findByUser(User user);

  Optional<UserBiodata> findByUserId(UUID userId);
}
