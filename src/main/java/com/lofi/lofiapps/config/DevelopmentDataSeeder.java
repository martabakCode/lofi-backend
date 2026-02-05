package com.lofi.lofiapps.config;

import com.lofi.lofiapps.entity.*;
import com.lofi.lofiapps.enums.ApprovalStage;
import com.lofi.lofiapps.enums.DocumentType;
import com.lofi.lofiapps.enums.LoanStatus;
import com.lofi.lofiapps.enums.RoleName;
import com.lofi.lofiapps.enums.UserStatus;
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
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

/**
 * DevelopmentDataSeeder - Extensive test data for DEVELOPMENT environment only.
 *
 * <p>This seeder ONLY runs when the 'dev' Spring profile is active. It creates comprehensive test
 * data including:
 *
 * <ul>
 *   <li>Roles with specific permissions
 *   <li>Multiple branches
 *   <li>Various user types (Marketing, Branch Managers, Back Office, Customers)
 *   <li>Multiple loan products (BASIC, STANDARD, PREMIUM)
 *   <li>Loans in various statuses (DRAFT, SUBMITTED, REVIEWED, APPROVED, DISBURSED, etc.)
 *   <li>SLA test scenarios
 *   <li>Approval history, notifications, documents, and audit logs
 * </ul>
 *
 * <p><strong>WARNING:</strong> This seeder cleans up and recreates transactional data on each run.
 * It should NEVER be used in production.
 *
 * <p>For minimal required data that runs in all environments, see {@link DataInitializer}.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
@Profile("dev") // Only runs in 'dev' profile
public class DevelopmentDataSeeder {

  private final RoleRepository roleRepository;
  private final PermissionRepository permissionRepository;
  private final UserRepository userRepository;
  private final BranchRepository branchRepository;
  private final ProductRepository productRepository;
  private final LoanRepository loanRepository;
  private final ApprovalHistoryRepository approvalHistoryRepository;
  private final NotificationRepository notificationRepository;
  private final DocumentRepository documentRepository;
  private final AuditLogRepository auditLogRepository;
  private final PasswordEncoder passwordEncoder;

  /**
   * Seeds development data. Only executes when 'dev' profile is active. Cleans up existing
   * transactional data before seeding to ensure fresh test data.
   */
  @Bean
  @Transactional
  public CommandLineRunner seedDevelopmentData() {
    return args -> {
      log.info("Starting Development Data Seeding (dev profile only)...");

      // Clean up transactional data first to ensure fresh test data
      cleanupTransactionalData();

      // 1. Roles & Permissions
      initRolesAndPermissions();

      // 2. Branches
      List<Branch> branches = initIndonesiaCapitalBranches();

      // 3. Admin & Users
      initUsersPerBranch(branches);

      // 4. Products
      initProducts();

      // 5. Loans & History
      initLoans();

      // 6. SLA Test Data
      initSlaLoans();
      initPendingSlaLoans();

      log.info("Development Data Seeding Completed.");
      log.info("Test Accounts:");
      log.info("  - Super Admin: superadmin@lofi.test / Password123! / PIN: [generated]");
      log.info("  - Head Office BO: bo_ho@lofi.test / Password123! / PIN: [generated]");
      log.info("  - DKI Branch BM: bm_dki@lofi.test / Password123! / PIN: [generated]");
      log.info("  - Bali Branch BM: bm_bali@lofi.test / Password123! / PIN: [generated]");
      log.info("  - DKI Customer: cust_dki_1@lofi.test / Password123! / PIN: [generated]");
      log.info("  - Bali Customer: cust_bali_1@lofi.test / Password123! / PIN: [generated]");
    };
  }

  private void initRolesAndPermissions() {
    // Define Permissions
    String[] permissions = {
      "LOAN_CREATE", "LOAN_SUBMIT", "LOAN_REVIEW", "LOAN_APPROVE",
      "LOAN_DISBURSE", "LOAN_ROLLBACK", "VIEW_DASHBOARD", "EXPORT_REPORT",
      "NOTIFICATION_VIEW", "NOTIFICATION_CREATE", "NOTIFICATION_MANAGE", "NOTIFICATION_DELETE"
    };

    Set<Permission> allPermissions = new HashSet<>();
    for (String permName : permissions) {
      Permission permission =
          permissionRepository
              .findByName(permName)
              .orElseGet(
                  () -> {
                    Permission newPerm =
                        Permission.builder()
                            .name(permName)
                            .description("Permission " + permName)
                            .build();
                    return permissionRepository.save(newPerm);
                  });
      allPermissions.add(permission);
    }

    // Define Roles and assign permissions
    // SUPER_ADMIN get all
    createRole(RoleName.ROLE_SUPER_ADMIN, allPermissions);
    createRole(RoleName.ROLE_ADMIN, allPermissions); // Keep legacy admin populated too

    // CUSTOMER: Create, Submit
    Set<Permission> custPerms = filterPermissions(allPermissions, "LOAN_CREATE", "LOAN_SUBMIT");
    createRole(RoleName.ROLE_CUSTOMER, custPerms);

    // MARKETING: Review, View Dashboard
    Set<Permission> mktPerms = filterPermissions(allPermissions, "LOAN_REVIEW", "VIEW_DASHBOARD");
    createRole(RoleName.ROLE_MARKETING, mktPerms);

    // BRANCH_MANAGER: Approve, Rollback, View Dashboard, Export
    Set<Permission> bmPerms =
        filterPermissions(
            allPermissions, "LOAN_APPROVE", "LOAN_ROLLBACK", "VIEW_DASHBOARD", "EXPORT_REPORT");
    createRole(RoleName.ROLE_BRANCH_MANAGER, bmPerms);

    // BACK_OFFICE: Disburse, View Dashboard
    Set<Permission> boPerms = filterPermissions(allPermissions, "LOAN_DISBURSE", "VIEW_DASHBOARD");
    createRole(RoleName.ROLE_BACK_OFFICE, boPerms);
  }

  private Set<Permission> filterPermissions(Set<Permission> all, String... names) {
    Set<String> target = Set.of(names);
    return all.stream().filter(p -> target.contains(p.getName())).collect(Collectors.toSet());
  }

  private void createRole(RoleName roleName, Set<Permission> permissions) {
    Optional<Role> existingRole = roleRepository.findByName(roleName);
    if (existingRole.isEmpty()) {
      roleRepository.save(Role.builder().name(roleName).permissions(permissions).build());
      log.info("Created Role with permissions: {}", roleName);
    } else {
      // Update permissions if role exists but has no permissions
      Role role = existingRole.get();
      if (role.getPermissions() == null || role.getPermissions().isEmpty()) {
        role.setPermissions(permissions);
        roleRepository.save(role);
        log.info("Updated Role permissions: {}", roleName);
      }
    }
  }

  private record BranchSeed(
      String code,
      String name,
      String city,
      String state,
      String address,
      java.math.BigDecimal lat,
      java.math.BigDecimal lng,
      boolean isHeadOffice) {}

  private List<Branch> initIndonesiaCapitalBranches() {
    List<BranchSeed> seeds = new ArrayList<>();

    // Head Office
    seeds.add(
        new BranchSeed(
            "HO",
            "Head Office",
            "Jakarta",
            "DKI",
            "Jalan Merdeka No. 1",
            new BigDecimal("-6.175110"),
            new BigDecimal("106.865036"),
            true));

    // 38 Provinces
    seeds.add(
        new BranchSeed(
            "ACEH",
            "Aceh Branch",
            "Banda Aceh",
            "Aceh",
            "Jl. T. Nyak Arief",
            new BigDecimal("5.548290"),
            new BigDecimal("95.323753"),
            false));
    seeds.add(
        new BranchSeed(
            "SUMUT",
            "Sumut Branch",
            "Medan",
            "Sumatera Utara",
            "Jl. Putri Hijau",
            new BigDecimal("3.595196"),
            new BigDecimal("98.672226"),
            false));
    seeds.add(
        new BranchSeed(
            "SUMBAR",
            "Sumbar Branch",
            "Padang",
            "Sumatera Barat",
            "Jl. Sudirman",
            new BigDecimal("-0.947083"),
            new BigDecimal("100.417181"),
            false));
    seeds.add(
        new BranchSeed(
            "RIAU",
            "Riau Branch",
            "Pekanbaru",
            "Riau",
            "Jl. Jend. Sudirman",
            new BigDecimal("0.507068"),
            new BigDecimal("101.447779"),
            false));
    seeds.add(
        new BranchSeed(
            "KEPRI",
            "Kepri Branch",
            "Tanjung Pinang",
            "Kepulauan Riau",
            "Jl. Basuki Rahmat",
            new BigDecimal("0.916500"),
            new BigDecimal("104.454500"),
            false));
    seeds.add(
        new BranchSeed(
            "JAMBI",
            "Jambi Branch",
            "Jambi",
            "Jambi",
            "Jl. Gatot Subroto",
            new BigDecimal("-1.610122"),
            new BigDecimal("103.613123"),
            false));
    seeds.add(
        new BranchSeed(
            "SUMSEL",
            "Sumsel Branch",
            "Palembang",
            "Sumatera Selatan",
            "Jl. Jend. Sudirman",
            new BigDecimal("-2.976074"),
            new BigDecimal("104.775431"),
            false));
    seeds.add(
        new BranchSeed(
            "BENGKULU",
            "Bengkulu Branch",
            "Bengkulu",
            "Bengkulu",
            "Jl. S. Parman",
            new BigDecimal("-3.800440"),
            new BigDecimal("102.265540"),
            false));
    seeds.add(
        new BranchSeed(
            "LAMPUNG",
            "Lampung Branch",
            "Bandar Lampung",
            "Lampung",
            "Jl. Raden Intan",
            new BigDecimal("-5.450000"),
            new BigDecimal("105.266670"),
            false));
    seeds.add(
        new BranchSeed(
            "BABEL",
            "Babel Branch",
            "Pangkal Pinang",
            "Bangka Belitung",
            "Jl. Jend. Sudirman",
            new BigDecimal("-2.131200"),
            new BigDecimal("106.116100"),
            false));
    seeds.add(
        new BranchSeed(
            "DKI",
            "DKI Branch",
            "Jakarta",
            "DKI Jakarta",
            "Jl. Thamrin",
            new BigDecimal("-6.208763"),
            new BigDecimal("106.845599"),
            false));
    seeds.add(
        new BranchSeed(
            "BANTEN",
            "Banten Branch",
            "Serang",
            "Banten",
            "Jl. Jend. Sudirman",
            new BigDecimal("-6.110400"),
            new BigDecimal("106.163600"),
            false));
    seeds.add(
        new BranchSeed(
            "JABAR",
            "Jabar Branch",
            "Bandung",
            "Jawa Barat",
            "Jl. Asia Afrika",
            new BigDecimal("-6.917464"),
            new BigDecimal("107.619123"),
            false));
    seeds.add(
        new BranchSeed(
            "JATENG",
            "Jateng Branch",
            "Semarang",
            "Jawa Tengah",
            "Jl. Pahlawan",
            new BigDecimal("-6.966667"),
            new BigDecimal("110.416664"),
            false));
    seeds.add(
        new BranchSeed(
            "DIY",
            "DIY Branch",
            "Yogyakarta",
            "DIY",
            "Jl. Malioboro",
            new BigDecimal("-7.795580"),
            new BigDecimal("110.369490"),
            false));
    seeds.add(
        new BranchSeed(
            "JATIM",
            "Jatim Branch",
            "Surabaya",
            "Jawa Timur",
            "Jl. Tunjungan",
            new BigDecimal("-7.257472"),
            new BigDecimal("112.752088"),
            false));
    seeds.add(
        new BranchSeed(
            "BALI",
            "Bali Branch",
            "Denpasar",
            "Bali",
            "Jl. Gajah Mada",
            new BigDecimal("-8.670458"),
            new BigDecimal("115.212629"),
            false));
    seeds.add(
        new BranchSeed(
            "NTB",
            "NTB Branch",
            "Mataram",
            "Nusa Tenggara Barat",
            "Jl. Pejanggik",
            new BigDecimal("-8.583333"),
            new BigDecimal("116.116667"),
            false));
    seeds.add(
        new BranchSeed(
            "NTT",
            "NTT Branch",
            "Kupang",
            "Nusa Tenggara Timur",
            "Jl. El Tari",
            new BigDecimal("-10.177200"),
            new BigDecimal("123.607030"),
            false));
    seeds.add(
        new BranchSeed(
            "KALBAR",
            "Kalbar Branch",
            "Pontianak",
            "Kalimantan Barat",
            "Jl. Ahmad Yani",
            new BigDecimal("-0.026330"),
            new BigDecimal("109.342503"),
            false));
    seeds.add(
        new BranchSeed(
            "KALTENG",
            "Kalteng Branch",
            "Palangka Raya",
            "Kalimantan Tengah",
            "Jl. Tjilik Riwut",
            new BigDecimal("-2.210000"),
            new BigDecimal("113.921300"),
            false));
    seeds.add(
        new BranchSeed(
            "KALSEL",
            "Kalsel Branch",
            "Banjarbaru",
            "Kalimantan Selatan",
            "Jl. A. Yani",
            new BigDecimal("-3.440425"),
            new BigDecimal("114.831500"),
            false));
    seeds.add(
        new BranchSeed(
            "KALTIM",
            "Kaltim Branch",
            "Samarinda",
            "Kalimantan Timur",
            "Jl. Gajah Mada",
            new BigDecimal("-0.502183"),
            new BigDecimal("117.153801"),
            false));
    seeds.add(
        new BranchSeed(
            "KALTARA",
            "Kaltara Branch",
            "Tanjung Selor",
            "Kalimantan Utara",
            "Jl. Sengkawit",
            new BigDecimal("2.891700"),
            new BigDecimal("117.361100"),
            false));
    seeds.add(
        new BranchSeed(
            "SULUT",
            "Sulut Branch",
            "Manado",
            "Sulawesi Utara",
            "Jl. Sam Ratulangi",
            new BigDecimal("1.474830"),
            new BigDecimal("124.842079"),
            false));
    seeds.add(
        new BranchSeed(
            "SULTENG",
            "Sulteng Branch",
            "Palu",
            "Sulawesi Tengah",
            "Jl. Moh. Hatta",
            new BigDecimal("-0.901030"),
            new BigDecimal("119.839584"),
            false));
    seeds.add(
        new BranchSeed(
            "SULSEL",
            "Sulsel Branch",
            "Makassar",
            "Sulawesi Selatan",
            "Jl. Jend. Sudirman",
            new BigDecimal("-5.147665"),
            new BigDecimal("119.432731"),
            false));
    seeds.add(
        new BranchSeed(
            "SULTRA",
            "Sultra Branch",
            "Kendari",
            "Sulawesi Tenggara",
            "Jl. Ahmad Yani",
            new BigDecimal("-3.997290"),
            new BigDecimal("122.512060"),
            false));
    seeds.add(
        new BranchSeed(
            "GORONTALO",
            "Gorontalo Branch",
            "Gorontalo",
            "Gorontalo",
            "Jl. Nani Wartabone",
            new BigDecimal("0.543544"),
            new BigDecimal("123.056769"),
            false));
    seeds.add(
        new BranchSeed(
            "SULBAR",
            "Sulbar Branch",
            "Mamuju",
            "Sulawesi Barat",
            "Jl. Yos Sudarso",
            new BigDecimal("-2.677800"),
            new BigDecimal("118.882100"),
            false));
    seeds.add(
        new BranchSeed(
            "MALUKU",
            "Maluku Branch",
            "Ambon",
            "Maluku",
            "Jl. Pattimura",
            new BigDecimal("-3.695370"),
            new BigDecimal("128.181410"),
            false));
    seeds.add(
        new BranchSeed(
            "MALUT",
            "Malut Branch",
            "Sofifi",
            "Maluku Utara",
            "Jl. K.H. Dewantoro",
            new BigDecimal("0.733300"),
            new BigDecimal("127.566700"),
            false));
    seeds.add(
        new BranchSeed(
            "PAPUA",
            "Papua Branch",
            "Jayapura",
            "Papua",
            "Jl. Ahmad Yani",
            new BigDecimal("-2.548926"),
            new BigDecimal("140.718038"),
            false));
    seeds.add(
        new BranchSeed(
            "PAPBAR",
            "Papua Barat Branch",
            "Manokwari",
            "Papua Barat",
            "Jl. Merdeka",
            new BigDecimal("-0.861453"),
            new BigDecimal("134.062042"),
            false));
    seeds.add(
        new BranchSeed(
            "PAPSEL",
            "Papua Selatan Branch",
            "Merauke",
            "Papua Selatan",
            "Jl. Brawijaya",
            new BigDecimal("-8.499112"),
            new BigDecimal("140.404900"),
            false));
    seeds.add(
        new BranchSeed(
            "PAPTENG",
            "Papua Tengah Branch",
            "Nabire",
            "Papua Tengah",
            "Jl. Merdeka",
            new BigDecimal("-3.366700"),
            new BigDecimal("135.483300"),
            false));
    seeds.add(
        new BranchSeed(
            "PAPPEG",
            "Papua Pegunungan Branch",
            "Wamena",
            "Papua Pegunungan",
            "Jl. Trikora",
            new BigDecimal("-4.097000"),
            new BigDecimal("138.941300"),
            false));
    seeds.add(
        new BranchSeed(
            "PAPBD",
            "Papua Barat Daya Branch",
            "Sorong",
            "Papua Barat Daya",
            "Jl. Ahmad Yani",
            new BigDecimal("-0.876667"),
            new BigDecimal("131.255556"),
            false));

    List<Branch> branches = new ArrayList<>();
    for (BranchSeed seed : seeds) {
      Branch branch =
          branchRepository
              .findByName(seed.name())
              .orElseGet(
                  () ->
                      branchRepository.save(
                          Branch.builder()
                              .name(seed.name())
                              .address(seed.address())
                              .city(seed.city())
                              .state(seed.state())
                              .zipCode("12345")
                              .phone("021-1234567")
                              .latitude(seed.lat())
                              .longitude(seed.lng())
                              .isHeadOffice(seed.isHeadOffice())
                              .build()));
      branches.add(branch);
      log.info("Initialized Branch: {} ({})", branch.getName(), seed.code());

      // Store code in temporary map or pass it down if needed?
      // Actually initUsersPerBranch needs the code.
      // I'll assume we can derive or pass it.
      // Simple hack: Store code in phone or zip? No, unclean.
      // I'll change initUsersPerBranch to take BranchSeed list or just infer code
      // from name or pass a Map.
    }
    return branches;
  }

  // Revised to use Map to map Branch -> Code
  private void initUsersPerBranch(List<Branch> branches) {
    String password = passwordEncoder.encode("Password123!");

    // Create Map of Branch Name -> Code
    Map<String, String> branchCodes = new HashMap<>();
    branchCodes.put("Head Office", "HO");
    branchCodes.put("Aceh Branch", "ACEH");
    branchCodes.put("Sumut Branch", "SUMUT");
    branchCodes.put("Sumbar Branch", "SUMBAR");
    branchCodes.put("Riau Branch", "RIAU");
    branchCodes.put("Kepri Branch", "KEPRI");
    branchCodes.put("Jambi Branch", "JAMBI");
    branchCodes.put("Sumsel Branch", "SUMSEL");
    branchCodes.put("Bengkulu Branch", "BENGKULU");
    branchCodes.put("Lampung Branch", "LAMPUNG");
    branchCodes.put("Babel Branch", "BABEL");
    branchCodes.put("DKI Branch", "DKI");
    branchCodes.put("Banten Branch", "BANTEN");
    branchCodes.put("Jabar Branch", "JABAR");
    branchCodes.put("Jateng Branch", "JATENG");
    branchCodes.put("DIY Branch", "DIY");
    branchCodes.put("Jatim Branch", "JATIM");
    branchCodes.put("Bali Branch", "BALI");
    branchCodes.put("NTB Branch", "NTB");
    branchCodes.put("NTT Branch", "NTT");
    branchCodes.put("Kalbar Branch", "KALBAR");
    branchCodes.put("Kalteng Branch", "KALTENG");
    branchCodes.put("Kalsel Branch", "KALSEL");
    branchCodes.put("Kaltim Branch", "KALTIM");
    branchCodes.put("Kaltara Branch", "KALTARA");
    branchCodes.put("Sulut Branch", "SULUT");
    branchCodes.put("Sulteng Branch", "SULTENG");
    branchCodes.put("Sulsel Branch", "SULSEL");
    branchCodes.put("Sultra Branch", "SULTRA");
    branchCodes.put("Gorontalo Branch", "GORONTALO");
    branchCodes.put("Sulbar Branch", "SULBAR");
    branchCodes.put("Maluku Branch", "MALUKU");
    branchCodes.put("Malut Branch", "MALUT");
    branchCodes.put("Papua Branch", "PAPUA");
    branchCodes.put("Papua Barat Branch", "PAPBAR");
    branchCodes.put("Papua Selatan Branch", "PAPSEL");
    branchCodes.put("Papua Tengah Branch", "PAPTENG");
    branchCodes.put("Papua Pegunungan Branch", "PAPPEG");
    branchCodes.put("Papua Barat Daya Branch", "PAPBD");

    for (Branch branch : branches) {
      String code = branchCodes.getOrDefault(branch.getName(), "UNK");

      if (branch.getIsHeadOffice()) {
        // Super Admin
        createUserIfNotExists(
            "superadmin@lofi.test",
            "superadmin",
            "Super Admin",
            RoleName.ROLE_SUPER_ADMIN,
            branch,
            password);
        // HO Back Office (2)
        createUserIfNotExists(
            "bo_ho_1@lofi.test",
            "bo_ho_1",
            "HO Back Office 1",
            RoleName.ROLE_BACK_OFFICE,
            branch,
            password);
        createUserIfNotExists(
            "bo_ho_2@lofi.test",
            "bo_ho_2",
            "HO Back Office 2",
            RoleName.ROLE_BACK_OFFICE,
            branch,
            password);
        continue;
      }

      // Branch Manager
      createUserIfNotExists(
          "bm_" + code.toLowerCase() + "@lofi.test",
          "bm_" + code.toLowerCase(),
          "BM " + code,
          RoleName.ROLE_BRANCH_MANAGER,
          branch,
          password);

      // Marketing (2)
      for (int i = 1; i <= 2; i++) {
        createUserIfNotExists(
            "mkt_" + code.toLowerCase() + "_" + i + "@lofi.test",
            "mkt_" + code.toLowerCase() + "_" + i,
            "Marketing " + code + " " + i,
            RoleName.ROLE_MARKETING,
            branch,
            password);
      }

      // Back Office (1)
      createUserIfNotExists(
          "bo_" + code.toLowerCase() + "@lofi.test",
          "bo_" + code.toLowerCase(),
          "BO " + code,
          RoleName.ROLE_BACK_OFFICE,
          branch,
          password);

      // Customers (10)
      for (int i = 1; i <= 10; i++) {
        createUserIfNotExists(
            "cust_" + code.toLowerCase() + "_" + i + "@lofi.test",
            "cust_" + code.toLowerCase() + "_" + i,
            "Customer " + code + " " + i,
            RoleName.ROLE_CUSTOMER,
            branch,
            password);
      }
    }
  }

  private String generateRandomPin() {
    Random random = new Random();
    int pin = 100000 + random.nextInt(900000);
    return String.valueOf(pin);
  }

  private void createUserIfNotExists(
      String email,
      String username,
      String fullName,
      RoleName roleName,
      Branch branch,
      String password) {
    if (!userRepository.existsByEmail(email) && !userRepository.existsByUsername(username)) {
      Role role =
          roleRepository
              .findByName(roleName)
              .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

      String pin = generateRandomPin();
      String encryptedPin = passwordEncoder.encode(pin);

      User user =
          User.builder()
              .username(username)
              .email(email)
              .password(password)
              .fullName(fullName)
              .branch(branch)
              .status(UserStatus.ACTIVE)
              .roles(Collections.singleton(role))
              .profileCompleted(true)
              .pin(encryptedPin)
              .pinSet(true)
              .failedLoginAttempts(0)
              .build();
      userRepository.save(user);
      log.info("Created User: {} (PIN: {})", email, pin); // Log PIN for checking
    } else {
      log.debug("User already exists, skipping: {}", email);
    }
  }

  private void initProducts() {
    // Note: DataInitializer may have created KTA-001, we add more products here
    List<String> allowedCodes = List.of("BASIC", "STANDARD", "PREMIUM");

    // Cleanup old products not in the new list (except KTA-001 from
    // DataInitializer)
    productRepository.findAll().stream()
        .filter(
            p ->
                !allowedCodes.contains(p.getProductCode()) && !"KTA-001".equals(p.getProductCode()))
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

    Optional<Product> existing = productRepository.findByProductCode(code);
    Product product;

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
          Product.builder()
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
    Product prod1 = productRepository.findByProductCode("BASIC").orElseThrow();
    Product prod2 = productRepository.findByProductCode("STANDARD").orElseThrow();

    User cust1 = findUser("cust_dki_1@lofi.test");
    User cust2 = findUser("cust_dki_2@lofi.test");
    User cust3 = findUser("cust_dki_3@lofi.test");
    User cust4 = findUser("cust_dki_4@lofi.test");
    User cust5 = findUser("cust_dki_5@lofi.test");
    User cust6 = findUser("cust_dki_6@lofi.test");

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
    User cust = findUser("cust_dki_1@lofi.test");
    boolean slaExists =
        loanRepository.findByCustomerId(cust.getId()).stream()
            .anyMatch(l -> "SLA-TEST-REF".equals(l.getDisbursementReference()));

    if (slaExists) return;

    Product prod = productRepository.findByProductCode("PREMIUM").orElseThrow();

    // SLA Scenario: Marketing PASS, BM FAIL, BO PASS
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime submitTime = now.minusHours(100);
    LocalDateTime reviewTime = now.minusHours(90); // 10h delta (PASS)
    LocalDateTime approveTime = now.minusHours(30); // 60h delta (FAIL)
    LocalDateTime disburseTime = now.minusHours(20); // 10h delta (PASS)

    Loan loan =
        Loan.builder()
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
        ApprovalHistory.builder()
            .loanId(loan.getId())
            .fromStatus(LoanStatus.DRAFT)
            .toStatus(LoanStatus.SUBMITTED)
            .actionBy(cust.getUsername())
            .createdAt(submitTime)
            .build());
    approvalHistoryRepository.save(
        ApprovalHistory.builder()
            .loanId(loan.getId())
            .fromStatus(LoanStatus.SUBMITTED)
            .toStatus(LoanStatus.REVIEWED)
            .actionBy("mkt_dki_1")
            .createdAt(reviewTime)
            .build());
    approvalHistoryRepository.save(
        ApprovalHistory.builder()
            .loanId(loan.getId())
            .fromStatus(LoanStatus.REVIEWED)
            .toStatus(LoanStatus.APPROVED)
            .actionBy("bm_dki")
            .createdAt(approveTime)
            .build());

    approvalHistoryRepository.save(
        ApprovalHistory.builder()
            .loanId(loan.getId())
            .fromStatus(LoanStatus.APPROVED)
            .toStatus(LoanStatus.DISBURSED)
            .actionBy("bo_dki")
            .createdAt(disburseTime)
            .build());
  }

  private void createLoan(
      User customer,
      Product product,
      LoanStatus status,
      ApprovalStage stage,
      int dayOffset,
      String action) {
    Loan loan =
        Loan.builder()
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

  private void createRollbackScenario(User user) {
    Product prod = productRepository.findByProductCode("BASIC").orElseThrow();
    Loan loan =
        Loan.builder()
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
        ApprovalHistory.builder()
            .loanId(loan.getId())
            .fromStatus(LoanStatus.DRAFT)
            .toStatus(LoanStatus.SUBMITTED)
            .actionBy(user.getUsername())
            .build());
    approvalHistoryRepository.save(
        ApprovalHistory.builder()
            .loanId(loan.getId())
            .fromStatus(LoanStatus.SUBMITTED)
            .toStatus(LoanStatus.REVIEWED)
            .actionBy("mkt_dki_1")
            .build());
    approvalHistoryRepository.save(
        ApprovalHistory.builder()
            .loanId(loan.getId())
            .fromStatus(LoanStatus.REVIEWED)
            .toStatus(LoanStatus.SUBMITTED)
            .actionBy("mkt_dki_1")
            .notes("Rollback for correction")
            .build());
  }

  private void createDoubleSubmissionScenario(User user) {
    Product prod = productRepository.findByProductCode("BASIC").orElseThrow();
    Loan loan1 =
        Loan.builder()
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

    Loan loan2 =
        Loan.builder()
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
        ApprovalHistory.builder()
            .loanId(loan2.getId())
            .fromStatus(LoanStatus.SUBMITTED)
            .toStatus(LoanStatus.CANCELLED)
            .actionBy("SYSTEM")
            .notes("Auto-cancelled")
            .build());
  }

  private void createHistory(Loan loan, String action) {
    ApprovalHistory history =
        ApprovalHistory.builder()
            .loanId(loan.getId())
            .fromStatus(LoanStatus.DRAFT)
            .toStatus(loan.getLoanStatus())
            .actionBy("system_seed")
            .notes("Seeded action: " + action)
            .build();
    approvalHistoryRepository.save(history);
  }

  private void createNotification(Loan loan, String action) {
    Notification notif =
        Notification.builder()
            .userId(loan.getCustomer().getId())
            .title("Loan Update: " + loan.getLoanStatus())
            .body("Your loan has been updated via " + action)
            .type(com.lofi.lofiapps.enums.NotificationType.LOAN)
            .referenceId(loan.getId())
            .isRead(false)
            .build();
    notificationRepository.save(notif);
  }

  private void createAudit(Loan loan, String action) {
    AuditLog log =
        AuditLog.builder()
            .action("SEED_" + action.toUpperCase())
            .resourceType("Loan")
            .resourceId(loan.getId().toString())
            .userId(loan.getCustomer().getId())
            .details("Seeded " + action)
            .build();
    auditLogRepository.save(log);
  }

  private User findUser(String email) {
    return userRepository
        .findByEmail(email)
        .orElseThrow(() -> new RuntimeException("User not found: " + email));
  }

  private void createDocuments(Loan loan) {
    createDocument(loan, DocumentType.KTP, "ktp_dummy.jpg");
    createDocument(loan, DocumentType.NPWP, "npwp_dummy.jpg");
    createDocument(loan, DocumentType.KK, "kk_dummy.jpg");
  }

  private void createDocument(Loan loan, DocumentType type, String filename) {
    if (documentRepository.findByLoanId(loan.getId()).stream()
        .noneMatch(d -> d.getDocumentType() == type)) {
      Document doc =
          Document.builder()
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
    Branch branch = branchRepository.findAll().get(0);

    // Create customer7 if not exists
    String cust7Email = "cust_dki_7@lofi.test";
    if (!userRepository.existsByEmail(cust7Email)) {
      createUserIfNotExists(
          cust7Email,
          "cust_dki_7",
          "Customer 7",
          RoleName.ROLE_CUSTOMER,
          branch,
          passwordEncoder.encode("Password123!"));
    }

    User cust = findUser(cust7Email);
    Product prod = productRepository.findByProductCode("BASIC").orElseThrow();

    // 1. Breached REVIEW (SUBMITTED 2 days ago)
    createLoan(
        cust, prod, LoanStatus.SUBMITTED, ApprovalStage.MARKETING, 0, "SLA_BREACH_SUBMITTED");
    Loan l1 =
        loanRepository.findAll().stream()
            .filter(
                l ->
                    l.getCustomer().getId().equals(cust.getId())
                        && l.getCurrentStage() == ApprovalStage.MARKETING)
            .max(Comparator.comparing(Loan::getCreatedAt))
            .orElseThrow(() -> new RuntimeException("SLA loan not found"));

    // Backdate Loan
    // l1.setSubmittedAt(LocalDateTime.now().minusDays(2));
    // l1.setLastStatusChangedAt(LocalDateTime.now().minusDays(2));
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
    Loan l3 =
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
