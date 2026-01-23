---

description:
------------

# Notification, Email & Document Upload Workflow

Project: Loan Approval System  
Version: 1.1  
Channels: Firebase (Push), Email, In-App  
Storage: Cloudflare R2  
Compliance: OJK, BI, OWASP

---

## 1. Notification Channels

|    Channel    |              Usage              |
|---------------|---------------------------------|
| Firebase Push | Real-time workflow notification |
| Email         | Legal & formal communication    |
| In-App        | Status visibility               |
| Audit Log     | Traceability                    |

---

## 2. Global Notification Rules (Hard Rules)

1. **State change → notification mandatory**
2. **No notification without successful transaction**
3. **Notification content non-sensitive**
4. **Email for legal-impact events**
5. **Retry & fallback supported**

---

## 3. Authentication & Password Recovery

### 3.1 Forgot Password Workflow

```text
User → Forgot Password
 → Generate Token
 → Send Email
 → Deep Link to Mobile App
 → Reset Password
Email Content
Subject: Reset Password

CTA Link:

pgsql
Salin kode
lofiapp://reset-password?token={token}
Fallback (Web):

perl
Salin kode
https://app.lofi.id/reset-password?token={token}
Rules:

Token single-use

Expiry max 15 menit

Token disimpan hashed (Redis / DB)

4. Loan Workflow Notifications (FCM)
4.1 Customer Apply Loan
Trigger:

bash
Salin kode
POST /loans
Notify:

Branch Manager (FCM)

Marketing (FCM)

Message:

New loan application submitted

4.2 Marketing Review
Trigger:

bash
Salin kode
POST /loans/{id}/review
Notify:

Customer (FCM)

Branch Manager (FCM)

Message:

Your loan is under review by marketing

4.3 Branch Manager Approve
Trigger:

bash
Salin kode
POST /loans/{id}/approve
Notify:

Customer (FCM)

Back Office (FCM)

Message:

Loan approved by Branch Manager

4.4 Back Office Final Approve
Trigger:

bash
Salin kode
POST /loans/{id}/approve (Final)
Notify:

Customer (FCM)

Branch Manager (FCM)

Email to Customer

Email Subject:

Loan Approval Confirmation

4.5 Back Office Disbursement
Trigger:

bash
Salin kode
POST /loans/{id}/disburse
Notify:

Customer (FCM)

Email to Customer

Email Content:

Amount

Reference number

Date

5. Notification Delivery Flow
text
Salin kode
Business Action
 → Commit Transaction
 → Event Published
 → Notification Service
 → FCM / Email
 → Audit Log
❌ Notification sebelum commit

6. Firebase Notification Payload (Standard)
json
Salin kode
{
  "type": "LOAN_STATUS",
  "loanId": "UUID",
  "status": "APPROVED",
  "message": "Your loan has been approved",
  "timestamp": "ISO-8601"
}
Rules:

Tidak kirim nominal sensitif

Gunakan loanId sebagai context

7. Email Rules (Compliance)
Email WAJIB untuk:

Reset Password

Final Approval

Disbursement

Email TIDAK BOLEH:

Berisi token mentah (kecuali reset)

Menampilkan data sensitif

8. Document Upload Workflow (Cloudflare R2)
8.1 Upload Flow
text
Salin kode
Request Presign URL
 → Upload to R2
 → Verify Upload
 → Save Metadata
 → Trigger Notification
Trigger:

bash
Salin kode
POST /documents/presign-upload
8.2 On Upload Event
Notify:

Customer (FCM)

Marketing (FCM jika loan aktif)

Message:

Document uploaded successfully

9. Document Security Rules
File tidak lewat backend

URL presign expiry ≤ 10 menit

Virus scan (async, recommended)

File encrypted at rest

10. Notification Retry & Fallback
Case	Action
FCM failed	Retry
Email failed	Retry
Retry exhausted	Log + Alert

11. Audit Logging (Mandatory)
Setiap notifikasi:

eventType

recipientRole

channel (FCM / Email)

timestamp

status (SENT / FAILED)

12. Pen-Test Safety Rules
No PII in push notification

Deep link token validated backend

Rate limit forgot password

Webhook protected

13. Final Notification Law
State berubah → User tahu → Audit tercatat

Jika satu notifikasi hilang, workflow dianggap tidak lengkap.

END OF NOTIFICATION WORKFLOW
```

