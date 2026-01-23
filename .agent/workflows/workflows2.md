---

description:
------------

Backend Architecture, API Design, AI Integration & Security Workflow

Project: Loan Approval System
Backend: Spring Boot 3.3.x
Standard: OJK, BI, OWASP Top 10, Antigravity MCP
Audience: Backend, Security, Audit, AI Engineer

1. Backend Architecture Workflow (Spring Boot)
   1.1 Layered Architecture (STRICT)
   Controller
   ↓
   Request DTO + Validation
   ↓
   UseCase / Application Service
   ↓
   Domain Model (Entity + State Machine)
   ↓
   Repository
   ↓
   Database

❌ Forbidden

Business logic di Controller

Entity langsung di Controller

AI call di Controller

1.2 Module Structure (Recommended)
com.lofi
├── auth
├── user
├── loan
│    ├── controller
│    ├── dto
│    ├── usecase
│    ├── domain
│    ├── repository
│    ├── policy        ← business rules (DBR, plafon)
│    └── state         ← loan state machine
├── notification
├── document
├── ai
│    ├── mcp
│    ├── agent
│    └── dto
├── audit
├── security
└── common

2. API Design & Endpoint Workflow
   2.1 REST API Design Rules (Hard)
   Rule	Description
   Stateless	JWT only
   Idempotent	POST approve/disburse wajib idempotency-key
   Explicit State	Tidak implicit transition
   Versioned	/api/v1/**
   Audit First	Semua POST/PUT tercatat
   2.2 Loan Lifecycle Endpoints (Example)
   POST   /api/v1/loans                → submit
   POST   /api/v1/loans/{id}/review
   POST   /api/v1/loans/{id}/approve
   POST   /api/v1/loans/{id}/reject
   POST   /api/v1/loans/{id}/disburse
   GET    /api/v1/loans/{id}

Workflow

HTTP Request
→ Auth Filter
→ RBAC
→ Validation
→ UseCase
→ State Guard
→ Transaction Commit
→ Event Published
→ Notification + Audit

❌ Tidak ada endpoint multi-action
❌ Tidak ada auto-transition

3. AI Integration Workflow (MCP-Safe)
   3.1 AI Invocation Position
   Controller
   → UseCase
   → Fetch Data
   → Invoke AI (Optional)
   → Validate AI Output
   → Return Recommendation

❌ AI tidak boleh di Controller
❌ AI tidak boleh menulis DB

3.2 AI UseCase Flow (Concrete)
Reviewer Opens Loan
→ GET loan + risks
→ AI Analyzer invoked
→ AI returns recommendation
→ Backend validates
→ Reviewer decides
→ State change API called

3.3 AI Boundary Rules
Rule	Status
Read-only data	✅
State mutation	❌
Auth access	❌
Presign upload	❌
4. Security & Pen-Test Workflow
4.1 Request Security Flow (OWASP-Aligned)
Incoming Request
→ Rate Limit (Redis)
→ JWT Validation
→ RBAC Check
→ Input Validation
→ Business Rule Validation
→ UseCase
→ Commit
→ Audit Log

4.2 Critical Pen-Test Controls
OWASP Risk	Mitigation
A1 Broken Access	RBAC + method-level security
A2 Crypto	Hashed token, TLS only
A3 Injection	DTO validation, no raw SQL
A5 Misconfig	Disable actuator public
A9 Logging	No PII in logs
4.3 Idempotency Workflow (Critical)
POST /approve
→ Check Idempotency-Key
→ Already processed? → Return same result
→ Else process

Mandatory for

Approve

Disburse

Reset password

5. Transaction, Event & Notification Flow
   @Transaction
   → Business Logic
   → Save State
   COMMIT
   → Domain Event
   → Notification
   → Audit

❌ No notification before commit
❌ No email inside transaction

6. Audit & Traceability Workflow
   Action
   → AuditAspect
   → Save:
   - actorId
   - role
   - action
   - oldState → newState
   - timestamp

Audit tidak boleh dihapus.

7. Secure Deployment Workflow
   Code
   → Unit Test
   → Integration Test
   → Security Scan
   → CI Pass
   → Deploy
   → Runtime Monitoring

Mandatory:

HTTPS

Secret via env / vault

No hardcoded credential

8. Failure & Fallback Strategy
   Case	Behavior
   AI down	Loan flow tetap jalan
   Notification fail	Retry + log
   Partial commit	Rollback
   Duplicate request	Idempotent response
9. Final Governance Rule
   API jelas,
   State dijaga,
   AI membantu,
   Security memblokir,
   Audit mencatat.

Jika salah satu hilang → system tidak layak produksi.
