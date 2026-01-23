package com.lofi.lofiapps.config;

import com.lofi.lofiapps.entity.Branch;
import com.lofi.lofiapps.entity.Product;
import com.lofi.lofiapps.entity.Role;
import com.lofi.lofiapps.entity.User;
import com.lofi.lofiapps.enums.RoleName;
import com.lofi.lofiapps.enums.UserStatus;
import com.lofi.lofiapps.repository.BranchRepository;
import com.lofi.lofiapps.repository.ProductRepository;
import com.lofi.lofiapps.repository.RoleRepository;
import com.lofi.lofiapps.repository.UserRepository;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

  private final RoleRepository roleRepository;
  private final UserRepository userRepository;
  private final BranchRepository branchRepository;
  private final ProductRepository productRepository;
  private final PasswordEncoder passwordEncoder;

  @Bean
  @Transactional
  public CommandLineRunner initData() {
    return args -> {
      log.info("Starting Data Initialization...");

      // 1. Roles
      initRoles();

      // 2. Branch
      Branch branch = initBranch();

      // 3. Product
      initProduct();

      // 4. Admin User
      initAdminUser(branch);

      log.info("Data Initialization Completed.");
    };
  }

  private void initRoles() {
    for (RoleName roleName : RoleName.values()) {
      if (roleRepository.findByName(roleName).isEmpty()) {
        roleRepository.save(Role.builder().name(roleName).build());
        log.info("Created Role: {}", roleName);
      }
    }
  }

  private Branch initBranch() {
    if (branchRepository.count() == 0) {
      Branch branch =
          Branch.builder()
              .name("Headquarters")
              .address("123 Main St")
              .city("Jakarta")
              .state("DKI Jakarta")
              .zipCode("12345")
              .phone("021-12345678")
              .build();
      return branchRepository.save(branch);
    }
    return branchRepository.findAll().get(0);
  }

  private void initProduct() {
    if (productRepository.count() == 0) {
      Product product =
          Product.builder()
              .productCode("KTA-001")
              .productName("Kredit Tanpa Agunan")
              .description("Fast cash loan without collateral")
              .interestRate(new BigDecimal("0.12")) // 12%
              .adminFee(new BigDecimal("100000"))
              .minTenor(3)
              .maxTenor(24)
              .minLoanAmount(new BigDecimal("1000000"))
              .maxLoanAmount(new BigDecimal("50000000"))
              .isActive(true)
              .build();
      productRepository.save(product);
      log.info("Created Default Product: KTA-001");
    }
  }

  private void initAdminUser(Branch branch) {
    String email = "admin@lofi.test";
    if (!userRepository.existsByEmail(email) && !userRepository.existsByUsername("admin")) {
      Role adminRole =
          roleRepository
              .findByName(RoleName.ROLE_SUPER_ADMIN)
              .orElseThrow(() -> new RuntimeException("Role Admin not found"));

      User admin =
          User.builder()
              .username("admin")
              .email(email)
              .password(passwordEncoder.encode("Password123!"))
              .fullName("Super Admin")
              .branch(branch)
              .status(UserStatus.ACTIVE)
              .roles(new HashSet<>(Collections.singletonList(adminRole)))
              .profileCompleted(true)
              .build();

      userRepository.save(admin);
      log.info("Created Admin User: {}", email);
    }
  }
}
