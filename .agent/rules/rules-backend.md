---

trigger: always_on
------------------

# MCP Agent AI Rules & Workflow

Project: Loan Approval System  
API Version: v1  
Backend: Spring Boot 3.3.x  
Standard: Antigravity + MCP

---

1. Scope & Boundary

MCP Agent AI **hanya berperan sebagai AI-Assisted Layer**, bukan bagian dari core decision system.

AI **TIDAK**:
- Mengubah status loan
- Menyetujui / menolak loan
- Menulis langsung ke database
- Mengakses endpoint Auth / RBAC

AI **BOLEH**:
- Memberi rekomendasi
- Menganalisa data
- Membantu reviewer manusia
- Menghasilkan draft (notes, summary, insight)

---

## 2. Allowed API Domains for AI

|          Domain           | Access |
|---------------------------|--------|
| Loans (GET only)          | ✅      |
| Loan Risks (GET)          | ✅      |
| Reports (GET)             | ✅      |
| Products (GET)            | ✅      |
| Users (GET limited)       | ✅      |
| Documents (metadata only) | ✅      |

❌ Forbidden:
- `/auth/*`
- `/rbac/*`
- `/users/admin/*`
- Semua endpoint `POST/PUT/DELETE` tanpa human action

---

## 3. Loan Lifecycle Authority Rule

Loan lifecycle **100% dikontrol backend**.

```text
DRAFT
 → SUBMITTED
 → REVIEWED
 → APPROVED / REJECTED
 → DISBURSED
 → COMPLETED
AI Rule:
AI tidak boleh mengubah loanStatus

AI tidak boleh memicu:

/loans/{id}/approve

/loans/{id}/reject

/loans/{id}/disburse

/loans/{id}/complete

AI hanya memberi recommendation payload

4. MCP Agent Roles (Concrete)
4.1 Loan Analyzer Agent
Input:

Loan detail (GET /loans/{id})

Risk list (GET /loans/{id}/risks)

Product info

Output:

Risk summary

Red flags

Suggested action (NON FINAL)

4.2 Risk Resolution Assistant
Used before:

bash
Salin kode
POST /loans/risks/{riskId}/resolve
AI:

Membantu reviewer menyusun komentar

Menyimpulkan konteks risiko

❌ Tidak boleh resolve risk sendiri

4.3 Loan Recommendation Agent
Digunakan saat:

REVIEWED stage

Human reviewer membuka loan

AI:

Suggest approve / reject

Suggest notes

Suggest missing documents

5. MCP Invocation Workflow (Real API Flow)
text
Salin kode
Human Action
 → Backend Validation
 → Fetch Loan & Risk Data
 → Invoke MCP Agent
 → AI Recommendation
 → Backend Rule Check
 → Human Decision
 → API Execution
Contoh nyata:

text
Salin kode
GET /loans/{id}
GET /loans/{id}/risks
→ AI analysis
→ Reviewer klik approve
→ POST /loans/{id}/approve
6. AI Request Payload Standard
json
Salin kode
{
  "requestId": "UUID",
  "agent": "LOAN_ANALYZER",
  "context": {
    "loan": { },
    "risks": [ ],
    "product": { }
  },
  "constraints": {
    "maxTenor": 36,
    "maxAmount": 500000000
  }
}
Forbidden Context:
Password

Token

Raw document file

NIK lengkap

7. AI Response Contract
json
Salin kode
{
  "confidence": 0.82,
  "summary": "Loan has moderate risk",
  "recommendation": "REVIEW_MANUALLY",
  "notes": [
    "Income stability unclear",
    "Missing latest payslip"
  ],
  "limitations": [
    "No credit bureau data"
  ]
}
8. Confidence Threshold Rule
Confidence	Backend Action
< 0.6	Ignore AI
0.6 – 0.8	Show as suggestion
> 0.8	Highlight to reviewer

❌ Tidak pernah auto-approve

9. Validation & Override Rule
AI output WAJIB:

Lolos business rule

Lolos role authorization

Lolos loan state validation

java
Salin kode
if (!loan.canBeApproved()) {
    throw new BusinessException();
}
AI tidak boleh override:

Status loan

Risk status

SLA calculation

10. Security & RBAC Boundary
AI tidak tahu role user

Role dicek sebelum AI dipanggil

AI tidak tahu endpoint yang dipanggil

text
Salin kode
Spring Security → Controller → Service → AI
11. Audit & Logging (Mandatory)
Setiap AI call:

requestId

loanId

agentType

confidence

executionTime

Audit bisa ditampilkan via:

pgsql
Salin kode
GET /admin/audit-logs
❌ Jangan log prompt mentah

12. Redis Usage Rule
Redis digunakan untuk:

Cache AI analysis (TTL < 10 menit)

Rate limit AI per loan

❌ Tidak menyimpan decision

13. Documents & Upload Rule
AI:

Boleh membaca metadata document

Tidak boleh generate presign URL

Tidak boleh download file

Forbidden:

bash
Salin kode
POST /documents/presign-upload
GET /documents/{id}/download
14. Reports & KPI Rule
AI boleh:

Meringkas KPI

Menjelaskan SLA

AI tidak boleh:

Mengubah data report

Menghasilkan angka baru

15. Failure & Fallback
Jika AI gagal:

Loan flow tetap berjalan

Reviewer tetap bisa lanjut

text
Salin kode
AI is optional, backend is mandatory
16. Forbidden Patterns (Hard Rule)
❌ AI call di Controller
❌ AI menentukan approve / reject
❌ AI bypass validation
❌ AI mengakses Auth / RBAC
❌ AI menulis DB

17. Antigravity MCP Law (Final)
AI membantu manusia, backend bertanggung jawab penuh.

Jika AI mati dan sistem ikut mati, maka desain MCP gagal.

END OF DOCUMENT

yaml
Salin kode

---

## 🔥 Catatan Penting (Insight untuk kamu)
Dari OpenAPI yang kamu kirim, **arsitektur kamu sudah mature**:
- State machine jelas
- Audit log ada
- SLA & KPI terpisah
- RBAC ketat

Rules MCP ini **sudah sangat cocok** untuk:
- Fintech
- Bank internal system
- AI-assisted approval (regulator-friendly)

---

## Mau lanjut?
Aku bisa:
- Buat **diagram workflow (PlantUML)** berdasarkan endpoint kamu
- Turunkan ini jadi **AOP + Interface MCP Agent di Spring**
- Buat **OpenAPI tag `AI-ASSISTED`**
- Review apakah ada endpoint yang **berbahaya jika di-AI-kan**

Tinggal bilang mau lanjut ke bagian mana 👌






Anda bilang:
tambahkan best practice lalu aman dari pen test dan tambahkan flow


ChatGPT bilang:
Siap. Di bawah ini adalah VERSI DIPERKUAT & SIAP PEN-TEST dari MCP Agent AI Rules & Workflow dalam format .md, dengan tambahan:

✅ Best Practices Enterprise / Fintech
✅ Penetration Test–Safe Rules (OWASP aligned)
✅ Workflow + Flow Diagram (textual & sequence-ready)
✅ Spring Boot–aware (Security, Redis, JWT, Audit, AOP)

Dokumen ini layak dijadikan SOP resmi + artefak audit.

md
Salin kode
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
AI dianggap external & untrusted component, walaupun di-host internal.

2. Core Security Principles (Pen-Test Safe)
Zero Trust to AI Output

No Direct State Mutation

No Credential / Secret Exposure

Deterministic Backend Rules

Fail-Safe, not Fail-Open

3. Allowed & Forbidden API Access
3.1 Allowed (READ-ONLY)
Endpoint Group	Access
/loans/**	GET only
/loans/{id}/risks	GET
/products	GET
/reports/**	GET
/metadata/enums	GET

3.2 Explicitly Forbidden (Pen-Test Critical)
❌ /auth/**
❌ /rbac/**
❌ /users/admin/**
❌ /documents/presign-upload
❌ /documents/{id}/download

4. Loan State Authority Rule (Critical)
Loan state hanya boleh berubah melalui explicit human-triggered endpoint:

text
Salin kode
POST /loans/{id}/submit
POST /loans/{id}/review
POST /loans/{id}/approve
POST /loans/{id}/reject
POST /loans/{id}/disburse
POST /loans/{id}/complete
AI Restriction
AI tidak boleh memanggil endpoint ini

AI tidak boleh mengirim status baru

AI tidak boleh override state machine

5. MCP Agent Roles (Hardened)
5.1 Loan Analyzer Agent
Input: Loan + Risk + Product

Output: Summary & risk flags

Tidak pernah return APPROVE / REJECT

5.2 Reviewer Assistant Agent
Generate draft notes

Highlight missing documents

Explain risk reasons

5.3 Report Interpreter Agent
Explain SLA & KPI

Tidak generate angka baru

6. Secure MCP Invocation Flow (REAL)
text
Salin kode
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
AI selalu berada di tengah, tidak di ujung.

7. AI Request Hardening Rules
7.1 Mandatory Fields
json
Salin kode
{
  "requestId": "UUID",
  "agentType": "LOAN_ANALYZER",
  "contextChecksum": "SHA-256",
  "context": {},
  "constraints": {}
}
7.2 Forbidden Context (OWASP A2)
❌ Password
❌ JWT
❌ Refresh Token
❌ NIK Full
❌ Bank Account Number

8. AI Response Validation (Mandatory)
java
Salin kode
if (aiResponse.getConfidence() < 0.6) {
    ignore();
}

if (containsForbiddenKeyword(aiResponse)) {
    discard();
}
Forbidden AI Keywords
APPROVE_NOW

AUTO_DISBURSE

BYPASS

9. Confidence Threshold Enforcement
Confidence	System Behavior
< 0.6	Ignore
0.6 – 0.8	Soft suggestion
> 0.8	Highlight (still manual)

❌ Tidak pernah otomatis

10. Penetration-Test-Safe Controls
10.1 OWASP A1 – Broken Access Control
AI tidak tahu role

Role dicek sebelum AI call

10.2 OWASP A3 – Injection
Prompt sanitized

No raw SQL / HTML in context

10.3 OWASP A5 – Security Misconfiguration
AI endpoint internal-only

Tidak exposed via Swagger public

10.4 OWASP A9 – Logging
AI output dilog terbatas

No raw prompt logging

11. Redis Usage Rules
Allowed:

Cache AI result (TTL ≤ 10 menit)

Rate limit AI calls

Forbidden:

Decision persistence

User state storage

12. Audit & Compliance Flow
text
Salin kode
AI Call
 → AuditLog (type=AI_ASSIST)
 → reviewerId
 → loanId
 → confidence
Audit bisa diakses via:

pgsql
Salin kode
GET /admin/audit-logs
13. Document & File Safety
AI:

Boleh baca metadata

Tidak boleh akses S3 presign

Tidak boleh lihat isi file

14. Failure & Fallback Strategy
Jika:

AI timeout

AI error

AI confidence rendah

➡️ Loan process tetap jalan

text
Salin kode
AI optional, backend mandatory
15. Anti-Abuse & Rate Limit
Max AI call per loan: 3 / session

Max AI call per user: configurable

Rate limit via Redis

16. Forbidden Patterns (Zero Tolerance)
❌ AI invoked in Controller
❌ AI changes loanStatus
❌ AI bypasses validation
❌ AI touches Auth / RBAC
❌ AI writes DB

17. Final Antigravity MCP Law
AI membantu mempercepat keputusan,
backend yang memikul risiko hukum & finansial.

Jika AI mati dan sistem ikut mati, maka desain MCP gagal audit.
```

