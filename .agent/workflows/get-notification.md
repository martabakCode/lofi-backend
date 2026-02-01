# Global Get Notification Workflow

Project: Loan Approval System
Version: 1.0
Channels: In-App, Rest API
Compliance: Audit Log, Read Tracking

---

## 1. Overview

This workflow describes the process of retrieving global notifications for a user, handling pagination (implied/future), and managing read status. It complements the push notification workflow by providing a persistent history.

---

## 2. Notification Data Structure

|     Field     |           Type            |           Description           |
|---------------|---------------------------|---------------------------------|
| `id`          | UUID                      | Unique Identifier               |
| `userId`      | UUID                      | Recipient User ID               |
| `title`       | String                    | Notification Title              |
| `body`        | String                    | Notification Content            |
| `type`        | Enum (LOAN, AUTH, SYSTEM) | Categorization                  |
| `referenceId` | UUID                      | Target Object ID (e.g., LoanID) |
| `isRead`      | Boolean                   | Read Status                     |
| `createdAt`   | LocalDateTime             | Timestamp                       |
| `link`        | String                    | Deep Link / URL (Optional)      |

---

## 3. Retrieval Workflow (Get Notifications)

### 3.1 Flow

```text
User Application (Mobile/Web)
 → Request GET /api/v1/notifications
 → Authenticate User (JWT)
 → Extract User ID
 → Query Database (descending by createdAt)
 → Return List<NotificationResponse>
```

### 3.2 Endpoint Specification

- **Method**: `GET`
- **Path**: `/api/v1/notifications`
- **Auth**: Bearer Token (Required)

### 3.3 Privacy & Security

- Users can ONLY retrieve their own notifications.
- No sensitive PII in `body` or `title` (refer to Push Notification Rules).
- `referenceId` allows context resolution without embedding sensitive data.

---

## 4. Interaction Workflow (Click & Redirect)

### 4.1 Mobile (Compose)

1. **User taps Notification Item**.
2. **App checks `type`**:
   - `LOAN`: Navigate to Loan Detail Screen (`/loan/{referenceId}`).
   - `AUTH`: Navigate to Profile/Security Screen.
   - `SYSTEM`: Show Dialog or WebView.
3. **App calls `markAsRead`** (Optional/Future).

### 4.2 Frontend (Angular)

1. **User clicks Notification in Dropdown/Page**.
2. **Service resolves Route**:
   - `LOAN`: `router.navigate(['/loans', notification.referenceId])`
   - `AUTH`: `router.navigate(['/profile'])`
3. **UI visual update** (mark as read style).

---

## 5. Audit & Compliance

- **Retrieval Log**: Access to notification history is logged (standard HTTP access logs).
- **Read Receipt**: When `markAsRead` is implemented, exact timestamp of "Read" action is recorded.

END OF WORKFLOW
