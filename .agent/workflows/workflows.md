---

description:
------------

# Loan Application Workflow (OJK & BI Compliant)

Project: Loan Approval System  
Version: 1.0  
Regulator Reference: OJK & Bank Indonesia  
Audience: Product, Backend, Audit, AI MCP

---

## 1. Actors & Roles

### 1.1 User (Customer)

- Registrasi & login
- Mengisi data diri
- Mengajukan pinjaman
- Melihat status

### 1.2 Marketing

- Review calon nasabah
- Input data pinjaman (on behalf of customer)
- Tidak bisa approve

### 1.3 Branch Manager

- Approve level cabang
- Kontrol plafon cabang

### 1.4 Back Office

- Final approval
- Hitung rasio & risiko
- Disbursement

---

## 2. Global Rules (Hard Rules)

1. **Tidak ada loan tanpa data wajib**
2. **Tidak ada approve jika plafon terlampaui**
3. **State loan tidak boleh lompat**
4. **Approval bersifat berjenjang**
5. **Semua keputusan terekam audit log**

---

## 3. High Level Workflow (Customer)

```text
Register
 → Login
 → Dashboard
 → Complete Profile
 → Eligibility Check
 → Apply Loan
 → Review & Approval
 → Disbursement
4. Detailed Customer Workflow
4.1 Registration & Dashboard
text
Salin kode
Register → Login → Dashboard
Dashboard hanya menampilkan:

Status profil

Plafon sementara

Tombol "Apply Loan" (jika eligible)

4.2 Profile Completion (MANDATORY)
A. Data Identitas (Wajib – OJK)
NIK (KTP)

Nama lengkap

Nama gadis ibu kandung

Status perkawinan

Kontak darurat (nama, relasi, no HP)

❌ Jika tidak lengkap → Tidak bisa lanjut

B. Domisili & Kontak
Alamat KTP

Alamat tinggal saat ini

Status kepemilikan rumah:

Milik sendiri

Sewa / Kontrak

Rumah dinas

C. Pekerjaan & Keuangan (Wajib)
Nama perusahaan

Jabatan

Lama bekerja

Penghasilan bulanan (THP)

Slip gaji (3 bulan terakhir)

4.3 Eligibility Check (System Rule)
text
Salin kode
IF profile incomplete → BLOCK
IF exceeds DBR (30–35%) → BLOCK
IF SLIK OJK bermasalah → BLOCK
Jika lolos → eligible apply loan

5. Product Assignment Logic (Auto + Manual)
5.1 Default Product Assignment
Sistem otomatis assign product TERENDAH

Berdasarkan:

Penghasilan

DBR

Risiko awal

❗ Customer TIDAK bisa menaikkan product

5.2 Product Upgrade (ADMIN ONLY)
Marketing / BM / BO bisa menaikkan product

Dengan justifikasi

Tercatat audit

6. Apply Loan Workflow (Customer)
6.1 Loan Input
Customer memilih:

Tenor

Tujuan pinjaman

Nominal (≤ plafon)

Tambahan redundant (untuk audit & risk):

Sumber dana pembayaran

Tipe pengeluaran utama

Estimasi cicilan ideal

6.2 Conditional Document Rule (Product-Based)
Product / Tenor	Wajib
Tenor rendah	Identitas + Slip Gaji
Tenor menengah	+ Rekening Koran
Tenor besar / produktif	+ SPT PPh 21 + NPWP
Rumah sewa	+ Bukti domisili

7. Staff Workflow
7.1 Marketing Workflow
text
Salin kode
View Application
 → Review Profile
 → View Scoring
 → Add Loan Data (Optional)
 → Submit for Review
Rules:

Bisa input loan untuk customer

Akun customer auto-generated

Data harus lengkap (sama seperti customer)

❌ Tidak bisa approve

7.2 Branch Manager Workflow
text
Salin kode
View Pending Loans
 → Check Plafon Cabang
 → Approve / Reject
Rules:

Bisa approve SELAMA plafon cabang cukup

Jika banyak pengajuan paralel:

Yang melewati plafon → auto disabled

7.3 Back Office Workflow (Final Authority)
text
Salin kode
Receive Approved Loan
 → Recalculate Risk
 → Final Approve
 → Disbursement
Back Office wajib cek:

DBR / DSR

Slip gaji

Rekening koran

Kondisi rumah

Riwayat SLIK

Konsistensi data

8. Disbursement Workflow
text
Salin kode
Final Approved
 → Input Reference
 → Bank Transfer
 → Update Status (DISBURSED)
 → Notify Customer
Status final:

DISBURSED

COMPLETED (setelah kewajiban selesai)

9. Compliance Rules (OJK & BI)
9.1 Data Protection
Consent eksplisit:

Akses SLIK OJK

Syarat & Ketentuan

Pemasaran (opsional)

9.2 Regulatory Ratios
DBR maksimal: 30–35%

NPWP wajib jika:

Plafon > threshold

Audit trail tidak boleh dihapus

10. Failure & Block Conditions
Condition	Result
Data tidak lengkap	BLOCK
Plafon terlampaui	BLOCK
DBR > limit	REJECT
SLIK buruk	REJECT
Dokumen invalid	RETURN

11. AI MCP Position in Workflow
text
Salin kode
Profile Complete
 → Loan Submitted
 → AI Analysis (Optional)
 → Staff Review
AI hanya:

Membantu analisis

Memberi rekomendasi

Tidak mengambil keputusan

12. Audit & Traceability
Semua aksi dicatat:

User / Staff ID

Timestamp

Action

Old → New state

13. Final Governance Rule
Customer mengajukan,
Staff menilai,
Back Office bertanggung jawab,
Sistem menjaga kepatuhan.

Jika satu tahap dilewati → loan invalid secara regulasi.

END OF WORKFLOW DOCUMENT

yaml
Salin kode

---

## 🔍 Catatan Penting (Professional Insight)
Workflow ini **sudah setara bank / multifinance** dan **aman saat audit OJK & pen-test** karena:
- Tidak ada auto approval
- Role separation jelas
- State machine ketat
- Consent & traceability lengkap
```

