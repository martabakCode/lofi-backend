# Prompt: Implement Notification Feature in Android (Jetpack Compose)

**Context:**
The backend for the Notification System is complete. We need to implement the mobile client side using Jetpack Compose. The API endpoint is ready at `GET /api/v1/notifications`.

**Backend Data Structure (Reference):**
The API returns a list of notifications with the following JSON structure:

```json
[
  {
    "id": "uuid",
    "userId": "uuid",
    "title": "Loan Approved",
    "body": "Your loan #123 has been approved.",
    "type": "LOAN", // Enum: LOAN, AUTH, SYSTEM
    "referenceId": "uuid-of-loan",
    "isRead": false,
    "createdAt": "2023-10-27T10:00:00",
    "link": "optional-deep-link"
  }
]
```

**Requirements:**

1. **Data Layer**:
   - Create `NotificationResponse` data class.
   - Create `NotificationType` enum (LOAN, AUTH, SYSTEM).
   - Add `getNotifications()` function to the Retrofit Service interface.
   - Implement specific repository function.
2. **Domain/ViewModel**:
   - Create a `NotificationViewModel`.
   - Expose `uiState` (Loading, Success<List<Notification>>, Error).
   - Implement a function `fetchNotifications()`.
3. **UI Implementation (Jetpack Compose)**:
   - Create a `NotificationScreen`.
   - Use `LazyColumn` to display the list.
   - **Item Design**:
     - Use a `Card` or `Surface`.
     - Highlight "Unread" notifications (e.g., different background color or a dot indicator).
     - Show Title, Body, and formatted Date (e.g., "2 hours ago").
     - Add an icon based on `NotificationType` (e.g., Money icon for LOAN, Shield for AUTH).
4. **Navigation Logic (Critical)**:
   - When a user clicks a notification, handle navigation based on `type`:
     - **LOAN**: Navigate to `LoanDetailScreen` passing the `referenceId`.
     - **AUTH**: Navigate to `ProfileScreen`.
     - **SYSTEM**: Show a tailored generic info dialog or screen.

**Tech Stack**:
-   Kotlin
-   Jetpack Compose
-   Hilt (Dependency Injection)
-   Retrofit
-   Coroutines/Flow

**Instruction**:
Please generate the code for:
1.  The Data Classes.
2.  The ViewModel.
3.  The Composable `NotificationItem` and `NotificationScreen`.
4.  The Navigation handling logic within the `onClick`.
