# Testing Implementation Plan

**Project**: Lofi Backend  
**Framework**: JUnit 5 + Mockito  
**Created**: 2026-02-01  
**Based on**: [Testing Coverage Analysis](testing-coverage-analysis.md)

---

## Table of Contents

1. [Overview](#overview)
2. [Testing Strategy](#testing-strategy)
3. [Priority Matrix](#priority-matrix)
4. [Implementation Roadmap](#implementation-roadmap)
5. [Security Components Testing Plan](#security-components-testing-plan)
6. [Service Implementation Testing Plan](#service-implementation-testing-plan)
7. [Use Cases Testing Plan](#use-cases-testing-plan)
8. [DTO Validation Testing Plan](#dto-validation-testing-plan)
9. [Best Practices & Patterns](#best-practices--patterns)

---

## Overview

Dokumen ini berisi rencana implementasi unit test komprehensif untuk Lofi Backend Project berdasarkan hasil analisis coverage yang ada. Target akhir adalah mencapai minimum 70% coverage untuk business logic layer dan 100% coverage untuk critical security components.

### Current State

|     Layer      | Current Coverage | Target Coverage |
|----------------|------------------|-----------------|
| Controller     | 100%             | 100%            |
| Mapper         | 100%             | 100%            |
| Security       | 22.2%            | 90%             |
| Service Impl   | 15.4%            | 70%             |
| Use Cases      | ~11%             | 70%             |
| DTO Validation | 5%               | 60%             |

---

## Testing Strategy

### 1. Testing Pyramid

```
      /\
     /  \     E2E Tests (Integration)
    /----\    
   /      \   Service Tests (Integration)
  /--------\  
 /          \ Unit Tests (Mockito/JUnit)
/------------\
```

Fokus utama plan ini adalah **Unit Tests** dengan mocking dependencies.

### 2. Test Categories

|     Category     |               Description               |        Tools         |
|------------------|-----------------------------------------|----------------------|
| Unit Test        | Test individual components in isolation | JUnit 5, Mockito     |
| Integration Test | Test component interactions             | @SpringBootTest      |
| Validation Test  | Test DTO validation constraints         | Jakarta Validator    |
| Security Test    | Test authentication & authorization     | Spring Security Test |

### 3. Mocking Strategy

```java
// Use @ExtendWith(MockitoExtension.class) for pure unit tests
@ExtendWith(MockitoExtension.class)
class ExampleTest {
    
    @Mock
    private Dependency dependency;
    
    @InjectMocks
    private ClassUnderTest classUnderTest;
    
    @Test
    void testMethod() {
        // Given
        when(dependency.method()).thenReturn(expected);
        
        // When
        Result result = classUnderTest.execute();
        
        // Then
        assertEquals(expected, result);
        verify(dependency).method();
    }
}
```

---

## Priority Matrix

### Priority 1: Critical (Security & Core Business)

|       Component        |            Reason             | Effort |  Impact  |
|------------------------|-------------------------------|--------|----------|
| AuthTokenFilter        | JWT validation, security gate | Medium | Critical |
| TokenBlacklistService  | Session management            | Low    | Critical |
| UserDetailsServiceImpl | Authentication                | Low    | Critical |
| GoogleAuthService      | OAuth security                | Medium | High     |
| ApplyLoanUseCase       | Core business logic           | High   | Critical |
| ApproveLoanUseCase     | Core business logic           | High   | Critical |

### Priority 2: High (Business Logic)

|      Component      |     Reason      | Effort | Impact |
|---------------------|-----------------|--------|--------|
| LoanServiceImpl     | Core service    | High   | High   |
| DocumentServiceImpl | File handling   | Medium | High   |
| RbacServiceImpl     | Authorization   | Medium | High   |
| CreateBranchUseCase | RBAC operations | Low    | Medium |
| CreateRoleUseCase   | RBAC operations | Low    | Medium |

### Priority 3: Medium (Supporting Components)

|        Component        |      Reason      | Effort | Impact |
|-------------------------|------------------|--------|--------|
| DTO Validations         | Input validation | Medium | Medium |
| NotificationServiceImpl | Notifications    | Medium | Low    |
| ReportServiceImpl       | Reporting        | Medium | Low    |

### Priority 4: Low (Data Holders)

|       Component        |      Reason       | Effort | Impact |
|------------------------|-------------------|--------|--------|
| Response DTOs          | Data holders only | Low    | Low    |
| Simple Getters/Setters | Auto-generated    | Low    | Low    |

---

## Implementation Roadmap

### Phase 1: Security Foundation (Week 1)

**Goal**: Secure the authentication & authorization layer

- [ ] AuthTokenFilterTest
- [ ] TokenBlacklistServiceTest
- [ ] UserDetailsServiceImplTest
- [ ] GoogleAuthServiceTest
- [ ] AuthEntryPointJwtTest

### Phase 2: Core Business Logic (Week 2-3)

**Goal**: Test critical loan processing use cases

- [ ] ApplyLoanUseCaseTest
- [ ] ApproveLoanUseCaseTest
- [ ] DisburseLoanUseCaseTest
- [ ] RejectLoanUseCaseTest
- [ ] SubmitLoanUseCaseTest (enhance existing)

### Phase 3: RBAC & User Management (Week 4)

**Goal**: Test authorization and user management

- [ ] CreateBranchUseCaseTest
- [ ] UpdateBranchUseCaseTest
- [ ] DeleteBranchUseCaseTest
- [ ] CreateRoleUseCaseTest
- [ ] AssignRolesToUserUseCaseTest
- [ ] AssignPermissionsToRoleUseCaseTest

### Phase 4: Service Layer (Week 5)

**Goal**: Test service implementations

- [ ] LoanServiceImplTest
- [ ] DocumentServiceImplTest
- [ ] RbacServiceImplTest
- [ ] NotificationServiceImplTest

### Phase 5: DTO Validation (Week 6)

**Goal**: Validate all request DTOs

- [ ] CreateUserRequestTest
- [ ] CreateProductRequestTest
- [ ] LoanRequestTest
- [ ] UpdateProfileRequestTest
- [ ] CreateBranchRequestTest
- [ ] CreateRoleRequestTest

---

## Security Components Testing Plan

### 1. AuthTokenFilter

**Location**: `src/main/java/com/lofi/lofiapps/security/jwt/AuthTokenFilter.java`

**Test Scenarios**:

```java
@ExtendWith(MockitoExtension.class)
class AuthTokenFilterTest {

    @Test
    @DisplayName("Should authenticate user with valid JWT token")
    void doFilterInternal_ValidToken_ShouldAuthenticate() throws Exception {
        // Test implementation
    }

    @Test
    @DisplayName("Should reject request with blacklisted token")
    void doFilterInternal_BlacklistedToken_ShouldReject() throws Exception {
        // Test implementation
    }

    @Test
    @DisplayName("Should reject token invalidated by admin force logout")
    void doFilterInternal_ForceLogoutToken_ShouldReject() throws Exception {
        // Test implementation
    }

    @Test
    @DisplayName("Should reject request with missing email claim")
    void doFilterInternal_MissingEmailClaim_ShouldReject() throws Exception {
        // Test implementation
    }

    @Test
    @DisplayName("Should reject request with invalid JWT token")
    void doFilterInternal_InvalidToken_ShouldReject() throws Exception {
        // Test implementation
    }

    @Test
    @DisplayName("Should allow request without token for public endpoints")
    void doFilterInternal_NoToken_PublicEndpoint_ShouldAllow() throws Exception {
        // Test implementation
    }
}
```

**Dependencies to Mock**:
- `JwtUtils` - Token validation and parsing
- `UserDetailsServiceImpl` - User loading
- `TokenBlacklistService` - Blacklist checking
- `HttpServletRequest` / `HttpServletResponse` / `FilterChain` - Servlet objects

**Test File**: `src/test/java/com/lofi/lofiapps/security/jwt/AuthTokenFilterTest.java`

---

### 2. TokenBlacklistService

**Location**: `src/main/java/com/lofi/lofiapps/security/service/TokenBlacklistService.java`

**Test Scenarios**:

```java
@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceTest {

    @Test
    @DisplayName("Should blacklist token with expiration")
    void blacklistToken_ShouldStoreInRedis() {
        // Test implementation
    }

    @Test
    @DisplayName("Should return true for blacklisted token")
    void isBlacklisted_BlacklistedToken_ShouldReturnTrue() {
        // Test implementation
    }

    @Test
    @DisplayName("Should return false for non-blacklisted token")
    void isBlacklisted_NonBlacklistedToken_ShouldReturnFalse() {
        // Test implementation
    }

    @Test
    @DisplayName("Should store force logout timestamp")
    void forceLogoutUser_ShouldStoreTimestamp() {
        // Test implementation
    }

    @Test
    @DisplayName("Should return forced logout timestamp for user")
    void getForcedLogoutTimestamp_ExistingUser_ShouldReturnTimestamp() {
        // Test implementation
    }

    @Test
    @DisplayName("Should return 0 for user without force logout")
    void getForcedLogoutTimestamp_NonExistingUser_ShouldReturnZero() {
        // Test implementation
    }
}
```

**Dependencies to Mock**:
- `StringRedisTemplate` - Redis operations

**Test File**: `src/test/java/com/lofi/lofiapps/security/service/TokenBlacklistServiceTest.java`

---

### 3. UserDetailsServiceImpl

**Location**: `src/main/java/com/lofi/lofiapps/security/service/UserDetailsServiceImpl.java`

**Test Scenarios**:

```java
@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Test
    @DisplayName("Should load user by email successfully")
    void loadUserByUsername_ExistingUser_ShouldReturnUserPrincipal() {
        // Test implementation
    }

    @Test
    @DisplayName("Should throw exception for non-existing user")
    void loadUserByUsername_NonExistingUser_ShouldThrowException() {
        // Test implementation
    }

    @Test
    @DisplayName("Should load user with correct authorities")
    void loadUserByUsername_UserWithRoles_ShouldReturnCorrectAuthorities() {
        // Test implementation
    }
}
```

**Dependencies to Mock**:
- `UserRepository` - User data access

**Test File**: `src/test/java/com/lofi/lofiapps/security/service/UserDetailsServiceImplTest.java`

---

### 4. GoogleAuthService

**Location**: `src/main/java/com/lofi/lofiapps/security/service/GoogleAuthService.java`

**Test Scenarios**:

```java
@ExtendWith(MockitoExtension.class)
class GoogleAuthServiceTest {

    @Test
    @DisplayName("Should verify valid Google token")
    void verifyGoogleToken_ValidToken_ShouldReturnGoogleUser() {
        // Test implementation
    }

    @Test
    @DisplayName("Should return null for invalid Google token")
    void verifyGoogleToken_InvalidToken_ShouldReturnNull() {
        // Test implementation
    }

    @Test
    @DisplayName("Should handle Firebase token verification")
    void verifyGoogleToken_FirebaseToken_ShouldReturnGoogleUser() {
        // Test implementation
    }
}
```

**Dependencies to Mock**:
- Firebase Admin SDK components

**Test File**: `src/test/java/com/lofi/lofiapps/security/service/GoogleAuthServiceTest.java`

---

### 5. AuthEntryPointJwt

**Location**: `src/main/java/com/lofi/lofiapps/security/jwt/AuthEntryPointJwt.java`

**Test Scenarios**:

```java
@ExtendWith(MockitoExtension.class)
class AuthEntryPointJwtTest {

    @Test
    @DisplayName("Should return 401 for unauthorized request")
    void commence_UnauthorizedRequest_ShouldReturn401() throws Exception {
        // Test implementation
    }

    @Test
    @DisplayName("Should return proper error message in response")
    void commence_UnauthorizedRequest_ShouldReturnErrorMessage() throws Exception {
        // Test implementation
    }
}
```

**Test File**: `src/test/java/com/lofi/lofiapps/security/jwt/AuthEntryPointJwtTest.java`

---

## Service Implementation Testing Plan

### 1. LoanServiceImpl

**Location**: `src/main/java/com/lofi/lofiapps/service/impl/LoanServiceImpl.java`

**Test Scenarios**:

```java
@ExtendWith(MockitoExtension.class)
class LoanServiceImplTest {

    // Submit Loan Tests
    @Test
    @DisplayName("Should submit loan successfully")
    void submitLoan_ValidRequest_ShouldSucceed() {
        // Test implementation
    }

    @Test
    @DisplayName("Should throw exception for inactive user")
    void submitLoan_InactiveUser_ShouldThrowException() {
        // Test implementation
    }

    // Get Loans Tests
    @Test
    @DisplayName("Should get loans with pagination")
    void getLoans_WithCriteria_ShouldReturnPagedResponse() {
        // Test implementation
    }

    @Test
    @DisplayName("Should filter loans by status")
    void getLoans_ByStatus_ShouldReturnFilteredResults() {
        // Test implementation
    }

    // Approve Loan Tests
    @Test
    @DisplayName("Should approve loan with valid permissions")
    void approveLoan_ValidRequest_ShouldSucceed() {
        // Test implementation
    }

    @Test
    @DisplayName("Should throw exception for invalid loan status transition")
    void approveLoan_InvalidStatusTransition_ShouldThrowException() {
        // Test implementation
    }

    // Reject Loan Tests
    @Test
    @DisplayName("Should reject loan with valid reason")
    void rejectLoan_ValidRequest_ShouldSucceed() {
        // Test implementation
    }

    // Disburse Loan Tests
    @Test
    @DisplayName("Should disburse loan successfully")
    void disburseLoan_ValidRequest_ShouldSucceed() {
        // Test implementation
    }
}
```

**Dependencies to Mock**:
- `LoanRepository`
- `UserRepository`
- `ProductRepository`
- `ApprovalHistoryRepository`
- `LoanDtoMapper`
- `NotificationService`
- `AuditLogService`

**Test File**: `src/test/java/com/lofi/lofiapps/service/impl/LoanServiceImplTest.java`

---

### 2. DocumentServiceImpl

**Location**: `src/main/java/com/lofi/lofiapps/service/impl/DocumentServiceImpl.java`

**Test Scenarios**:

```java
@ExtendWith(MockitoExtension.class)
class DocumentServiceImplTest {

    @Test
    @DisplayName("Should upload document successfully")
    void uploadDocument_ValidFile_ShouldSucceed() {
        // Test implementation
    }

    @Test
    @DisplayName("Should throw exception for invalid file type")
    void uploadDocument_InvalidFileType_ShouldThrowException() {
        // Test implementation
    }

    @Test
    @DisplayName("Should get presigned download URL")
    void getDownloadUrl_ValidDocument_ShouldReturnUrl() {
        // Test implementation
    }

    @Test
    @DisplayName("Should delete document successfully")
    void deleteDocument_ValidId_ShouldSucceed() {
        // Test implementation
    }

    @Test
    @DisplayName("Should get documents by loan ID")
    void getDocumentsByLoanId_ValidLoanId_ShouldReturnDocuments() {
        // Test implementation
    }

    @Test
    @DisplayName("Should validate document successfully")
    void validateDocument_ValidDocument_ShouldSucceed() {
        // Test implementation
    }
}
```

**Dependencies to Mock**:
- `DocumentRepository`
- `LoanRepository`
- `R2StorageService`
- `DocumentValidator`

**Test File**: `src/test/java/com/lofi/lofiapps/service/impl/DocumentServiceImplTest.java`

---

### 3. RbacServiceImpl

**Location**: `src/main/java/com/lofi/lofiapps/service/impl/RbacServiceImpl.java`

**Test Scenarios**:

```java
@ExtendWith(MockitoExtension.class)
class RbacServiceImplTest {

    // Role Tests
    @Test
    @DisplayName("Should get all roles")
    void getRoles_ShouldReturnAllRoles() {
        // Test implementation
    }

    @Test
    @DisplayName("Should create role successfully")
    void createRole_ValidRequest_ShouldSucceed() {
        // Test implementation
    }

    @Test
    @DisplayName("Should update role successfully")
    void updateRole_ValidRequest_ShouldSucceed() {
        // Test implementation
    }

    @Test
    @DisplayName("Should delete role successfully")
    void deleteRole_ValidId_ShouldSucceed() {
        // Test implementation
    }

    // Permission Tests
    @Test
    @DisplayName("Should get all permissions")
    void getPermissions_ShouldReturnAllPermissions() {
        // Test implementation
    }

    @Test
    @DisplayName("Should assign permissions to role")
    void assignPermissionsToRole_ValidRequest_ShouldSucceed() {
        // Test implementation
    }

    // User Role Tests
    @Test
    @DisplayName("Should assign roles to user")
    void assignRolesToUser_ValidRequest_ShouldSucceed() {
        // Test implementation
    }

    @Test
    @DisplayName("Should get user roles")
    void getUserRoles_ValidUserId_ShouldReturnRoles() {
        // Test implementation
    }

    // Branch Tests
    @Test
    @DisplayName("Should get all branches")
    void getBranches_ShouldReturnAllBranches() {
        // Test implementation
    }

    @Test
    @DisplayName("Should create branch successfully")
    void createBranch_ValidRequest_ShouldSucceed() {
        // Test implementation
    }
}
```

**Dependencies to Mock**:
- All Use Case dependencies (delegation pattern)

**Test File**: `src/test/java/com/lofi/lofiapps/service/impl/RbacServiceImplTest.java`

---

## Use Cases Testing Plan

### 1. ApplyLoanUseCase

**Location**: `src/main/java/com/lofi/lofiapps/service/impl/usecase/loan/ApplyLoanUseCase.java`

**Test Scenarios**:

```java
@ExtendWith(MockitoExtension.class)
class ApplyLoanUseCaseTest {

    @Test
    @DisplayName("Should apply loan successfully for active user with complete profile")
    void execute_ValidRequest_ShouldSucceed() {
        // Test implementation
    }

    @Test
    @DisplayName("Should throw exception when loan amount is null")
    void execute_NullLoanAmount_ShouldThrowException() {
        // Test implementation
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void execute_UserNotFound_ShouldThrowException() {
        // Test implementation
    }

    @Test
    @DisplayName("Should throw exception when user is inactive")
    void execute_InactiveUser_ShouldThrowException() {
        // Test implementation
    }

    @Test
    @DisplayName("Should throw exception when profile is incomplete")
    void execute_IncompleteProfile_ShouldThrowException() {
        // Test implementation
    }

    @Test
    @DisplayName("Should throw exception when user is underage")
    void execute_UnderageUser_ShouldThrowException() {
        // Test implementation
    }

    @Test
    @DisplayName("Should throw exception when biodata not found")
    void execute_BiodataNotFound_ShouldThrowException() {
        // Test implementation
    }

    @Test
    @DisplayName("Should throw exception when product not found")
    void execute_ProductNotFound_ShouldThrowException() {
        // Test implementation
    }

    @Test
    @DisplayName("Should throw exception when loan amount exceeds plafond")
    void execute_ExceedsPlafond_ShouldThrowException() {
        // Test implementation
    }

    @Test
    @DisplayName("Should throw exception when risk validation fails")
    void execute_RiskValidationFails_ShouldThrowException() {
        // Test implementation
    }
}
```

**Dependencies to Mock**:
- `LoanRepository`
- `UserRepository`
- `ProductRepository`
- `UserBiodataRepository`
- `LoanDtoMapper`
- `UserBiodataValidator`
- `RiskValidator`
- `PlafondCalculator`
- `ApprovalHistoryFactory`

**Test File**: `src/test/java/com/lofi/lofiapps/service/impl/usecase/loan/ApplyLoanUseCaseTest.java`

---

### 2. ApproveLoanUseCase

**Location**: `src/main/java/com/lofi/lofiapps/service/impl/usecase/loan/ApproveLoanUseCase.java`

**Test Scenarios**:

```java
@ExtendWith(MockitoExtension.class)
class ApproveLoanUseCaseTest {

    @Test
    @DisplayName("Should approve loan at BACK_OFFICE stage successfully")
    void execute_BackOfficeApproval_ShouldSucceed() {
        // Test implementation
    }

    @Test
    @DisplayName("Should approve loan at HEAD_OFFICE stage successfully")
    void execute_HeadOfficeApproval_ShouldSucceed() {
        // Test implementation
    }

    @Test
    @DisplayName("Should throw exception when loan not found")
    void execute_LoanNotFound_ShouldThrowException() {
        // Test implementation
    }

    @Test
    @DisplayName("Should throw exception for invalid status transition")
    void execute_InvalidStatusTransition_ShouldThrowException() {
        // Test implementation
    }

    @Test
    @DisplayName("Should throw exception when user lacks permission")
    void execute_NoPermission_ShouldThrowException() {
        // Test implementation
    }

    @Test
    @DisplayName("Should advance to next approval stage")
    void execute_ValidApproval_ShouldAdvanceStage() {
        // Test implementation
    }
}
```

**Test File**: `src/test/java/com/lofi/lofiapps/service/impl/usecase/loan/ApproveLoanUseCaseTest.java`

---

### 3. CreateBranchUseCase

**Location**: `src/main/java/com/lofi/lofiapps/service/impl/usecase/rbac/CreateBranchUseCase.java`

**Test Scenarios**:

```java
@ExtendWith(MockitoExtension.class)
class CreateBranchUseCaseTest {

    @Test
    @DisplayName("Should create branch successfully")
    void execute_ValidRequest_ShouldSucceed() {
        // Test implementation
    }

    @Test
    @DisplayName("Should save branch with all fields")
    void execute_ValidRequest_ShouldSaveAllFields() {
        // Test implementation
    }

    @Test
    @DisplayName("Should return correct response")
    void execute_ValidRequest_ShouldReturnCorrectResponse() {
        // Test implementation
    }
}
```

**Dependencies to Mock**:
- `BranchRepository`

**Test File**: `src/test/java/com/lofi/lofiapps/service/impl/usecase/rbac/CreateBranchUseCaseTest.java`

---

### 4. CreateRoleUseCase

**Location**: `src/main/java/com/lofi/lofiapps/service/impl/usecase/rbac/CreateRoleUseCase.java`

**Test Scenarios**:

```java
@ExtendWith(MockitoExtension.class)
class CreateRoleUseCaseTest {

    @Test
    @DisplayName("Should create role successfully")
    void execute_ValidRequest_ShouldSucceed() {
        // Test implementation
    }

    @Test
    @DisplayName("Should throw exception when role name already exists")
    void execute_DuplicateName_ShouldThrowException() {
        // Test implementation
    }

    @Test
    @DisplayName("Should create role with permissions")
    void execute_WithPermissions_ShouldAssignPermissions() {
        // Test implementation
    }
}
```

**Test File**: `src/test/java/com/lofi/lofiapps/service/impl/usecase/rbac/CreateRoleUseCaseTest.java`

---

## DTO Validation Testing Plan

### Pattern for DTO Validation Tests

```java
class RequestDtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("CreateUserRequest Validation Tests")
    class CreateUserRequestTests {

        @Test
        @DisplayName("Valid CreateUserRequest should have no violations")
        void validRequest_ShouldHaveNoViolations() {
            // Arrange
            CreateUserRequest request = CreateUserRequest.builder()
                .email("test@example.com")
                .username("testuser")
                .fullName("Test User")
                .password("Password123!")
                .roleIds(Set.of(UUID.randomUUID()))
                .build();

            // Act
            Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

            // Assert
            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("Should reject blank email")
        void blankEmail_ShouldHaveViolation() {
            // Test implementation
        }

        @Test
        @DisplayName("Should reject invalid email format")
        void invalidEmail_ShouldHaveViolation() {
            // Test implementation
        }

        @Test
        @DisplayName("Should reject blank username")
        void blankUsername_ShouldHaveViolation() {
            // Test implementation
        }

        @Test
        @DisplayName("Should reject weak password")
        void weakPassword_ShouldHaveViolation() {
            // Test implementation
        }
    }
}
```

### DTOs to Test

|         DTO          | Priority |      Validation Rules to Test      |
|----------------------|----------|------------------------------------|
| CreateUserRequest    | High     | @NotBlank, @Email, @Size, @Pattern |
| CreateProductRequest | High     | @NotBlank, @Positive, @Min, @Max   |
| LoanRequest          | High     | @NotNull, @Positive, @Min, @Max    |
| UpdateProfileRequest | Medium   | @NotBlank, @Pattern, @Size         |
| CreateBranchRequest  | Medium   | @NotBlank, @Pattern                |
| CreateRoleRequest    | Medium   | @NotBlank, @Size                   |

---

## Best Practices & Patterns

### 1. Test Naming Convention

```java
// Pattern: methodName_StateUnderTest_ExpectedBehavior
@Test
void execute_InvalidToken_ShouldThrowException()

// Pattern: should_ExpectedBehavior_When_StateUnderTest
@Test
void shouldThrowExceptionWhenTokenIsInvalid()

// Using @DisplayName for readable descriptions
@Test
@DisplayName("Should throw AuthenticationException when token is invalid")
void execute_InvalidToken_ShouldThrowException()
```

### 2. AAA Pattern (Arrange-Act-Assert)

```java
@Test
void exampleTest() {
    // Arrange
    when(dependency.method()).thenReturn(expectedValue);
    
    // Act
    Result result = classUnderTest.execute();
    
    // Assert
    assertEquals(expectedValue, result);
}
```

### 3. Given-When-Then Pattern

```java
@Test
void exampleTest() {
    // Given - initial context
    User user = createTestUser();
    when(repository.findById(any())).thenReturn(Optional.of(user));
    
    // When - action performed
    User result = service.getUser(userId);
    
    // Then - expected outcome
    assertNotNull(result);
    assertEquals(user.getEmail(), result.getEmail());
}
```

### 4. Test Data Builders

```java
class TestDataBuilder {
    
    static User createTestUser() {
        return User.builder()
            .id(UUID.randomUUID())
            .email("test@example.com")
            .username("testuser")
            .status(UserStatus.ACTIVE)
            .profileCompleted(true)
            .build();
    }
    
    static LoanRequest createValidLoanRequest() {
        return LoanRequest.builder()
            .loanAmount(BigDecimal.valueOf(10000000))
            .tenor(12)
            .productId(UUID.randomUUID())
            .build();
    }
}
```

### 5. Common Assertions

```java
// Basic assertions
assertEquals(expected, actual);
assertNotNull(object);
assertTrue(condition);
assertFalse(condition);
assertThrows(Exception.class, () -> method.call());

// Mockito verify
verify(mock).method();
verify(mock, times(2)).method();
verify(mock, never()).method();
verifyNoInteractions(mock);

// Collection assertions
assertEquals(2, list.size());
assertTrue(list.contains(item));
```

### 6. Exception Testing

```java
@Test
void method_ShouldThrowException_WhenCondition() {
    // Arrange
    when(repository.findById(any())).thenReturn(Optional.empty());
    
    // Act & Assert
    Exception exception = assertThrows(
        ResourceNotFoundException.class,
        () -> service.getUser(UUID.randomUUID())
    );
    
    assertEquals("User not found", exception.getMessage());
}
```

### 7. Parameterized Tests

```java
@ParameterizedTest
@ValueSource(strings = {"", " ", "invalid-email", "@example.com"})
@DisplayName("Should reject invalid email formats")
void shouldRejectInvalidEmail(String invalidEmail) {
    // Arrange
    request.setEmail(invalidEmail);
    
    // Act
    Set<ConstraintViolation<Request>> violations = validator.validate(request);
    
    // Assert
    assertFalse(violations.isEmpty());
}
```

### 8. Test Lifecycle

```java
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ExampleTest {
    
    @BeforeAll
    void setUpClass() {
        // Runs once before all tests
    }
    
    @BeforeEach
    void setUp() {
        // Runs before each test
    }
    
    @AfterEach
    void tearDown() {
        // Runs after each test
    }
    
    @AfterAll
    void tearDownClass() {
        // Runs once after all tests
    }
}
```

---

## Appendix A: Test File Structure

```
src/test/java/com/lofi/lofiapps/
├── controller/           # Controller tests (already complete)
├── dto/
│   ├── request/         # DTO validation tests
│   └── response/        # Response DTO tests (if needed)
├── mapper/              # Mapper tests (already complete)
├── security/
│   ├── jwt/             # JWT component tests
│   └── service/         # Security service tests
├── service/
│   └── impl/            # Service implementation tests
│       └── usecase/     # Use case tests
│           ├── auth/
│           ├── loan/
│           ├── rbac/
│           └── user/
└── util/                # Test utilities and builders
```

## Appendix B: Running Tests

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=AuthTokenFilterTest

# Run tests with coverage report
./mvnw test jacoco:report

# Run tests matching pattern
./mvnw test -Dtest="*UseCaseTest"

# Run with debug output
./mvnw test -X
```

## Appendix C: Coverage Reporting

```bash
# Generate Jacoco report
./mvnw jacoco:report

# View report at
target/site/jacoco/index.html
```

---

*Generated by Architect Mode - Kilo Code*
