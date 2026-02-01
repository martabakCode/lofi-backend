# Code Review Report: Lofi Backend

## Executive Summary

Project ini menggunakan arsitektur **Use Case Pattern** yang baik dengan pemisahan logika bisnis ke dalam class-class kecil yang fokus. Secara umum, kode sudah mengikuti banyak best practices seperti:
- Dependency Injection dengan Lombok [`@RequiredArgsConstructor`](src/main/java/com/lofi/lofiapps/service/impl/usecase/loan/ApplyLoanUseCase.java:27)
- Transactional boundaries yang jelas
- DTO pattern untuk request/response
- Repository pattern untuk data access

Namun, terdapat **duplikasi kode yang signifikan** yang perlu di-refactor untuk meningkatkan maintainability.

---

## 1. Duplikasi Kode yang Ditemukan

### 1.1 Validasi UserBiodata (HIGH PRIORITY)

**Lokasi Duplikasi:**
- [`ApplyLoanUseCase.java`](src/main/java/com/lofi/lofiapps/service/impl/usecase/loan/ApplyLoanUseCase.java:165-193) - lines 165-193
- [`MarketingApplyLoanUseCase.java`](src/main/java/com/lofi/lofiapps/service/impl/usecase/loan/MarketingApplyLoanUseCase.java:179-207) - lines 179-207  
- [`SubmitLoanUseCase.java`](src/main/java/com/lofi/lofiapps/service/impl/usecase/loan/SubmitLoanUseCase.java:115-143) - lines 115-143

**Potongan Kode Duplikat:**

```java
private void validateUserBiodataComplete(UserBiodata userBiodata) {
  if (userBiodata.getNik() == null || userBiodata.getNik().isBlank()) {
    throw new IllegalStateException("User biodata is incomplete: NIK is required.");
  }
  if (userBiodata.getDateOfBirth() == null) {
    throw new IllegalStateException("User biodata is incomplete: Date of birth is required.");
  }
  // ... 7 validasi lainnya dengan pola yang sama
}
```

**Rekomendasi:**
Buat class `UserBiodataValidator` sebagai Spring Component:

```java
@Component
public class UserBiodataValidator {
    public void validateComplete(UserBiodata userBiodata) {
        // implementation
    }
}
```

---

### 1.2 Validasi Risk Conditions (HIGH PRIORITY)

**Lokasi Duplikasi:**
- [`ApplyLoanUseCase.java`](src/main/java/com/lofi/lofiapps/service/impl/usecase/loan/ApplyLoanUseCase.java:195-224) - lines 195-224
- [`MarketingApplyLoanUseCase.java`](src/main/java/com/lofi/lofiapps/service/impl/usecase/loan/MarketingApplyLoanUseCase.java:209-239) - lines 209-239
- [`SubmitLoanUseCase.java`](src/main/java/com/lofi/lofiapps/service/impl/usecase/loan/SubmitLoanUseCase.java:145-174) - lines 145-174

**Potongan Kode Duplikat:**

```java
private void validateRiskConditions(User user, UserBiodata userBiodata, LoanRequest request) {
  // Risk 1: Check overdue days
  if (user.getTotalOverdueDays() > 30) {
    throw new IllegalStateException("Risk check failed: User has excessive overdue days...");
  }
  // Risk 2-4: Debt-to-income, loan history, minimum income
}
```

**Rekomendasi:**
Buat class `RiskValidator` sebagai Spring Component:

```java
@Component
public class RiskValidator {
    public void validate(User user, UserBiodata biodata, BigDecimal loanAmount) {
        // implementation
    }
}
```

---

### 1.3 Mapping BranchResponse (MEDIUM PRIORITY)

**Lokasi Duplikasi:**
- [`GetBranchesUseCase.java`](src/main/java/com/lofi/lofiapps/service/impl/usecase/rbac/GetBranchesUseCase.java:18-32) - lines 18-32
- [`CreateBranchUseCase.java`](src/main/java/com/lofi/lofiapps/service/impl/usecase/rbac/CreateBranchUseCase.java:32-43) - lines 32-43
- [`UpdateBranchUseCase.java`](src/main/java/com/lofi/lofiapps/service/impl/usecase/rbac/UpdateBranchUseCase.java:36-47) - lines 36-47

**Potongan Kode Duplikat:**

```java
return BranchResponse.builder()
    .id(branch.getId())
    .name(branch.getName())
    .address(branch.getAddress())
    .city(branch.getCity())
    .state(branch.getState())
    .zipCode(branch.getZipCode())
    .phone(branch.getPhone())
    .longitude(branch.getLongitude())
    .latitude(branch.getLatitude())
    .build();
```

**Rekomendasi:**
Tambahkan static factory method di `BranchResponse` atau buat `BranchMapper`:

```java
@Component
public class BranchMapper {
    public BranchResponse toResponse(Branch branch) {
        if (branch == null) return null;
        return BranchResponse.builder()
            .id(branch.getId())
            .name(branch.getName())
            // ... mapping lainnya
            .build();
    }
}
```

---

### 1.4 Mapping PermissionResponse (MEDIUM PRIORITY)

**Lokasi Duplikasi:**
- [`GetPermissionsUseCase.java`](src/main/java/com/lofi/lofiapps/service/impl/usecase/rbac/GetPermissionsUseCase.java:22-28) - lines 22-28
- [`GetRolePermissionsUseCase.java`](src/main/java/com/lofi/lofiapps/service/impl/usecase/rbac/GetRolePermissionsUseCase.java:25-32) - lines 25-32
- [`CreateRoleUseCase.java`](src/main/java/com/lofi/lofiapps/service/impl/usecase/rbac/CreateRoleUseCase.java:44-52) - lines 44-52
- [`UpdateRoleUseCase.java`](src/main/java/com/lofi/lofiapps/service/impl/usecase/rbac/UpdateRoleUseCase.java:48-57) - lines 48-57
- [`GetRolesUseCase.java`](src/main/java/com/lofi/lofiapps/service/impl/usecase/rbac/GetRolesUseCase.java:31-37) - lines 31-37

**Potongan Kode Duplikat:**

```java
p -> PermissionResponse.builder()
    .id(p.getId())
    .name(p.getName())
    .description(p.getDescription())
    .build()
```

**Rekomendasi:**
Buat `PermissionMapper` atau static method di `PermissionResponse`:

```java
public static PermissionResponse from(Permission permission) {
    if (permission == null) return null;
    return PermissionResponse.builder()
        .id(permission.getId())
        .name(permission.getName())
        .description(permission.getDescription())
        .build();
}
```

---

### 1.5 Mapping RoleResponse (MEDIUM PRIORITY)

**Lokasi Duplikasi:**
- [`GetRolesUseCase.java`](src/main/java/com/lofi/lofiapps/service/impl/usecase/rbac/GetRolesUseCase.java:21-39) - lines 21-39
- [`GetUserRolesUseCase.java`](src/main/java/com/lofi/lofiapps/service/impl/usecase/rbac/GetUserRolesUseCase.java:25-32) - lines 25-32
- [`CreateRoleUseCase.java`](src/main/java/com/lofi/lofiapps/service/impl/usecase/rbac/CreateRoleUseCase.java:54-60) - lines 54-60
- [`UpdateRoleUseCase.java`](src/main/java/com/lofi/lofiapps/service/impl/usecase/rbac/UpdateRoleUseCase.java:59-65) - lines 59-65

**Rekomendasi:**
Buat `RoleMapper` class.

---

### 1.6 Calculate Available Plafond (MEDIUM PRIORITY)

**Lokasi Duplikasi:**
- [`ApplyLoanUseCase.java`](src/main/java/com/lofi/lofiapps/service/impl/usecase/loan/ApplyLoanUseCase.java:315-333) - lines 315-333
- [`ApproveLoanUseCase.java`](src/main/java/com/lofi/lofiapps/service/impl/usecase/loan/ApproveLoanUseCase.java:153-175) - lines 153-175
- [`GetUserProfileUseCase.java`](src/main/java/com/lofi/lofiapps/service/impl/usecase/user/GetUserProfileUseCase.java:101-123) - lines 101-123

**Rekomendasi:**
Buat `PlafondCalculator` sebagai Spring Component:

```java
@Component
public class PlafondCalculator {
    private final LoanRepository loanRepository;
    
    public BigDecimal calculateAvailablePlafond(User user, Product product) {
        // implementation
    }
    
    public BigDecimal calculateAvailablePlafond(User user) {
        if (user.getProduct() == null) return BigDecimal.ZERO;
        return calculateAvailablePlafond(user, user.getProduct());
    }
}
```

---

### 1.7 ApprovalHistory Builder Pattern (LOW PRIORITY)

**Lokasi Duplikasi:**
Terjadi di hampir semua loan use cases:
- [`ApplyLoanUseCase.java`](src/main/java/com/lofi/lofiapps/service/impl/usecase/loan/ApplyLoanUseCase.java:153-160)
- [`ApproveLoanUseCase.java`](src/main/java/com/lofi/lofiapps/service/impl/usecase/loan/ApproveLoanUseCase.java:99-106)
- [`SubmitLoanUseCase.java`](src/main/java/com/lofi/lofiapps/service/impl/usecase/loan/SubmitLoanUseCase.java:90-98)
- [`ReviewLoanUseCase.java`](src/main/java/com/lofi/lofiapps/service/impl/usecase/loan/ReviewLoanUseCase.java:49-56)
- [`RejectLoanUseCase.java`](src/main/java/com/lofi/lofiapps/service/impl/usecase/loan/RejectLoanUseCase.java:46-53)
- [`CancelLoanUseCase.java`](src/main/java/com/lofi/lofiapps/service/impl/usecase/loan/CancelLoanUseCase.java:53-60)
- [`DisburseLoanUseCase.java`](src/main/java/com/lofi/lofiapps/service/impl/usecase/loan/DisburseLoanUseCase.java:57-64)
- [`RollbackLoanUseCase.java`](src/main/java/com/lofi/lofiapps/service/impl/usecase/loan/RollbackLoanUseCase.java:60-67)
- [`CompleteLoanUseCase.java`](src/main/java/com/lofi/lofiapps/service/impl/usecase/loan/CompleteLoanUseCase.java:47-54)

**Potongan Kode Duplikat:**

```java
approvalHistoryRepository.save(
    ApprovalHistory.builder()
        .loanId(loan.getId())
        .fromStatus(fromStatus)
        .toStatus(toStatus)
        .actionBy(username)
        .notes(notes)
        .build());
```

**Rekomendasi:**
Buat factory method di `ApprovalHistory` atau `ApprovalHistoryService`:

```java
@Service
public class ApprovalHistoryService {
    public void recordStatusChange(UUID loanId, LoanStatus from, LoanStatus to, 
                                   String actionBy, String notes) {
        approvalHistoryRepository.save(
            ApprovalHistory.builder()
                .loanId(loanId)
                .fromStatus(from)
                .toStatus(to)
                .actionBy(actionBy)
                .notes(notes)
                .build()
        );
    }
}
```

---

## 2. Best Practices yang Sudah Baik

### 2.1 Arsitektur Use Case Pattern

Project ini menerapkan **Use Case Pattern** dengan sangat baik. Setiap use case memiliki satu tanggung jawab tunggal:

```java
@Service
@RequiredArgsConstructor
public class ApplyLoanUseCase {
    // Dependencies...
    
    @Transactional
    public LoanResponse execute(LoanRequest request, UUID userId, String username) {
        // Single responsibility: apply loan
    }
}
```

### 2.2 Service Layer Delegation

[`AuthServiceImpl`](src/main/java/com/lofi/lofiapps/service/impl/AuthServiceImpl.java) dan [`LoanServiceImpl`](src/main/java/com/lofi/lofiapps/service/impl/LoanServiceImpl.java) hanya mendelegasikan ke use case tanpa logika bisnis:

```java
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final LoginUseCase loginUseCase;
    // ... other use cases
    
    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        return loginUseCase.execute(request);
    }
}
```

### 2.3 Guard Pattern

[`BranchAccessGuard`](src/main/java/com/lofi/lofiapps/service/BranchAccessGuard.java) dan [`RoleActionGuard`](src/main/java/com/lofi/lofiapps/service/RoleActionGuard.java) menerapkan Guard Pattern yang baik untuk authorization.

### 2.4 Global Exception Handler

[`GlobalExceptionHandler`](src/main/java/com/lofi/lofiapps/exception/GlobalExceptionHandler.java) menangani exception secara terpusat dengan response format yang konsisten.

### 2.5 ApiResponse Pattern

[`ApiResponse`](src/main/java/com/lofi/lofiapps/dto/response/ApiResponse.java) menyediakan format response yang konsisten untuk semua endpoint.

---

## 3. Issues dan Rekomendasi Perbaikan

### 3.1 System.out.println di Production Code

**Lokasi:** [`AuthController.java:26`](src/main/java/com/lofi/lofiapps/controller/AuthController.java:26)

```java
System.out.println("Login attempt for email: " + request.getEmail());
```

**Rekomendasi:** Gunakan logger (SLF4J) yang sudah tersedia:

```java
log.info("Login attempt for email: {}", request.getEmail());
```

### 3.2 Import Statement yang Tidak Konsisten

**Lokasi:** [`AuthController.java:41`](src/main/java/com/lofi/lofiapps/controller/AuthController.java:41)

```java
@Valid @RequestBody com.lofi.lofiapps.dto.request.GoogleLoginRequest request
```

**Rekomendasi:** Gunakan import statement:

```java
import com.lofi.lofiapps.dto.request.GoogleLoginRequest;
```

### 3.3 Mixed Annotation Usage (@Component vs @Service)

Beberapa use case menggunakan `@Component` dan `@Service` secara tidak konsisten:
- [`ApplyLoanUseCase`](src/main/java/com/lofi/lofiapps/service/impl/usecase/loan/ApplyLoanUseCase.java:27) - `@Component`
- [`GetBranchesUseCase`](src/main/java/com/lofi/lofiapps/service/impl/usecase/rbac/GetBranchesUseCase.java:11) - `@Service`

**Rekomendasi:** Gunakan `@Service` secara konsisten untuk semua use case.

### 3.4 Full Qualified Class Names

**Lokasi:** [`LoanServiceImpl.java:49-50`](src/main/java/com/lofi/lofiapps/service/impl/LoanServiceImpl.java:49-50)

```java
private final com.lofi.lofiapps.service.impl.usecase.loan.MarketingApplyLoanUseCase marketingApplyLoanUseCase;
```

**Rekomendasi:** Gunakan import statement.

---

## 4. Refactoring Priority Matrix

| Priority |                Issue                 |     Impact      |  Effort  |
|----------|--------------------------------------|-----------------|----------|
| HIGH     | Duplikasi validasi UserBiodata       | Maintainability | Low      |
| HIGH     | Duplikasi validasi Risk Conditions   | Maintainability | Low      |
| MEDIUM   | Duplikasi mapping BranchResponse     | Maintainability | Low      |
| MEDIUM   | Duplikasi mapping PermissionResponse | Maintainability | Low      |
| MEDIUM   | Duplikasi mapping RoleResponse       | Maintainability | Low      |
| MEDIUM   | Duplikasi calculateAvailablePlafond  | Maintainability | Low      |
| LOW      | Duplikasi ApprovalHistory builder    | Maintainability | Low      |
| LOW      | System.out.println                   | Code Quality    | Very Low |
| LOW      | Import statements                    | Code Style      | Very Low |

---

## 5. Struktur Folder yang Direkomendasikan

```
service/
├── impl/
│   ├── AuthServiceImpl.java
│   ├── LoanServiceImpl.java
│   └── ...
├── impl/usecase/
│   ├── loan/
│   ├── auth/
│   ├── rbac/
│   └── ...
├── impl/mapper/          # NEW: Centralized mappers
│   ├── BranchMapper.java
│   ├── PermissionMapper.java
│   ├── RoleMapper.java
│   └── UserBiodataMapper.java
├── impl/validator/       # NEW: Centralized validators
│   ├── UserBiodataValidator.java
│   ├── RiskValidator.java
│   └── DocumentValidator.java
├── impl/calculator/      # NEW: Business calculators
│   └── PlafondCalculator.java
├── impl/factory/         # NEW: Entity factories
│   └── ApprovalHistoryFactory.java
├── BranchAccessGuard.java
├── LoanActionValidator.java
└── RoleActionGuard.java
```

---

## 6. Contoh Implementasi Refactoring

### 6.1 UserBiodataValidator

```java
@Component
@RequiredArgsConstructor
public class UserBiodataValidator {
    
    private final UserBiodataRepository userBiodataRepository;
    
    public UserBiodata validateAndGet(UUID userId) {
        UserBiodata biodata = userBiodataRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalStateException(
                "User biodata is incomplete. Please complete your biodata first."));
        validateComplete(biodata);
        return biodata;
    }
    
    public void validateComplete(UserBiodata userBiodata) {
        validateField(userBiodata.getNik(), "NIK");
        validateField(userBiodata.getDateOfBirth(), "Date of birth");
        validateField(userBiodata.getPlaceOfBirth(), "Place of birth");
        validateField(userBiodata.getAddress(), "Address");
        validateField(userBiodata.getCity(), "City");
        validateField(userBiodata.getProvince(), "Province");
        validateField(userBiodata.getMonthlyIncome(), "Monthly income");
        validateField(userBiodata.getIncomeSource(), "Income source");
        validateField(userBiodata.getOccupation(), "Occupation");
    }
    
    private void validateField(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                "User biodata is incomplete: " + fieldName + " is required.");
        }
    }
    
    private void validateField(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalStateException(
                "User biodata is incomplete: " + fieldName + " is required.");
        }
    }
}
```

### 6.2 RiskValidator

```java
@Component
@RequiredArgsConstructor
public class RiskValidator {
    
    private static final int MAX_OVERDUE_DAYS = 30;
    private static final int MAX_OVERDUE_FOR_HIGH_LOAN_HISTORY = 10;
    private static final int MAX_COMPLETED_LOANS_FOR_RISK = 5;
    private static final double MAX_DEBT_TO_INCOME_RATIO = 10.0;
    private static final BigDecimal MIN_MONTHLY_INCOME = new BigDecimal("3000000");
    
    public void validate(User user, UserBiodata userBiodata, BigDecimal loanAmount) {
        validateOverdueDays(user);
        validateDebtToIncomeRatio(userBiodata, loanAmount);
        validateLoanHistory(user);
        validateMinimumIncome(userBiodata);
    }
    
    private void validateOverdueDays(User user) {
        if (user.getTotalOverdueDays() > MAX_OVERDUE_DAYS) {
            throw new IllegalStateException(String.format(
                "Risk check failed: User has excessive overdue days (%d). Loan application rejected.",
                user.getTotalOverdueDays()));
        }
    }
    
    private void validateDebtToIncomeRatio(UserBiodata userBiodata, BigDecimal loanAmount) {
        if (userBiodata.getMonthlyIncome() != null &&
            loanAmount.doubleValue() > userBiodata.getMonthlyIncome().doubleValue() * MAX_DEBT_TO_INCOME_RATIO) {
            throw new IllegalStateException(
                "Risk check failed: Loan amount exceeds 10x monthly income. Loan application rejected.");
        }
    }
    
    private void validateLoanHistory(User user) {
        if (user.getLoansCompleted() > MAX_COMPLETED_LOANS_FOR_RISK && 
            user.getTotalOverdueDays() > MAX_OVERDUE_FOR_HIGH_LOAN_HISTORY) {
            throw new IllegalStateException(
                "Risk check failed: User has high loan history with overdue records. Loan application rejected.");
        }
    }
    
    private void validateMinimumIncome(UserBiodata userBiodata) {
        if (userBiodata.getMonthlyIncome() != null &&
            userBiodata.getMonthlyIncome().compareTo(MIN_MONTHLY_INCOME) < 0) {
            throw new IllegalStateException(
                "Risk check failed: Monthly income below minimum requirement (Rp 3,000,000). Loan application rejected.");
        }
    }
}
```

### 6.3 BranchMapper

```java
@Component
public class BranchMapper {
    
    public BranchResponse toResponse(Branch branch) {
        if (branch == null) return null;
        return BranchResponse.builder()
            .id(branch.getId())
            .name(branch.getName())
            .address(branch.getAddress())
            .city(branch.getCity())
            .state(branch.getState())
            .zipCode(branch.getZipCode())
            .phone(branch.getPhone())
            .longitude(branch.getLongitude())
            .latitude(branch.getLatitude())
            .build();
    }
    
    public List<BranchResponse> toResponseList(List<Branch> branches) {
        if (branches == null) return List.of();
        return branches.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
}
```

---

## 7. Kesimpulan

Project ini memiliki fondasi arsitektur yang sangat baik dengan penerapan Use Case Pattern. Namun, terdapat duplikasi kode yang signifikan terutama pada:

1. **Validasi** (UserBiodata, Risk Conditions)
2. **Mapping** (Branch, Permission, Role responses)
3. **Business Logic** (Plafond calculation)
4. **Entity Creation** (ApprovalHistory)

Dengan refactoring yang direkomendasikan, kode akan menjadi:
- **Lebih maintainable**: Perubahan hanya perlu dilakukan di satu tempat
- **Lebih readable**: Logic terpusat dan lebih mudah dipahami
- **Lebih testable**: Setiap component dapat di-test secara terpisah
- **Lebih konsisten**: Format dan pola yang seragam

**Estimasi Effort:** 1-2 hari untuk refactoring semua duplikasi kode dengan prioritas HIGH dan MEDIUM.
