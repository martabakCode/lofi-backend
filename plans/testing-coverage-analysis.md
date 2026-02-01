# Testing Coverage Analysis Report

**Project**: Lofi Backend  
**Testing Framework**: JUnit 5 + Mockito  
**Analysis Date**: 2026-02-01

---

## Executive Summary

Analisis ini mengevaluasi cakupan testing unit pada komponen DTO, Mapper, Security, dan Service dalam project Lofi Backend.

|    Layer     | Total Files | Tested | Coverage |
|--------------|-------------|--------|----------|
| DTO Request  | 24          | 2      | 8.3%     |
| DTO Response | 31          | 1      | 3.2%     |
| Mapper       | 2           | 2      | 100%     |
| Security     | 9           | 2      | 22.2%    |
| Service Impl | 13          | 2      | 15.4%    |
| Use Cases    | 45+         | 5      | ~11%     |
| Controller   | 12          | 12     | 100%     |

---

## 1. DTO Testing Coverage

### 1.1 Request DTOs

**Location**: `src/main/java/com/lofi/lofiapps/dto/request/`

| No |               File               |   Status   |            Test File            |
|----|----------------------------------|------------|---------------------------------|
| 1  | `LoginRequest.java`              | Tested     | `RequestDtoValidationTest.java` |
| 2  | `RegisterRequest.java`           | Tested     | `RequestDtoValidationTest.java` |
| 3  | `AssignPermissionsRequest.java`  | NOT TESTED | -                               |
| 4  | `AssignRolesRequest.java`        | NOT TESTED | -                               |
| 5  | `ChangePasswordRequest.java`     | NOT TESTED | -                               |
| 6  | `CreateBranchRequest.java`       | NOT TESTED | -                               |
| 7  | `CreateProductRequest.java`      | NOT TESTED | -                               |
| 8  | `CreateRoleRequest.java`         | NOT TESTED | -                               |
| 9  | `CreateUserRequest.java`         | NOT TESTED | -                               |
| 10 | `DisbursementRequest.java`       | NOT TESTED | -                               |
| 11 | `ForgotPasswordRequest.java`     | NOT TESTED | -                               |
| 12 | `GoogleLoginRequest.java`        | NOT TESTED | -                               |
| 13 | `LoanCriteria.java`              | NOT TESTED | -                               |
| 14 | `LoanRequest.java`               | NOT TESTED | -                               |
| 15 | `MarketingApplyLoanRequest.java` | NOT TESTED | -                               |
| 16 | `PresignUploadRequest.java`      | NOT TESTED | -                               |
| 17 | `RefreshTokenRequest.java`       | NOT TESTED | -                               |
| 18 | `RejectLoanRequest.java`         | NOT TESTED | -                               |
| 19 | `ResetPasswordRequest.java`      | NOT TESTED | -                               |
| 20 | `ResolveRiskRequest.java`        | NOT TESTED | -                               |
| 21 | `ReviewLoanRequest.java`         | NOT TESTED | -                               |
| 22 | `UpdateProductRequest.java`      | NOT TESTED | -                               |
| 23 | `UpdateProfileRequest.java`      | NOT TESTED | -                               |
| 24 | `UpdateRoleRequest.java`         | NOT TESTED | -                               |
| 25 | `UserCriteria.java`              | NOT TESTED | -                               |

**Coverage**: 2/25 (8%)

### 1.2 Response DTOs

**Location**: `src/main/java/com/lofi/lofiapps/dto/response/`

|  No  |           File           |   Status   |
|------|--------------------------|------------|
| 1    | `ApiResponse.java`       | Tested     |
| 2-31 | 30 Response DTOs lainnya | NOT TESTED |

**List Response DTOs yang belum di-test**:
- `AuditLogResponse.java`
- `BackOfficeRiskEvaluationResponse.java`
- `BranchManagerSupportResponse.java`
- `BranchResponse.java`
- `DocumentResponse.java`
- `DocumentValidationResponse.java`
- `DownloadDocumentResponse.java`
- `EligibilityAnalysisResponse.java`
- `EmailDraftResponse.java`
- `LoanAnalysisResponse.java`
- `LoanKpiResponse.java`
- `LoanResponse.java`
- `LoanRiskResponse.java`
- `LoginResponse.java`
- `MarketingLoanReviewResponse.java`
- `NotificationGenerationResponse.java`
- `NotificationResponse.java`
- `PagedResponse.java`
- `PermissionResponse.java`
- `PresignUploadResponse.java`
- `ProductRecommendationResponse.java`
- `ProductResponse.java`
- `RegisterResponse.java`
- `RiskItem.java`
- `RoleResponse.java`
- `SlaReportResponse.java`
- `UserInfoResponse.java`
- `UserProfileResponse.java`
- `UserSummaryResponse.java`

**Coverage**: 1/31 (3.2%)

---

## 2. Mapper Testing Coverage

**Location**: `src/main/java/com/lofi/lofiapps/mapper/`

| No |          File           | Status |          Test File          |
|----|-------------------------|--------|-----------------------------|
| 1  | `LoanDtoMapper.java`    | Tested | `LoanDtoMapperTest.java`    |
| 2  | `ProductDtoMapper.java` | Tested | `ProductDtoMapperTest.java` |

**Coverage**: 2/2 (100%)

**Note**: Mapper layer memiliki testing coverage yang lengkap.

---

## 3. Security Testing Coverage

### 3.1 JWT Components

**Location**: `src/main/java/com/lofi/lofiapps/security/jwt/`

| No |           File           |   Status   |         Test File          |
|----|--------------------------|------------|----------------------------|
| 1  | `JwtUtils.java`          | Tested     | `JwtUtilsTest.java`        |
| 2  | `AuthTokenFilter.java`   | Tested     | `AuthTokenFilterTest.java` |
| 3  | `AuthEntryPointJwt.java` | NOT TESTED | -                          |

### 3.2 Security Service

**Location**: `src/main/java/com/lofi/lofiapps/security/service/`

| No |             File              |   Status   |             Test File             |
|----|-------------------------------|------------|-----------------------------------|
| 1  | `UserPrincipal.java`          | Tested     | `UserPrincipalTest.java`          |
| 2  | `GoogleAuthService.java`      | Tested     | `GoogleAuthServiceTest.java`      |
| 3  | `GoogleUser.java`             | NOT TESTED | -                                 |
| 4  | `TokenBlacklistService.java`  | Tested     | `TokenBlacklistServiceTest.java`  |
| 5  | `UserDetailsServiceImpl.java` | Tested     | `UserDetailsServiceImplTest.java` |

### 3.3 Security Filters & Interceptors

**Location**: `src/main/java/com/lofi/lofiapps/security/`

| No |               File               |   Status   |
|----|----------------------------------|------------|
| 1  | `RateLimitFilter.java`           | NOT TESTED |
| 2  | `IdempotencyInterceptor.java`    | NOT TESTED |
| 3  | `IdempotencyService.java`        | NOT TESTED |
| 4  | `IdempotencyResponseAdvice.java` | NOT TESTED |
| 5  | `RequireIdempotency.java`        | NOT TESTED |

**Coverage**: 2/9 (22.2%)

---

## 4. Service Testing Coverage

### 4.1 Service Implementations

**Location**: `src/main/java/com/lofi/lofiapps/service/impl/`

| No |              File              |   Status   |         Test File          |
|----|--------------------------------|------------|----------------------------|
| 1  | `AuthServiceImpl.java`         | Tested     | `AuthServiceImplTest.java` |
| 2  | `UserServiceImpl.java`         | Tested     | `UserServiceImplTest.java` |
| 3  | `AdminServiceImpl.java`        | NOT TESTED | -                          |
| 4  | `AuditLogServiceImpl.java`     | NOT TESTED | -                          |
| 5  | `AuditServiceImpl.java`        | NOT TESTED | -                          |
| 6  | `DocumentServiceImpl.java`     | NOT TESTED | -                          |
| 7  | `LoanServiceImpl.java`         | NOT TESTED | -                          |
| 8  | `NotificationServiceImpl.java` | NOT TESTED | -                          |
| 9  | `ProductServiceImpl.java`      | NOT TESTED | -                          |
| 10 | `RbacServiceImpl.java`         | NOT TESTED | -                          |
| 11 | `ReportServiceImpl.java`       | NOT TESTED | -                          |
| 12 | `RiskCheckServiceImpl.java`    | NOT TESTED | -                          |
| 13 | `RiskServiceImpl.java`         | NOT TESTED | -                          |

**Coverage**: 2/13 (15.4%)

### 4.2 Use Cases

**Location**: `src/main/java/com/lofi/lofiapps/service/impl/usecase/`

#### Auth Use Cases

| No |             File             |   Status   |           Test File            |
|----|------------------------------|------------|--------------------------------|
| 1  | `LoginUseCase.java`          | Tested     | `LoginUseCaseTest.java`        |
| 2  | `RegisterUseCase.java`       | Tested     | `RegisterUseCaseTest.java`     |
| 3  | `LogoutUseCase.java`         | Tested     | `LogoutUseCaseTest.java`       |
| 4  | `RefreshTokenUseCase.java`   | Tested     | `RefreshTokenUseCaseTest.java` |
| 5  | `ChangePasswordUseCase.java` | NOT TESTED | -                              |
| 6  | `ForgotPasswordUseCase.java` | NOT TESTED | -                              |
| 7  | `GoogleLoginUseCase.java`    | Tested     | `GoogleLoginUseCaseTest.java`  |
| 8  | `ResetPasswordUseCase.java`  | NOT TESTED | -                              |

#### Loan Use Cases

| No |                  File                  |   Status   |          Test File           |
|----|----------------------------------------|------------|------------------------------|
| 1  | `SubmitLoanUseCase.java`               | Tested     | `SubmitLoanUseCaseTest.java` |
| 2  | `AnalyzeLoanUseCase.java`              | NOT TESTED | -                            |
| 3  | `ApplyLoanUseCase.java`                | NOT TESTED | -                            |
| 4  | `ApproveLoanUseCase.java`              | NOT TESTED | -                            |
| 5  | `BackOfficeRiskEvaluationUseCase.java` | NOT TESTED | -                            |
| 6  | `BranchManagerSupportUseCase.java`     | NOT TESTED | -                            |
| 7  | `CancelLoanUseCase.java`               | NOT TESTED | -                            |
| 8  | `CompleteLoanUseCase.java`             | NOT TESTED | -                            |
| 9  | `DisburseLoanUseCase.java`             | NOT TESTED | -                            |
| 10 | `GetLoanDetailUseCase.java`            | NOT TESTED | -                            |
| 11 | `GetLoansUseCase.java`                 | NOT TESTED | -                            |
| 12 | `MarketingApplyLoanUseCase.java`       | NOT TESTED | -                            |
| 13 | `MarketingReviewLoanUseCase.java`      | NOT TESTED | -                            |
| 14 | `RejectLoanUseCase.java`               | NOT TESTED | -                            |
| 15 | `ReviewLoanUseCase.java`               | NOT TESTED | -                            |
| 16 | `RollbackLoanUseCase.java`             | NOT TESTED | -                            |

#### RBAC Use Cases

| No |                  File                  |   Status   |
|----|----------------------------------------|------------|
| 1  | `AssignPermissionsToRoleUseCase.java`  | NOT TESTED |
| 2  | `AssignRolesToUserUseCase.java`        | NOT TESTED |
| 3  | `CreateBranchUseCase.java`             | NOT TESTED |
| 4  | `CreateRoleUseCase.java`               | NOT TESTED |
| 5  | `DeleteBranchUseCase.java`             | NOT TESTED |
| 6  | `DeleteRoleUseCase.java`               | NOT TESTED |
| 7  | `GetBranchesUseCase.java`              | NOT TESTED |
| 8  | `GetPermissionsUseCase.java`           | NOT TESTED |
| 9  | `GetRolePermissionsUseCase.java`       | NOT TESTED |
| 10 | `GetRolesUseCase.java`                 | NOT TESTED |
| 11 | `GetUserRolesUseCase.java`             | NOT TESTED |
| 12 | `RemovePermissionFromRoleUseCase.java` | NOT TESTED |
| 13 | `RemoveRoleFromUserUseCase.java`       | NOT TESTED |
| 14 | `UpdateBranchUseCase.java`             | NOT TESTED |
| 15 | `UpdateRoleUseCase.java`               | NOT TESTED |

#### User Use Cases

| No |                File                |   Status   |
|----|------------------------------------|------------|
| 1  | `CreateUserUseCase.java`           | NOT TESTED |
| 2  | `DeleteUserUseCase.java`           | NOT TESTED |
| 3  | `GetProfilePhotoUseCase.java`      | NOT TESTED |
| 4  | `GetUserProfileUseCase.java`       | NOT TESTED |
| 5  | `GetUsersUseCase.java`             | NOT TESTED |
| 6  | `UpdateProfilePictureUseCase.java` | NOT TESTED |
| 7  | `UpdateProfileUseCase.java`        | NOT TESTED |

#### Document Use Cases

| No |                 File                  |   Status   |
|----|---------------------------------------|------------|
| 1  | `GetPresignedDownloadUrlUseCase.java` | NOT TESTED |
| 2  | `PresignUploadUseCase.java`           | NOT TESTED |

#### Notification Use Cases

| No |              File              |   Status   |
|----|--------------------------------|------------|
| 1  | `GetNotificationsUseCase.java` | NOT TESTED |

#### Product Use Cases

| No |              File              |   Status   |
|----|--------------------------------|------------|
| 1  | `RecommendProductUseCase.java` | NOT TESTED |

#### Report Use Cases

| No |            File            |   Status   |
|----|----------------------------|------------|
| 1  | `ExcelExportService.java`  | NOT TESTED |
| 2  | `GetLoanKpisUseCase.java`  | NOT TESTED |
| 3  | `GetSlaReportUseCase.java` | NOT TESTED |

#### Risk Use Cases

| No |            File            |   Status   |
|----|----------------------------|------------|
| 1  | `GetLoanRisksUseCase.java` | NOT TESTED |
| 2  | `ResolveRiskUseCase.java`  | NOT TESTED |

#### Other Components

| No |             File              |   Status   |
|----|-------------------------------|------------|
| 1  | `PlafondCalculator.java`      | NOT TESTED |
| 2  | `ApprovalHistoryFactory.java` | NOT TESTED |
| 3  | `BranchMapper.java`           | NOT TESTED |
| 4  | `PermissionMapper.java`       | NOT TESTED |
| 5  | `RoleMapper.java`             | NOT TESTED |
| 6  | `R2StorageService.java`       | NOT TESTED |
| 7  | `RiskValidator.java`          | NOT TESTED |
| 8  | `UserBiodataValidator.java`   | NOT TESTED |

**Use Case Coverage**: 5/45+ (~11%)

---

## 5. Controller Testing Coverage

**Location**: `src/test/java/com/lofi/lofiapps/controller/`

| No |                 File                  | Status |
|----|---------------------------------------|--------|
| 1  | `AuditControllerTest.java`            | Tested |
| 2  | `AuthControllerTest.java`             | Tested |
| 3  | `DocumentControllerTest.java`         | Tested |
| 4  | `LoanControllerTest.java`             | Tested |
| 5  | `MetadataControllerTest.java`         | Tested |
| 6  | `NotificationControllerTest.java`     | Tested |
| 7  | `NotificationTestControllerTest.java` | Tested |
| 8  | `ProductControllerTest.java`          | Tested |
| 9  | `RbacControllerTest.java`             | Tested |
| 10 | `ReportControllerTest.java`           | Tested |
| 11 | `RiskControllerTest.java`             | Tested |
| 12 | `UserControllerTest.java`             | Tested |

**Coverage**: 12/12 (100%)

---

## 6. Recommendations

### Priority Tinggi (Critical Missing Tests)

#### Security Components

1. `AuthTokenFilter.java` - Filter untuk validasi JWT token
2. `TokenBlacklistService.java` - Service untuk blacklist token
3. `UserDetailsServiceImpl.java` - Service untuk load user details
4. `GoogleAuthService.java` - Service untuk Google OAuth

#### Critical Use Cases

1. `GoogleLoginUseCase.java` - Login dengan Google
2. `ApplyLoanUseCase.java` - Apply pinjaman
3. `ApproveLoanUseCase.java` - Approve pinjaman
4. `DisburseLoanUseCase.java` - Disburse pinjaman
5. `CreateBranchUseCase.java` - Create branch
6. `CreateRoleUseCase.java` - Create role

#### Service Implementations

1. `LoanServiceImpl.java` - Core business logic
2. `DocumentServiceImpl.java` - Document handling
3. `RbacServiceImpl.java` - RBAC operations

### Priority Medium

#### DTO Validation

1. `CreateUserRequest.java`
2. `CreateProductRequest.java`
3. `LoanRequest.java`
4. `UpdateProfileRequest.java`

#### Factory & Validator

1. `ApprovalHistoryFactory.java`
2. `RiskValidator.java`
3. `UserBiodataValidator.java`

### Priority Low

#### Response DTOs

Response DTOs umumnya hanya data holder, testing priority lebih rendah kecuali memiliki business logic.

---

## 7. Conclusion

### Strengths

- **Mapper Layer**: 100% coverage
- **Controller Layer**: 100% coverage
- **Auth Use Cases**: Good coverage untuk core authentication flow

### Weaknesses

- **DTO Layer**: Sangat rendah (5% overall)
- **Security Layer**: Rendah (22%), banyak critical components belum di-test
- **Service Layer**: Rendah (15%), terutama use cases

### Action Items

1. Prioritaskan testing untuk Security components
2. Tambahkan testing untuk critical business use cases (Loan, RBAC)
3. Pertimbangkan testing untuk DTO validation
4. Maintain 100% coverage untuk Controller dan Mapper

---

*Generated by Architect Mode - Kilo Code*
