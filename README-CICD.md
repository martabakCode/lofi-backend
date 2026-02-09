# Panduan CI/CD GitHub Actions & Jenkins

Dokumen ini berisi panduan setup environment variables dan secrets untuk pipeline CI/CD menggunakan GitHub Actions.

## 1. Setup GitHub Secrets

Masuk ke repository GitHub Anda -> **Settings** -> **Secrets and variables** -> **Actions** -> **New repository secret**.

Tambahkan secret berikut sesuai dengan environment variable yang Anda miliki di lokal (`.env`):

### Credentials Server & Docker
| Secret Name | Value Example | Description |
|---|---|---|
| `DOCKER_USERNAME` | `lofiuser` | Username Docker Hub Anda |
| `DOCKER_PASSWORD` | `dckr_pat_xxx` | Password/Token Docker Hub |
| `VPS_HOST` | `103.10.xx.xx` | IP Address VPS Target |
| `VPS_USER` | `root` | Username SSH VPS |
| `VPS_SSH_KEY` | `-----BEGIN OPENSSH PRIVATE KEY...` | Private Key SSH untuk akses ke VPS |

### Konfigurasi Aplikasi (Dari .env)
| Secret Name | Description |
|---|---|
| `DB_PASSWORD` | Password database SQL Server |
| `JWT_SECRET` | Secret key untuk JWT Token |
| `REDIS_PASSWORD` | Password Redis (jika ada) |
| `MAIL_HOST` | SMTP Host (e.g., smtp.gmail.com) |
| `MAIL_PORT` | SMTP Port (e.g., 587) |
| `MAIL_USERNAME` | Email pengirim |
| `MAIL_PASSWORD` | App Password Email |
| `CLOUDFLARE_R2_ACCESS_KEY_ID` | R2 Access Key |
| `CLOUDFLARE_R2_SECRET_ACCESS_KEY` | R2 Secret Key |
| `CLOUDFLARE_R2_ENDPOINT` | R2 Endpoint URL |
| `CLOUDFLARE_R2_BUCKET_NAME` | R2 Bucket Name |
| `FIREBASE_PROJECT_ID` | Project ID Firebase |
| `FIREBASE_CLIENT_EMAIL` | Email Service Account Firebase |
| `GOOGLE_CLIENT_ID` | Google OAuth Client ID |
| `GOOGLE_CLIENT_SECRET` | Google OAuth Client Secret |
| `APP_BASE_URL` | Base URL Backend (e.g. `https://api.lofi.com`) |
| `FRONTEND_URL` | URL Frontend (untuk CORS) |

### Khusus: Firebase Service Account File
Pipeline ini memetlukan file `firebase-service-account.json` agar bisa dibuild ke dalam aplikasi.
Karena file ini tidak boleh dicommit ke repo, kita menyimpannya sebagai **Base64 Encoded Secret**.

**Cara Membuat Secret `FIREBASE_SERVICE_ACCOUNT_JSON_BASE64`:**

1.  Di komputer lokal Anda, konversi file json ke base64.
    *   **Linux/Mac/Git Bash**:
        ```bash
        cat src/main/resources/firebase-service-account.json | base64 -w 0
        ```
    *   **Windows PowerShell**:
        ```powershell
        [Convert]::ToBase64String([IO.File]::ReadAllBytes("src/main/resources/firebase-service-account.json"))
        ```
2.  Copy string panjang hasil perintah di atas.
3.  Buat Secret baru di GitHub bernama `FIREBASE_SERVICE_ACCOUNT_JSON_BASE64` dan paste string tersebut.

## 2. Struktur Env Variables di VPS

Pipeline akan otomatis membuat file `.env.production` (untuk branch main) atau `.env.development` (untuk branch develop) di VPS pada folder `/opt/lofiapps/<env>`.

Isi file ini diregenerate setiap deployment menggunakan value dari GitHub Secrets, sehingga Anda **TIDAK PERLU** membuat file `.env` manual di VPS kecuali untuk debugging.

## 3. Workflow

- **Push ke `main`**:
    - Build JAR (include firebase-json).
    - Build Docker Image (tag: `latest` & `production`).
    - Deploy ke VPS Port **8080**.
- **Push ke `develop`**:
    - Build JAR (include firebase-json).
    - Build Docker Image (tag: `development`).
    - Deploy ke VPS Port **8081**.
