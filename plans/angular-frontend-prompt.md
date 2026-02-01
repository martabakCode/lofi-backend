# Angular Frontend Development Prompt

## Project Context

Sistem backend LOFI (Loan Origination & Financial Intelligence) adalah aplikasi manajemen pinjaman dengan arsitektur Role-Based Access Control (RBAC). Backend dibangun menggunakan Spring Boot dengan fitur lengkap untuk pengajuan, review, approval, dan disbursement pinjaman.

## User Roles

|        Role        |                                      Deskripsi                                       |
|--------------------|--------------------------------------------------------------------------------------|
| **MARKETING**      | Mengajukan pinjaman atas nama customer, review aplikasi pinjaman, melihat SLA report |
| **BRANCH_MANAGER** | Approve/reject pinjaman, melihat branch support analysis, melihat SLA report         |
| **BACKOFFICE**     | Disburse pinjaman, melihat risk evaluation, complete loan                            |
| **ADMIN**          | Manajemen user, role, permission, branch, product, audit logs                        |
| **SUPER_ADMIN**    | Full access ke semua fitur                                                           |
| **CUSTOMER**       | Mengajukan pinjaman, upload dokumen, melihat status pinjaman                         |

## Base URL

```
http://localhost:8080/api/v1
```

## Authentication

### Login Request

```typescript
interface LoginRequest {
  email: string;      // Valid email format
  password: string;   // Min 6 characters
  fcmToken?: string;  // Optional for push notification
}
```

### Login Response

```typescript
interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;    // in seconds
  tokenType: string;    // "Bearer"
}
```

### User Info (from /auth/me)

```typescript
interface UserInfoResponse {
  id: string;           // UUID
  email: string;
  username: string;
  branchId: string;     // UUID
  branchName: string;
  roles: string[];      // ["ROLE_MARKETING", "ROLE_ADMIN"]
  plafond: number;      // BigDecimal
  permissions: string[];
}
```

### Standard API Response Wrapper

```typescript
interface ApiResponse<T> {
  success: boolean;
  message: string;
  code?: string;
  data: T;
  errors?: any;
}
```

### Paged Response

```typescript
interface PagedResponse<T> {
  items: T[];
  meta: {
    page: number;
    size: number;
    totalItems: number;
    totalPages: number;
  };
}
```

---

## 1. MARKETING Role - API Endpoints

### A. Dashboard Marketing

**GET** `/loans`
- Query params: `status`, `branchId`, `customerId`, `page`, `size`, `sort`
- Response: `PagedResponse<LoanResponse>`

### B. Apply Loan on Behalf of Customer

**POST** `/loans/marketing/apply-on-behalf`

#### Request Body

```typescript
interface MarketingApplyLoanRequest {
  // User basic data
  fullName: string;              // required
  email: string;                 // required, valid email
  username: string;              // required
  phoneNumber: string;           // required
  branchId: string;              // UUID, required

  // Biodata fields
  incomeSource: string;          // required, e.g., "Salary"
  incomeType: string;            // required, e.g., "Monthly"
  monthlyIncome: number;         // BigDecimal, required
  nik: string;                   // required, 16 digits
  dateOfBirth: string;           // ISO date, required
  placeOfBirth: string;          // required
  city: string;                  // required
  address: string;               // required
  province: string;              // required
  district: string;              // required
  subDistrict: string;           // required
  postalCode: string;            // required
  gender: 'MALE' | 'FEMALE';     // required
  maritalStatus: 'SINGLE' | 'MARRIED' | 'DIVORCED' | 'WIDOWED';  // required
  occupation: string;            // required

  // Loan fields
  productId: string;             // UUID, required
  loanAmount: number;            // BigDecimal, required
  tenor: number;                 // required, months
  purpose?: string;              // optional

  // Bank Account Information
  bankName?: string;
  bankBranch?: string;
  accountNumber?: string;
  accountHolderName?: string;
}
```

#### Response: `LoanResponse`

```typescript
interface LoanResponse {
  id: string;                    // UUID
  customerId: string;            // UUID
  customerName: string;
  product: ProductResponse;
  loanAmount: number;            // BigDecimal
  tenor: number;
  loanStatus: LoanStatus;
  currentStage: ApprovalStage;
  submittedAt: string;           // ISO datetime
  approvedAt?: string;
  rejectedAt?: string;
  disbursedAt?: string;
  documents: DocumentResponse[];
  disbursementReference?: string;
  aiAnalysis?: LoanAnalysisResponse;
  longitude?: number;
  latitude?: number;

  // Income and NPWP
  declaredIncome?: number;
  npwpNumber?: string;

  // Employment/Business Details
  jobType?: 'KARYAWAN' | 'WIRASWASTA' | 'PROFESIONAL';
  companyName?: string;
  jobPosition?: string;
  workDurationMonths?: number;
  workAddress?: string;
  officePhoneNumber?: string;
  additionalIncome?: number;

  // Emergency Contact
  emergencyContactName?: string;
  emergencyContactRelation?: string;
  emergencyContactPhone?: string;
  emergencyContactAddress?: string;

  // Down Payment
  downPayment?: number;

  // Loan Purpose
  purpose?: string;

  // Bank Account Information
  bankName?: string;
  bankBranch?: string;
  accountNumber?: string;
  accountHolderName?: string;

  // Product rates snapshot
  interestRate?: number;
  adminFee?: number;
}

// Enums
type LoanStatus = 'DRAFT' | 'SUBMITTED' | 'REVIEWED' | 'APPROVED' | 'REJECTED' | 'DISBURSED' | 'COMPLETED' | 'CANCELLED';
type ApprovalStage = 'CUSTOMER' | 'MARKETING' | 'BRANCH_MANAGER' | 'BACKOFFICE';
```

### C. Review Loan

**POST** `/loans/{id}/review`

#### Request Body

```typescript
interface ReviewLoanRequest {
  notes?: string;  // max 1000 characters
}
```

#### Response: `LoanResponse`

### D. Reject Loan

**POST** `/loans/{id}/reject`

#### Request Body

```typescript
interface RejectLoanRequest {
  reason: string;  // required
}
```

#### Response: `LoanResponse`

### E. Rollback Loan

**POST** `/loans/{id}/rollback`

#### Request Body

```typescript
interface ReviewLoanRequest {
  notes?: string;
}
```

#### Response: `LoanResponse`

### F. Get Loan Detail

**GET** `/loans/{id}`
- Response: `LoanResponse`

### G. Get AI Analysis for Loan

**GET** `/loans/{id}/analysis`
- Response: `LoanAnalysisResponse`

```typescript
interface LoanAnalysisResponse {
  confidence: number;           // 0.0 - 1.0
  summary: string;
  riskFlags: string[];
  reviewNotes: string[];
  limitations: string[];
}
```

### H. Get SLA Report

**GET** `/reports/sla/{loanId}`
- Response: `SlaReportResponse`

```typescript
interface SlaReportResponse {
  loanId: string;
  customerName: string;
  stages: StageSlaInfo[];
  totalDurationMinutes: number;
}

interface StageSlaInfo {
  stage: string;
  status: string;
  actionBy: string;
  durationMinutes: number;
}
```

### I. Check User Eligibility

**GET** `/users/{id}/eligibility`
- Response: `EligibilityAnalysisResponse`

```typescript
interface EligibilityAnalysisResponse {
  confidence: number;
  missingData: string[];
  potentialIssues: string[];
  notes: string[];
}
```

### J. Get Product Recommendation

**GET** `/products/recommendation?userId={userId}`
- Response: `ProductRecommendationResponse`

```typescript
interface ProductRecommendationResponse {
  confidence: number;
  recommendedProductCode: string;
  recommendedProductName: string;
  reasoning: string;
  limitations: string[];
}
```

### K. Document Management

**POST** `/documents/presign-upload`

```typescript
interface PresignUploadRequest {
  loanId?: string;           // UUID, optional
  fileName: string;          // required
  documentType: DocumentType; // required
  contentType?: string;
}

type DocumentType = 'KTP' | 'NPWP' | 'KK' | 'PAYSLIP' | 'PROOFOFRESIDENCE' | 'PROFILE_PICTURE' | 'BANK_STATEMENT' | 'OTHER';
```

Response: `PresignUploadResponse`

```typescript
interface PresignUploadResponse {
  documentId: string;   // UUID
  uploadUrl: string;    // Pre-signed URL for direct upload
  objectKey: string;
}
```

**GET** `/documents/{id}/download`
- Response: `DownloadDocumentResponse`

```typescript
interface DownloadDocumentResponse {
  downloadUrl: string;  // Pre-signed URL
  fileName: string;
}
```

---

## 2. BRANCH_MANAGER Role - API Endpoints

### A. Dashboard Branch Manager

**GET** `/loans`
- Query params: `status`, `branchId`, `customerId`, `page`, `size`, `sort`
- Response: `PagedResponse<LoanResponse>`

### B. Approve Loan

**POST** `/loans/{id}/approve`

#### Request Body

```typescript
interface ReviewLoanRequest {
  notes?: string;  // Optional, default: "Approved by Branch Manager"
}
```

#### Response: `LoanResponse`

### C. Reject Loan

**POST** `/loans/{id}/reject`
- Same as Marketing

### D. Rollback Loan

**POST** `/loans/{id}/rollback`
- Same as Marketing

### E. Get Loan Detail

**GET** `/loans/{id}`
- Response: `LoanResponse`

### F. Get AI Branch Decision Support

**GET** `/loans/{id}/analysis/branch-support`
- Response: `BranchManagerSupportResponse`

```typescript
interface BranchManagerSupportResponse {
  confidence: number;
  branchRisks: string[];
  attentionPoints: string[];
  limitations: string[];
}
```

### G. Get SLA Report

**GET** `/reports/sla/{loanId}`
- Same as Marketing

### H. Check User Eligibility

**GET** `/users/{id}/eligibility`
- Same as Marketing

---

## 3. BACKOFFICE Role - API Endpoints

### A. Dashboard Back Office

**GET** `/loans`
- Query params: `status`, `branchId`, `customerId`, `page`, `size`, `sort`
- Response: `PagedResponse<LoanResponse>`

### B. Disburse Loan

**POST** `/loans/{id}/disburse`

#### Request Body

```typescript
interface DisbursementRequest {
  referenceNumber: string;  // required
  bankName?: string;
  accountNumber?: string;
}
```

#### Response: `LoanResponse`

### C. Complete Loan

**POST** `/loans/{id}/complete`
- Response: `LoanResponse`

### D. Get Loan Detail

**GET** `/loans/{id}`
- Response: `LoanResponse`

### E. Get AI Risk Evaluation

**GET** `/loans/{id}/analysis/risk-evaluation`
- Response: `BackOfficeRiskEvaluationResponse`

```typescript
interface BackOfficeRiskEvaluationResponse {
  confidence: number;
  riskOverview: string;
  keyRiskFactors: string[];
  verificationChecklist: string[];
  limitations: string[];
}
```

### F. Get SLA Report

**GET** `/reports/sla/{loanId}`
- Same as Marketing

---

## 4. ADMIN Role - API Endpoints

### A. User Management

#### Get All Users

**GET** `/users`
- Query params: `status`, `roleName`, `branchId`, `page`, `size`
- Response: `PagedResponse<UserSummaryResponse>`

```typescript
interface UserSummaryResponse {
  id: string;
  fullName: string;
  username: string;
  email: string;
  roles: string[];
  status: UserStatus;
  branchName: string;
}

type UserStatus = 'ACTIVE' | 'INACTIVE' | 'PENDING' | 'SUSPENDED';
```

#### Create User

**POST** `/users`

```typescript
interface CreateUserRequest {
  fullName: string;  // required
  email: string;     // required, valid email
  branchId?: string; // UUID, optional
}
```

- Response: `UserSummaryResponse`

#### Delete User

**DELETE** `/users/admin/users/{userId}`

#### Force Logout User

**POST** `/users/admin/users/{userId}/force-logout`

### B. Product Management

#### Get All Products

**GET** `/products`
- Query params: `isActive`, `page`, `size`
- Response: `PagedResponse<ProductResponse>`

```typescript
interface ProductResponse {
  id: string;
  productCode: string;
  productName: string;
  description: string;
  interestRate: number;
  adminFee: number;
  minTenor: number;
  maxTenor: number;
  minLoanAmount: number;
  maxLoanAmount: number;
  isActive: boolean;
}
```

#### Create Product

**POST** `/products`

```typescript
interface CreateProductRequest {
  productCode: string;      // required
  productName: string;      // required
  description?: string;
  interestRate: number;     // required, positive
  adminFee: number;         // required, positive
  minTenor: number;         // required, positive
  maxTenor: number;         // required, positive
  minLoanAmount: number;    // required, positive
  maxLoanAmount: number;    // required, positive
}
```

#### Update Product

**PUT** `/products/{id}`

```typescript
interface UpdateProductRequest {
  productName: string;      // required
  description?: string;
  interestRate: number;     // required, > 0
  minTenor: number;         // required, positive
  maxTenor: number;         // required, positive
  minLoanAmount: number;    // required, positive
  maxLoanAmount: number;    // required, positive
  adminFee: number;         // required, positive
  isActive: boolean;        // required
}
```

#### Delete Product

**DELETE** `/products/{id}`

### C. Branch Management (RBAC)

#### Get All Branches

**GET** `/rbac/branches`
- Response: `BranchResponse[]`

```typescript
interface BranchResponse {
  id: string;
  name: string;
  address: string;
  city: string;
  state: string;
  zipCode: string;
  phone: string;
  longitude: string;
  latitude: string;
}
```

#### Create Branch

**POST** `/rbac/branches`

```typescript
interface CreateBranchRequest {
  name: string;      // required
  address: string;   // required
  city: string;      // required
  state: string;     // required
  zipCode: string;   // required
  phone: string;     // required
  longitude?: string;
  latitude?: string;
}
```

#### Update Branch

**PUT** `/rbac/branches/{branchId}`
- Same body as CreateBranch

#### Delete Branch

**DELETE** `/rbac/branches/{branchId}`

### D. Role Management (RBAC)

#### Get All Roles

**GET** `/rbac/roles`
- Response: `RoleResponse[]`

```typescript
interface RoleResponse {
  id: string;
  name: RoleName;
  description: string;
  permissions: PermissionResponse[];
}

type RoleName = 'ROLE_CUSTOMER' | 'ROLE_ADMIN' | 'ROLE_BRANCH_MANAGER' | 'ROLE_BACK_OFFICE' | 'ROLE_SUPER_ADMIN' | 'ROLE_MARKETING';

interface PermissionResponse {
  id: string;
  name: string;
  description: string;
}
```

#### Get All Permissions

**GET** `/rbac/permissions`
- Response: `PermissionResponse[]`

#### Get Role Permissions

**GET** `/rbac/roles/{roleId}/permissions`
- Response: `PermissionResponse[]`

#### Assign Permissions to Role

**POST** `/rbac/roles/{roleId}/permissions`

```typescript
interface AssignPermissionsRequest {
  permissionIds: string[];  // UUID array
}
```

#### Remove Permission from Role

**DELETE** `/rbac/roles/{roleId}/permissions/{permissionId}`

### E. User-Role Management (RBAC)

#### Get User Roles

**GET** `/rbac/users/{userId}/roles`
- Response: `RoleResponse[]`

#### Assign Roles to User

**POST** `/rbac/users/{userId}/roles`

```typescript
interface AssignRolesRequest {
  roleIds: string[];  // UUID array
}
```

#### Remove Role from User

**DELETE** `/rbac/users/{userId}/roles/{roleId}`

### F. Audit Logs

#### Get Audit Logs

**GET** `/admin/audit-logs`
- Query params: `page`, `size`, `sort`
- Response: `PagedResponse<AuditLogResponse>`

```typescript
interface AuditLogResponse {
  id: string;
  userId: string;
  action: string;
  resourceType: string;
  resourceId: string;
  details: string;
  createdAt: string;
}
```

### G. Reports

#### Get Loan KPIs

**GET** `/reports/kpis`
- Response: `LoanKpiResponse`

```typescript
interface LoanKpiResponse {
  totalLoans: number;
  totalSubmitted: number;
  totalReviewed: number;
  totalApproved: number;
  totalRejected: number;
  totalCancelled: number;
  totalDisbursed: number;
  totalCompleted: number;
  loansByProduct: Record<string, number>;
}
```

#### Export KPIs to Excel

**GET** `/reports/kpis/export`
- Response: `Blob` (Excel file)

---

## 5. Notification Endpoints (All Roles)

### Get My Notifications

**GET** `/notifications`
- Response: `NotificationResponse[]`

```typescript
interface NotificationResponse {
  id: string;
  userId: string;
  title: string;
  body: string;
  type: NotificationType;
  referenceId: string;
  isRead: boolean;
  createdAt: string;
  link: string;
}

type NotificationType = 'LOAN' | 'AUTH' | 'SYSTEM';
```

### Mark Notification as Read

**PUT** `/notifications/{id}/read`

### Mark All Notifications as Read

**PUT** `/notifications/mark-all-read`

---

## 6. Profile Endpoints (All Roles)

### Get My Profile

**GET** `/users/me`
- Response: `UserProfileResponse`

```typescript
interface UserProfileResponse {
  id: string;
  fullName: string;
  email: string;
  phoneNumber: string;
  profilePictureUrl: string;
  branch: BranchInfo;
  biodata: BiodataInfo;
  product: ProductResponse;
}

interface BranchInfo {
  id: string;
  name: string;
}

interface BiodataInfo {
  incomeSource: string;
  incomeType: string;
  monthlyIncome: number;
  nik: string;
  dateOfBirth: string;
  placeOfBirth: string;
  city: string;
  address: string;
  province: string;
  district: string;
  subDistrict: string;
  postalCode: string;
  gender: 'MALE' | 'FEMALE';
  maritalStatus: 'SINGLE' | 'MARRIED' | 'DIVORCED' | 'WIDOWED';
  occupation: string;
}
```

### Update My Profile

**PUT** `/users/me`

```typescript
interface UpdateProfileRequest {
  fullName: string;           // required
  phoneNumber: string;        // required
  profilePictureUrl?: string;
  
  // Biodata fields (all optional)
  incomeSource?: string;
  incomeType?: string;
  monthlyIncome?: number;
  nik?: string;
  dateOfBirth?: string;
  placeOfBirth?: string;
  city?: string;
  address?: string;
  province?: string;
  district?: string;
  subDistrict?: string;
  postalCode?: string;
  gender?: 'MALE' | 'FEMALE';
  maritalStatus?: 'SINGLE' | 'MARRIED' | 'DIVORCED' | 'WIDOWED';
  occupation?: string;
  longitude?: number;
  latitude?: number;
}
```

### Update Profile Photo

**PUT** `/users/me/photo`
- Content-Type: `multipart/form-data`
- Body: `photo: File`
- Response: `UserProfileResponse`

### Get My Profile Photo

**GET** `/users/me/photo`
- Response: `Blob` (JPEG image)

### Delete My Account

**DELETE** `/users/me`

---

## 7. Authentication Endpoints (Public)

### Register

**POST** `/auth/register`

```typescript
interface RegisterRequest {
  fullName: string;     // required
  username: string;     // required
  email: string;        // required, valid email
  password: string;     // required, min 6 characters
  phoneNumber: string;  // required, 10-13 digits
}
```

- Response: `LoginResponse`

### Login

**POST** `/auth/login`
- See Login section above

### Google Login

**POST** `/auth/google`

```typescript
interface GoogleLoginRequest {
  idToken: string;  // Google ID Token
}
```

- Response: `LoginResponse`

### Refresh Token

**POST** `/auth/refresh`

```typescript
interface RefreshTokenRequest {
  refreshToken: string;
}
```

- Response: `LoginResponse`

### Logout

**POST** `/auth/logout`
- Header: `Authorization: Bearer {token}`

### Forgot Password

**POST** `/auth/forgot-password`

```typescript
interface ForgotPasswordRequest {
  email: string;
}
```

### Reset Password

**POST** `/auth/reset-password`

```typescript
interface ResetPasswordRequest {
  token: string;
  newPassword: string;
}
```

### Change Password

**POST** `/auth/change-password`

```typescript
interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}
```

### Get Current User

**GET** `/auth/me`
- Response: `UserInfoResponse`

---

## Angular Implementation Guidelines

### 1. Service Structure

```typescript
// services/auth.service.ts
// services/loan.service.ts
// services/marketing.service.ts
// services/branch-manager.service.ts
// services/backoffice.service.ts
// services/admin.service.ts
// services/user.service.ts
// services/notification.service.ts
// services/document.service.ts
```

### 2. Interceptors

```typescript
// interceptors/auth.interceptor.ts - Add JWT token to requests
// interceptors/error.interceptor.ts - Handle API errors
// interceptors/loading.interceptor.ts - Show loading spinner
```

### 3. Guards

```typescript
// guards/auth.guard.ts - Check authentication
// guards/role.guard.ts - Check user roles
// guards/marketing.guard.ts
// guards/branch-manager.guard.ts
// guards/backoffice.guard.ts
// guards/admin.guard.ts
```

### 4. Module Structure

```
src/app/
├── core/
│   ├── models/           # TypeScript interfaces
│   ├── services/         # API services
│   ├── guards/           # Route guards
│   ├── interceptors/     # HTTP interceptors
│   └── enums/            # TypeScript enums
├── features/
│   ├── auth/             # Login, Register, Forgot Password
│   ├── marketing/        # Marketing dashboard, apply loan
│   ├── branch-manager/   # Branch manager dashboard, approve
│   ├── backoffice/       # Backoffice dashboard, disburse
│   ├── admin/            # User, Role, Product, Branch management
│   └── shared/           # Shared components
└── layout/
    ├── sidebar/          # Role-based sidebar
    ├── header/           # Header with notifications
    └── dashboard/        # Main layout
```

### 5. Role-Based Sidebar Menu

#### Marketing Menu

- Dashboard
- Apply Loan (On Behalf)
- Loan List
- SLA Reports
- Product Recommendations

#### Branch Manager Menu

- Dashboard
- Pending Approvals
- Loan List
- Branch Support Analysis
- SLA Reports

#### Back Office Menu

- Dashboard
- Pending Disbursements
- Loan List
- Risk Evaluation
- SLA Reports

#### Admin Menu

- Dashboard
- User Management
- Role & Permission
- Branch Management
- Product Management
- Audit Logs
- Reports

### 6. HTTP Client Configuration

```typescript
// environment.ts
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api/v1'
};

// Base service
export abstract class BaseService {
  protected baseUrl = environment.apiUrl;
  
  protected get headers() {
    return {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${this.getToken()}`
    };
  }
  
  private getToken(): string {
    return localStorage.getItem('accessToken') || '';
  }
}
```

### 7. Error Handling Pattern

```typescript
interface ApiError {
  success: false;
  code: string;
  message: string;
  errors?: any;
}

// Common error codes
// 401 - Unauthorized (redirect to login)
// 403 - Forbidden (insufficient permissions)
// 404 - Not Found
// 409 - Conflict (duplicate data)
// 422 - Validation Error
// 500 - Internal Server Error
```

### 8. Pagination Pattern

```typescript
// For all list endpoints
interface PaginationParams {
  page?: number;      // default: 0
  size?: number;      // default: 10
  sort?: string;      // e.g., "createdAt,desc"
}

// Usage
getLoans(params: PaginationParams & { status?: LoanStatus }): Observable<ApiResponse<PagedResponse<LoanResponse>>> {
  return this.http.get(`${this.baseUrl}/loans`, { params: { ...params } });
}
```

---

## Summary of Key Endpoints by Role

|      Feature       |               MARKETING               |             BRANCH_MANAGER              |                BACKOFFICE                |               ADMIN                |
|--------------------|---------------------------------------|-----------------------------------------|------------------------------------------|------------------------------------|---|
| View Loans         | GET /loans                            | GET /loans                              | GET /loans                               | GET /loans                         |
| Apply Loan         | POST /loans/marketing/apply-on-behalf | -                                       | -                                        | -                                  |
| Review Loan        | POST /loans/{id}/review               | -                                       | -                                        | -                                  |
| Approve Loan       | -                                     | POST /loans/{id}/approve                | -                                        | -                                  |
| Reject Loan        | POST /loans/{id}/reject               | POST /loans/{id}/reject                 | -                                        | -                                  |
| Disburse Loan      | -                                     | -                                       | POST /loans/{id}/disburse                | -                                  |
| Complete Loan      | -                                     | -                                       | POST /loans/{id}/complete                | -                                  |
| Rollback Loan      | POST /loans/{id}/rollback             | POST /loans/{id}/rollback               | -                                        | -                                  |
| AI Analysis        | GET /loans/{id}/analysis              | -                                       | -                                        | -                                  |
| Branch Support     | -                                     | GET /loans/{id}/analysis/branch-support | -                                        | -                                  |
| Risk Evaluation    | -                                     | -                                       | GET /loans/{id}/analysis/risk-evaluation | -                                  |
| SLA Report         | GET /reports/sla/{loanId}             | GET /reports/sla/{loanId}               | GET /reports/sla/{loanId}                | -                                  |
| User Management    | -                                     | -                                       | -                                        | GET/POST /users                    |
| Product Management | -                                     | -                                       | -                                        | GET/POST/PUT/DELETE /products      |
| Branch Management  | -                                     | -                                       | -                                        | GET/POST/PUT/DELETE /rbac/branches |
| Role Management    | -                                     | -                                       | -                                        | GET/POST/PUT/DELETE /rbac/roles    |
| Audit Logs         | -                                     | -                                       | -                                        | GET /admin/audit-logs              |
| KPI Reports        | -                                     | -                                       | -                                        | GET /reports/kpis                  | " |

