package com.lofi.lofiapps.config;

import com.lofi.lofiapps.model.entity.*;
import com.lofi.lofiapps.model.enums.ApprovalStage;
import com.lofi.lofiapps.model.enums.DocumentType;
import com.lofi.lofiapps.model.enums.LoanStatus;
import com.lofi.lofiapps.model.enums.RoleName;
import com.lofi.lofiapps.model.enums.UserStatus;
import com.lofi.lofiapps.repository.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
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
public class DevelopmentDataSeeder {

  private final JpaRoleRepository roleRepository;
  private final JpaPermissionRepository permissionRepository;
  private final JpaUserRepository userRepository;
  private final JpaBranchRepository branchRepository;
  private final JpaProductRepository productRepository;
  private final JpaLoanRepository loanRepository;
  private final JpaApprovalHistoryRepository approvalHistoryRepository;
  private final JpaNotificationRepository notificationRepository;
  private final JpaDocumentRepository documentRepository;
  private final JpaAuditLogRepository auditLogRepository;
  private final PasswordEncoder passwordEncoder;

  @Bean
  @Transactional
  public CommandLineRunner seedDevelopmentData() {
    return args -> {
      log.info("Starting Development Data Seeding...");

      cleanupTransactionalData();

      // 1. Roles & Permissions
      initRolesAndPermissions();

      // 2. Branch (Prerequisite for Users)
      JpaBranch branch = initBranch();

      // 3. Admin & Users
      initUsers(branch);

      // 4. Products
      initProducts();

      // 5. Loans & History & Notifications (20 Test Cases)
      initLoans();

      // 6. SLA Test Data
      initSlaLoans();
      initPendingSlaLoans();

      log.info("Development Data Seeding Completed.");
    };
  }

  private void initRolesAndPermissions() {
    // Define Permissions
    String[] permissions = {
      "LOAN_CREATE", "LOAN_SUBMIT", "LOAN_REVIEW", "LOAN_APPROVE",
      "LOAN_DISBURSE", "LOAN_ROLLBACK", "VIEW_DASHBOARD", "EXPORT_REPORT"
    };

    Set<JpaPermission> allPermissions = new HashSet<>();
    for (String permName : permissions) {
      JpaPermission permission =
          permissionRepository
              .findByName(permName)
              .orElseGet(
                  () -> {
                    JpaPermission newPerm =
                        JpaPermission.builder()
                            .name(permName)
                            .description("Permission " + permName)
                            .build();
                    return permissionRepository.save(newPerm);
                  });
      allPermissions.add(permission);
    }

    // Define Roles and assign permissions
    // Simplification: Assigning SUBSET of permissions based on role
    // SUPER_ADMIN get all
    createRole(RoleName.ROLE_SUPER_ADMIN, allPermissions);
    createRole(RoleName.ROLE_ADMIN, allPermissions); // Keep legacy admin populated too

    // CUSTOMER: Create, Submit
    Set<JpaPermission> custPerms = filterPermissions(allPermissions, "LOAN_CREATE", "LOAN_SUBMIT");
    createRole(RoleName.ROLE_CUSTOMER, custPerms);

    // MARKETING: Review, View Dashboard
    Set<JpaPermission> mktPerms =
        filterPermissions(allPermissions, "LOAN_REVIEW", "VIEW_DASHBOARD");
    createRole(RoleName.ROLE_MARKETING, mktPerms);

    // BRANCH_MANAGER: Approve, Rollback, View Dashboard, Export
    Set<JpaPermission> bmPerms =
        filterPermissions(
            allPermissions, "LOAN_APPROVE", "LOAN_ROLLBACK", "VIEW_DASHBOARD", "EXPORT_REPORT");
    createRole(RoleName.ROLE_BRANCH_MANAGER, bmPerms);

    // BACK_OFFICE: Disburse, View Dashboard
    Set<JpaPermission> boPerms =
        filterPermissions(allPermissions, "LOAN_DISBURSE", "VIEW_DASHBOARD");
    createRole(RoleName.ROLE_BACK_OFFICE, boPerms);
  }

  private Set<JpaPermission> filterPermissions(Set<JpaPermission> all, String... names) {
    Set<String> target = Set.of(names);
    return all.stream().filter(p -> target.contains(p.getName())).collect(Collectors.toSet());
  }

  private void createRole(RoleName roleName, Set<JpaPermission> permissions) {
    if (roleRepository.findByName(roleName).isEmpty()) {
      roleRepository.save(JpaRole.builder().name(roleName).permissions(permissions).build());
      log.info("Created Role: {}", roleName);
    } else {
      // Update permissions if exists (optional, but good for idemp)
      JpaRole role = roleRepository.findByName(roleName).get();
      if (role.getPermissions() == null || role.getPermissions().isEmpty()) {
        role.setPermissions(permissions);
        roleRepository.save(role);
      }
    }
  }

  private JpaBranch initBranch() {
    if (branchRepository.count() == 0) {
      JpaBranch branch =
          JpaBranch.builder()
              .name("Main Branch")
              .address("123 Seeder St")
              .city("Jakarta")
              .state("DKI")
              .zipCode("10000")
              .phone("021-000000")
              .build();
      return branchRepository.save(branch);
    }
    return branchRepository.findAll().get(0);
  }

  private void initUsers(JpaBranch branch) {
    String password = passwordEncoder.encode("Password123!");

    // Super Admin
    createUser(
        "admin@lofi.test", "admin", "Super Admin", RoleName.ROLE_SUPER_ADMIN, branch, password);

    // Marketing (3)
    createUser(
        "marketing1@lofi.test",
        "marketing1",
        "Marketing One",
        RoleName.ROLE_MARKETING,
        branch,
        password);
    createUser(
        "marketing2@lofi.test",
        "marketing2",
        "Marketing Two",
        RoleName.ROLE_MARKETING,
        branch,
        password);
    createUser(
        "marketing3@lofi.test",
        "marketing3",
        "Marketing Three",
        RoleName.ROLE_MARKETING,
        branch,
        password);

    // Branch Manager (3)
    createUser(
        "bm1@lofi.test", "bm1", "Branch Manager 1", RoleName.ROLE_BRANCH_MANAGER, branch, password);
    createUser(
        "bm2@lofi.test", "bm2", "Branch Manager 2", RoleName.ROLE_BRANCH_MANAGER, branch, password);
    createUser(
        "bm3@lofi.test", "bm3", "Branch Manager 3", RoleName.ROLE_BRANCH_MANAGER, branch, password);

    // Back Office (2)
    createUser(
        "bo1@lofi.test", "bo1", "Back Office 1", RoleName.ROLE_BACK_OFFICE, branch, password);
    createUser(
        "bo2@lofi.test", "bo2", "Back Office 2", RoleName.ROLE_BACK_OFFICE, branch, password);

    // Customers (6)
    for (int i = 1; i <= 6; i++) {
      createUser(
          "customer" + i + "@lofi.test",
          "customer" + i,
          "Customer " + i,
          RoleName.ROLE_CUSTOMER,
          branch,
          password);
    }
  }

  private void createUser(
      String email,
      String username,
      String fullName,
      RoleName roleName,
      JpaBranch branch,
      String password) {
    if (!userRepository.existsByEmail(email) && !userRepository.existsByUsername(username)) {
      JpaRole role = roleRepository.findByName(roleName).orElseThrow();
      JpaUser user =
          JpaUser.builder()
              .username(username)
              .email(email)
              .password(password)
              .fullName(fullName)
              .branch(branch)
              .status(UserStatus.ACTIVE)
              .roles(Collections.singleton(role))
              .profileCompleted(true)
              .build();
      userRepository.save(user);
      log.info("Created User: {}", email);
    }
  }

  private void initProducts() {
    List<String> allowedCodes = List.of("BASIC", "STANDARD", "PREMIUM");

    // Cleanup old products not in the new list
    productRepository.findAll().stream()
        .filter(p -> !allowedCodes.contains(p.getProductCode()))
        .forEach(
            p -> {
              productRepository.delete(p);
              log.info("Deleted obsolete product: {}", p.getProductName());
            });

    upsertProduct(
        "BASIC",
        "Basic",
        "Basic Product",
        new BigDecimal("1.5"),
        3,
        12,
        new BigDecimal("1000000"),
        new BigDecimal("10000000"));

    upsertProduct(
        "STANDARD",
        "Standard",
        "Standard Product",
        new BigDecimal("1.2"),
        6,
        24,
        new BigDecimal("5000000"),
        new BigDecimal("50000000"));

    upsertProduct(
        "PREMIUM",
        "Premium",
        "Premium Product",
        new BigDecimal("0.9"),
        12,
        60,
        new BigDecimal("20000000"),
        new BigDecimal("200000000"));
  }

  private void upsertProduct(
      String code,
      String name,
      String desc,
      BigDecimal rate,
      int minTenor,
      int maxTenor,
      BigDecimal minAmount,
      BigDecimal maxAmount) {

    Optional<JpaProduct> existing = productRepository.findByProductCode(code);
    JpaProduct product;

    if (existing.isPresent()) {
      product = existing.get();
      product.setProductName(name);
      product.setDescription(desc);
      product.setInterestRate(rate);
      product.setMinTenor(minTenor);
      product.setMaxTenor(maxTenor);
      product.setMinLoanAmount(minAmount);
      product.setMaxLoanAmount(maxAmount);
      // Ensure active if it was previously deactivated
      product.setIsActive(true);
      log.info("Updated Product: {}", name);
    } else {
      product =
          JpaProduct.builder()
              .productCode(code)
              .productName(name)
              .description(desc)
              .interestRate(rate)
              .adminFee(new BigDecimal("100000")) // Default fee
              .minTenor(minTenor)
              .maxTenor(maxTenor)
              .minLoanAmount(minAmount)
              .maxLoanAmount(maxAmount)
              .isActive(true)
              .build();
      log.info("Created Product: {}", name);
    }
    productRepository.save(product);
  }

  private void initLoans() {
    // if (loanRepository.count() > 0) return; // Removed to force re-seed after
    // cleanup

    JpaProduct prod1 = productRepository.findByProductCode("BASIC").orElseThrow();
    JpaProduct prod2 = productRepository.findByProductCode("STANDARD").orElseThrow();

    JpaUser cust1 = findUser("customer1@lofi.test");
    JpaUser cust2 = findUser("customer2@lofi.test");
    JpaUser cust3 = findUser("customer3@lofi.test");
    JpaUser cust4 = findUser("customer4@lofi.test");
    JpaUser cust5 = findUser("customer5@lofi.test");
    JpaUser cust6 = findUser("customer6@lofi.test");

    // TC01-TC05: Draft
    createLoan(cust1, prod1, LoanStatus.DRAFT, ApprovalStage.CUSTOMER, 0, null);
    createLoan(cust1, prod2, LoanStatus.DRAFT, ApprovalStage.CUSTOMER, 1, null);
    createLoan(cust2, prod1, LoanStatus.DRAFT, ApprovalStage.CUSTOMER, 2, null);
    createLoan(cust3, prod1, LoanStatus.DRAFT, ApprovalStage.CUSTOMER, 3, null);
    createLoan(cust4, prod1, LoanStatus.DRAFT, ApprovalStage.CUSTOMER, 4, null);

    // TC06-TC08: Submitted
    createLoan(cust2, prod1, LoanStatus.SUBMITTED, ApprovalStage.MARKETING, 0, "Submit");
    createLoan(cust5, prod2, LoanStatus.SUBMITTED, ApprovalStage.MARKETING, 1, "Submit");
    createLoan(cust2, prod2, LoanStatus.SUBMITTED, ApprovalStage.MARKETING, 2, "Submit");

    // TC09-TC11: Reviewed (By Marketing)
    createLoan(cust3, prod1, LoanStatus.REVIEWED, ApprovalStage.BRANCH_MANAGER, 5, "Review");
    createLoan(cust3, prod2, LoanStatus.REVIEWED, ApprovalStage.BRANCH_MANAGER, 6, "Review");
    createLoan(cust1, prod2, LoanStatus.REVIEWED, ApprovalStage.BRANCH_MANAGER, 7, "Review");

    // TC12-TC14: Approved (By BM)
    createLoan(cust4, prod1, LoanStatus.APPROVED, ApprovalStage.BACKOFFICE, 10, "Approve");
    createLoan(cust1, prod1, LoanStatus.APPROVED, ApprovalStage.BACKOFFICE, 11, "Approve");
    createLoan(cust5, prod1, LoanStatus.APPROVED, ApprovalStage.BACKOFFICE, 12, "Approve");

    // TC15-TC16: Disbursed (By BO)
    createLoan(cust5, prod2, LoanStatus.DISBURSED, ApprovalStage.BACKOFFICE, 20, "Disburse");
    createLoan(cust4, prod2, LoanStatus.DISBURSED, ApprovalStage.BACKOFFICE, 21, "Disburse");

    // TC17: Completed
    createLoan(cust6, prod1, LoanStatus.COMPLETED, ApprovalStage.BACKOFFICE, 30, "Complete");

    // TC18: Cancelled
    createLoan(cust6, prod2, LoanStatus.CANCELLED, ApprovalStage.CUSTOMER, 5, "Cancel");

    // TC19: Rollback (Reviewed -> Submitted)
    createRollbackScenario(cust3);

    // TC20: Double Submission (One Approved, one Cancelled)
    createDoubleSubmissionScenario(cust4);
  }

  private void initSlaLoans() {
    // Check if SLA loans exist. We assume if total loans > 20 (base init), we might
    // have them.
    // But initLoans checks count > 0.
    // Let's rely on checking for a specific reference.

    JpaUser cust = findUser("customer1@lofi.test");
    boolean slaExists =
        loanRepository.findByCustomerId(cust.getId()).stream()
            .anyMatch(l -> "SLA-TEST-REF".equals(l.getDisbursementReference()));

    if (slaExists) return;

    JpaBranch branch = branchRepository.findAll().get(0);
    JpaProduct prod = productRepository.findByProductCode("PREMIUM").orElseThrow();

    // SLA Scenario: Marketing PASS, BM FAIL, BO PASS
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime submitTime = now.minusHours(100);
    LocalDateTime reviewTime = now.minusHours(90); // 10h delta (PASS)
    LocalDateTime approveTime = now.minusHours(30); // 60h delta (FAIL)
    LocalDateTime disburseTime = now.minusHours(20); // 10h delta (PASS)

    JpaLoan loan =
        JpaLoan.builder()
            .customer(cust)
            .product(prod)
            .loanAmount(new BigDecimal("99000000"))
            .tenor(24)
            .loanStatus(LoanStatus.DISBURSED)
            .currentStage(ApprovalStage.BACKOFFICE)
            .submittedAt(submitTime)
            .approvedAt(approveTime)
            .disbursedAt(disburseTime)
            .disbursementReference("SLA-TEST-REF")
            .lastStatusChangedAt(disburseTime)
            .build();
    loan = loanRepository.save(loan);
    log.info("Created SLA Test Loan");

    // History
    approvalHistoryRepository.save(
        JpaApprovalHistory.builder()
            .loanId(loan.getId())
            .fromStatus(LoanStatus.DRAFT)
            .toStatus(LoanStatus.SUBMITTED)
            .actionBy(cust.getUsername())
            .createdAt(submitTime)
            .build());
    approvalHistoryRepository.save(
        JpaApprovalHistory.builder()
            .loanId(loan.getId())
            .fromStatus(LoanStatus.SUBMITTED)
            .toStatus(LoanStatus.REVIEWED)
            .actionBy("marketing1")
            .createdAt(reviewTime)
            .build());
    approvalHistoryRepository.save(
        JpaApprovalHistory.builder()
            .loanId(loan.getId())
            .fromStatus(LoanStatus.REVIEWED)
            .toStatus(LoanStatus.APPROVED)
            .actionBy("bm1")
            .createdAt(approveTime)
            .build());
    approvalHistoryRepository.save(
        JpaApprovalHistory.builder()
            .loanId(loan.getId())
            .fromStatus(LoanStatus.APPROVED)
            .toStatus(LoanStatus.DISBURSED)
            .actionBy("bo1")
            .createdAt(disburseTime)
            .build());
  }

  private void createLoan(
      JpaUser customer,
      JpaProduct product,
      LoanStatus status,
      ApprovalStage stage,
      int dayOffset,
      String action) {
    JpaLoan loan =
        JpaLoan.builder()
            .customer(customer)
            .product(product)
            .loanAmount(product.getMinLoanAmount().add(new BigDecimal("1000000")))
            .tenor(product.getMinTenor())
            .loanStatus(status)
            .currentStage(stage)
            .submittedAt(status != LoanStatus.DRAFT ? LocalDateTime.now().minusDays(5) : null)
            .approvedAt(
                (status == LoanStatus.APPROVED
                        || status == LoanStatus.DISBURSED
                        || status == LoanStatus.COMPLETED)
                    ? LocalDateTime.now().minusDays(3)
                    : null)
            .disbursedAt(
                (status == LoanStatus.DISBURSED || status == LoanStatus.COMPLETED)
                    ? LocalDateTime.now().minusDays(1)
                    : null)
            .disbursementReference(
                status == LoanStatus.DISBURSED || status == LoanStatus.COMPLETED
                    ? UUID.randomUUID().toString()
                    : null)
            .lastStatusChangedAt(LocalDateTime.now())
            .build();

    loan = loanRepository.save(loan);
    log.info("Created Loan for {}: Status {}", customer.getEmail(), status);

    if (action != null) {
      createHistory(loan, action);
      createNotification(loan, action);
      createAudit(loan, action);
    }

    if (status != LoanStatus.DRAFT) {
      createDocuments(loan);
    }
  }

  private void createRollbackScenario(JpaUser user) {
    JpaProduct prod = productRepository.findByProductCode("BASIC").orElseThrow();
    JpaLoan loan =
        JpaLoan.builder()
            .customer(user)
            .product(prod)
            .loanAmount(new BigDecimal("15000000"))
            .tenor(12)
            .loanStatus(LoanStatus.SUBMITTED)
            .currentStage(ApprovalStage.MARKETING)
            .submittedAt(LocalDateTime.now().minusDays(2))
            .lastStatusChangedAt(LocalDateTime.now())
            .build();
    loan = loanRepository.save(loan);

    approvalHistoryRepository.save(
        JpaApprovalHistory.builder()
            .loanId(loan.getId())
            .fromStatus(LoanStatus.DRAFT)
            .toStatus(LoanStatus.SUBMITTED)
            .actionBy(user.getUsername())
            .build());
    approvalHistoryRepository.save(
        JpaApprovalHistory.builder()
            .loanId(loan.getId())
            .fromStatus(LoanStatus.SUBMITTED)
            .toStatus(LoanStatus.REVIEWED)
            .actionBy("marketing1")
            .build());
    approvalHistoryRepository.save(
        JpaApprovalHistory.builder()
            .loanId(loan.getId())
            .fromStatus(LoanStatus.REVIEWED)
            .toStatus(LoanStatus.SUBMITTED)
            .actionBy("marketing1")
            .notes("Rollback for correction")
            .build());
  }

  private void createDoubleSubmissionScenario(JpaUser user) {
    JpaProduct prod = productRepository.findByProductCode("BASIC").orElseThrow();
    JpaLoan loan1 =
        JpaLoan.builder()
            .customer(user)
            .product(prod)
            .loanAmount(new BigDecimal("10000000"))
            .tenor(12)
            .loanStatus(LoanStatus.APPROVED)
            .currentStage(ApprovalStage.BACKOFFICE)
            .submittedAt(LocalDateTime.now().minusDays(5))
            .approvedAt(LocalDateTime.now())
            .lastStatusChangedAt(LocalDateTime.now())
            .build();
    loanRepository.save(loan1);

    JpaLoan loan2 =
        JpaLoan.builder()
            .customer(user)
            .product(prod)
            .loanAmount(new BigDecimal("20000000"))
            .tenor(24)
            .loanStatus(LoanStatus.CANCELLED)
            .currentStage(ApprovalStage.CUSTOMER)
            .submittedAt(LocalDateTime.now().minusDays(5))
            .lastStatusChangedAt(LocalDateTime.now())
            .build();
    loanRepository.save(loan2);

    approvalHistoryRepository.save(
        JpaApprovalHistory.builder()
            .loanId(loan2.getId())
            .fromStatus(LoanStatus.SUBMITTED)
            .toStatus(LoanStatus.CANCELLED)
            .actionBy("SYSTEM")
            .notes("Auto-cancelled")
            .build());
  }

  private void createHistory(JpaLoan loan, String action) {
    JpaApprovalHistory history =
        JpaApprovalHistory.builder()
            .loanId(loan.getId())
            .fromStatus(LoanStatus.DRAFT)
            .toStatus(loan.getLoanStatus())
            .actionBy("system_seed")
            .notes("Seeded action: " + action)
            .build();
    approvalHistoryRepository.save(history);
  }

  private void createNotification(JpaLoan loan, String action) {
    JpaNotification notif =
        JpaNotification.builder()
            .userId(loan.getCustomer().getId())
            .title("Loan Update: " + loan.getLoanStatus())
            .message("Your loan has been updated via " + action)
            .type("LOAN_STATUS")
            .isRead(false)
            .build();
    notificationRepository.save(notif);
  }

  private void createAudit(JpaLoan loan, String action) {
    JpaAuditLog log =
        JpaAuditLog.builder()
            .action("SEED_" + action.toUpperCase())
            .resourceType("Loan")
            .resourceId(loan.getId().toString())
            .userId(loan.getCustomer().getId())
            .details("Seeded " + action)
            .build();
    auditLogRepository.save(log);
  }

  private JpaUser findUser(String email) {
    return userRepository.findByEmail(email).orElseThrow();
  }

  private void createDocuments(JpaLoan loan) {
    createDocument(loan, DocumentType.KTP, "ktp_dummy.jpg");
    createDocument(loan, DocumentType.NPWP, "npwp_dummy.jpg");
    createDocument(loan, DocumentType.KK, "kk_dummy.jpg");
  }

  private void createDocument(JpaLoan loan, DocumentType type, String filename) {
    if (documentRepository.findByLoanId(loan.getId()).stream()
        .noneMatch(d -> d.getDocumentType() == type)) {
      JpaDocument doc =
          JpaDocument.builder()
              .loanId(loan.getId())
              .documentType(type)
              .fileName(filename)
              .objectKey("seeded/" + loan.getId() + "/" + filename)
              .uploadedBy(loan.getCustomer().getId())
              .build();
      documentRepository.save(doc);
    }
  }

  private void cleanupTransactionalData() {
    log.info("Cleaning up transactional data...");
    approvalHistoryRepository.deleteAll();
    notificationRepository.deleteAll();
    documentRepository.deleteAll();
    auditLogRepository.deleteAll();
    loanRepository.deleteAll();
    log.info("Transactional data cleaned up.");
  }

  private void initPendingSlaLoans() {
    JpaBranch branch = branchRepository.findAll().get(0);
    createUser(
        "customer7@lofi.test",
        "customer7",
        "Customer 7",
        RoleName.ROLE_CUSTOMER,
        branch,
        passwordEncoder.encode("Password123!"));

    JpaUser cust = findUser("customer7@lofi.test");
    JpaProduct prod = productRepository.findByProductCode("BASIC").orElseThrow();

    // 1. Breached REVIEW (SUBMITTED 2 days ago)
    createLoan(
        cust, prod, LoanStatus.SUBMITTED, ApprovalStage.MARKETING, 0, "SLA_BREACH_SUBMITTED");
    JpaLoan l1 =
        loanRepository.findAll().stream()
            .filter(
                l ->
                    l.getCustomer().getId().equals(cust.getId())
                        && l.getCurrentStage() == ApprovalStage.MARKETING)
            .max(Comparator.comparing(JpaLoan::getCreatedAt))
            .orElseThrow();

    // Backdate Loan
    l1.setSubmittedAt(LocalDateTime.now().minusDays(2));
    l1.setLastStatusChangedAt(LocalDateTime.now().minusDays(2));
    loanRepository.save(l1);

    // Backdate History (SUBMITTED)
    approvalHistoryRepository
        .findByLoanId(l1.getId())
        .forEach(
            h -> {
              if (h.getToStatus() == LoanStatus.SUBMITTED) {
                h.setCreatedAt(LocalDateTime.now().minusDays(2));
                approvalHistoryRepository.save(h);
              }
            });

    // 2. Safe REVIEW (SUBMITTED 2 hours ago)
    createLoan(cust, prod, LoanStatus.SUBMITTED, ApprovalStage.MARKETING, 0, "SLA_SAFE_SUBMITTED");

    // 3. Breached APPROVAL (REVIEWED 3 days ago)
    createLoan(
        cust, prod, LoanStatus.REVIEWED, ApprovalStage.BRANCH_MANAGER, 0, "SLA_BREACH_REVIEWED");
    JpaLoan l3 =
        loanRepository.findAll().stream()
            .filter(
                l ->
                    l.getCustomer().getId().equals(cust.getId())
                        && l.getCurrentStage() == ApprovalStage.BRANCH_MANAGER
                        && "Seeded action: SLA_BREACH_REVIEWED"
                            .equals(
                                approvalHistoryRepository
                                    .findByLoanId(l.getId())
                                    .get(0)
                                    .getNotes()))
            .findFirst()
            .orElse(null);

    if (l3 != null) {
      // Backdate Loan
      l3.setSubmittedAt(LocalDateTime.now().minusDays(5));
      l3.setLastStatusChangedAt(LocalDateTime.now().minusDays(3));
      loanRepository.save(l3);

      // Backdate History (REVIEWED)
      approvalHistoryRepository
          .findByLoanId(l3.getId())
          .forEach(
              h -> {
                if (h.getToStatus() == LoanStatus.REVIEWED) {
                  h.setCreatedAt(LocalDateTime.now().minusDays(3));
                  approvalHistoryRepository.save(h);
                } else if (h.getToStatus() == LoanStatus.SUBMITTED) {
                  h.setCreatedAt(LocalDateTime.now().minusDays(5));
                  approvalHistoryRepository.save(h);
                }
              });
    }
  }
}
