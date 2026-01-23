# MCP Agent AI Rules, Best Practices & Secure Workflow

Project: Loan Approval System  
API Version: v1  
Backend: Spring Boot 3.3.x  
Security Standard: OWASP Top 10 + Antigravity MCP  
Status: Production-Ready

---

## 1. MCP AI Security Positioning

MCP Agent AI berada di **ASSISTIVE ZONE**, bukan **TRUSTED ZONE**.

```text
Client → Backend (Trusted) → MCP AI (Untrusted) → Backend (Trusted)
```

AI dianggap external & untrusted component, walaupun di-host internal.

## 2. Core Security Principles (Pen-Test Safe)

- **Zero Trust to AI Output**
- **No Direct State Mutation**
- **No Credential / Secret Exposure**
- **Deterministic Backend Rules**
- **Fail-Safe, not Fail-Open**

## 3. Allowed & Forbidden API Access

### 3.1 Allowed (READ-ONLY)

|   Endpoint Group    |  Access  |
|---------------------|----------|
| `/loans/**`         | GET only |
| `/loans/{id}/risks` | GET      |
| `/products`         | GET      |
| `/reports/**`       | GET      |
| `/metadata/enums`   | GET      |

### 3.2 Explicitly Forbidden (Pen-Test Critical)

- ❌ `/auth/**`
- ❌ `/rbac/**`
- ❌ `/users/admin/**`
- ❌ `/documents/presign-upload`
- ❌ `/documents/{id}/download`

## 4. Loan State Authority Rule (Critical)

Loan state hanya boleh berubah melalui explicit human-triggered endpoint:

```text
POST /loans/{id}/submit
POST /loans/{id}/review
POST /loans/{id}/approve
POST /loans/{id}/reject
POST /loans/{id}/disburse
POST /loans/{id}/complete
```

**AI Restriction**:
- AI tidak boleh memanggil endpoint ini
- AI tidak boleh mengirim status baru
- AI tidak boleh override state machine

## 5. MCP Agent Roles (Hardened)

### 5.1 Loan Analyzer Agent

- Input: Loan + Risk + Product
- Output: Summary & risk flags
- **Tidak pernah return APPROVE / REJECT**

### 5.2 Reviewer Assistant Agent

- Generate draft notes
- Highlight missing documents
- Explain risk reasons

### 5.3 Report Interpreter Agent

- Explain SLA & KPI
- **Tidak generate angka baru**

## 6. Secure MCP Invocation Flow (REAL)

```text
[User Action]
   ↓
[Spring Security Filter]
   ↓
[Controller]
   ↓
[Request Validation]
   ↓
[Business Rule Validation]
   ↓
[Fetch Loan + Risks]
   ↓
[MCP Agent Invocation]
   ↓
[AI Response Validation]
   ↓
[Human Decision]
   ↓
[State-Changing API]
```

AI selalu berada di tengah, tidak di ujung.

## 7. AI Request Hardening Rules

### 7.1 Mandatory Fields

```json
{
  "requestId": "UUID",
  "agentType": "LOAN_ANALYZER",
  "contextChecksum": "SHA-256",
  "context": {},
  "constraints": {}
}
```

### 7.2 Forbidden Context (OWASP A2)

- ❌ Password
- ❌ JWT
- ❌ Refresh Token
- ❌ NIK Full
- ❌ Bank Account Number

## 8. AI Response Validation (Mandatory)

```java
if (aiResponse.getConfidence() < 0.6) {
    ignore();
}

if (containsForbiddenKeyword(aiResponse)) {
    discard();
}
```

**Forbidden AI Keywords**:
- `APPROVE_NOW`
- `AUTO_DISBURSE`
- `BYPASS`

## 9. Confidence Threshold Enforcement

| Confidence |     System Behavior      |
|------------|--------------------------|
| < 0.6      | Ignore                   |
| 0.6 – 0.8  | Soft suggestion          |
| > 0.8      | Highlight (still manual) |

❌ **Tidak pernah otomatis**

## 10. Penetration-Test-Safe Controls

### 10.1 OWASP A1 – Broken Access Control

- AI tidak tahu role
- Role dicek sebelum AI call

### 10.2 OWASP A3 – Injection

- Prompt sanitized
- No raw SQL / HTML in context

### 10.3 OWASP A5 – Security Misconfiguration

- AI endpoint internal-only
- Tidak exposed via Swagger public

### 10.4 OWASP A9 – Logging

- AI output dilog terbatas
- No raw prompt logging

## 11. Redis Usage Rules

- **Allowed**:
  - Cache AI result (TTL ≤ 10 menit)
  - Rate limit AI calls
- **Forbidden**:
  - Decision persistence
  - User state storage

## 12. Audit & Compliance Flow

```text
AI Call
 → AuditLog (type=AI_ASSIST)
 → reviewerId
 → loanId
 → confidence
```

Audit bisa diakses via:

```sql
GET /admin/audit-logs
```

## 13. Document & File Safety

**AI**:
- Boleh baca metadata
- Tidak boleh akses S3 presign
- Tidak boleh lihat isi file

## 14. Failure & Fallback Strategy

Jika:
- AI timeout
- AI error
- AI confidence rendah

➡️ Loan process tetap jalan

```text
AI optional, backend mandatory
```

## 15. Anti-Abuse & Rate Limit

- Max AI call per loan: 3 / session
- Max AI call per user: configurable
- Rate limit via Redis

## 16. Forbidden Patterns (Zero Tolerance)

- ❌ AI invoked in Controller
- ❌ AI changes loanStatus
- ❌ AI bypasses validation
- ❌ AI touches Auth / RBAC
- ❌ AI writes DB

## 17. Final Antigravity MCP Law

AI membantu mempercepat keputusan,
backend yang memikul risiko hukum & finansial.

Jika AI mati dan sistem ikut mati, maka desain MCP gagal audit.
