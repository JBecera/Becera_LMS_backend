# Vertical Slice Architecture Refactoring Report

## 1. Previous Project Structure
The project initially followed a traditional layered structure where related logic was grouped by technical concern:

- Controllers under `controllers`
- Services under `services`
- Repositories under `repositories`
- Entities under `entities`

This structure worked for small features, but business features were spread across multiple folders and were harder to trace end-to-end.

## 2. Refactored Project Structure
The backend was reorganized into feature-based vertical slices under the `features` package.

```text
backend/src/main/java/edu/cit/becera/lrbms/
├── features/
│   ├── auth/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── model/
│   │   └── service/
│   ├── membership/
│   │   ├── controller/
│   │   ├── dto/
│   │   └── service/
│   ├── catalog/
│   │   ├── controller/
│   │   ├── dto/
│   │   └── service/
│   └── booking/
│       ├── controller/
│       ├── dto/
│       └── service/
├── entities/
├── repositories/
└── services/
```

## 3. Features Converted into Vertical Slices

### a. Authentication Slice
- Responsible for login handling.
- Contains:
  - `AuthController`
  - `AuthService`
  - `LoginRequest`
  - `AuthResponse`

### b. Membership Slice
- Responsible for user registration and account management.
- Contains:
  - `MembershipController`
  - `MembershipService`
  - `CreateMemberRequest`

### c. Catalog Slice
- Responsible for book management and reservation.
- Contains:
  - `CatalogController`
  - `CatalogService`
  - `BookRequest`

### d. Booking Slice
- Responsible for booking workflow handling.
- Contains:
  - `BookingSliceController`
  - `BookingSliceService`
  - `CreateBookingRequest`

## 4. How Each Slice Works

### Authentication Slice
1. The frontend sends login data to `/api/auth/login`.
2. The controller forwards the request to `AuthService`.
3. The service validates the credentials and returns an `AuthResponse`.

### Membership Slice
1. The admin or user submits registration details.
2. `MembershipController` receives the request.
3. `MembershipService` validates and stores the member record.

### Catalog Slice
1. The librarian or member interacts with the book catalog.
2. The controller routes create, update, delete, search, and reserve operations.
3. The service manages validation and updates the book inventory.

### Booking Slice
1. A booking request is received from the client.
2. The controller routes it to the booking service.
3. The service ensures the resource and member exist and saves the booking.

## 5. Challenges Encountered and Solutions Applied

### Challenge 1: Feature logic was previously spread across generic layers
- Solution: Grouped related controller, service, DTO, and validation code into feature packages.

### Challenge 2: Maintaining compatibility with existing frontend API calls
- Solution: Kept the same endpoint paths such as `/api/auth/login`, `/api/members`, `/api/books`, and `/api/bookings`.

### Challenge 3: Preserving validation and domain rules
- Solution: Moved validation into each slice service so each feature owns its own rules.

## 6. Code Samples Showing the Changes

### Authentication Slice Example
```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", ex.getMessage()));
        }
    }
}
```

### Catalog Slice Example
```java
@RestController
@RequestMapping("/api/books")
public class CatalogController {
    @PostMapping
    public ResponseEntity<?> createBook(@RequestBody BookRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.createBook(request));
    }
}
```

## 7. Verification
The refactored backend was compiled and tested successfully using:

```bash
mvnw.cmd test
```

Result:
- 4 tests run
- 0 failures
- 0 errors
- 0 skipped

## 8. Suggested Commit History Table

| Feature Refactored | Commit Hash Link |
|---|---|
| Authentication Slice | [https://github.com/JBecera/Becera_LMS_backend/commit/](https://github.com/JBecera/Becera_LMS_backend/commit/) |
| Membership Slice | [https://github.com/JBecera/Becera_LMS_backend/commit/](https://github.com/JBecera/Becera_LMS_backend/commit/) |
| Catalog Slice | [https://github.com/JBecera/Becera_LMS_backend/commit/](https://github.com/JBecera/Becera_LMS_backend/commit/) |
| Booking Slice | [https://github.com/JBecera/Becera_LMS_backend/commit/](https://github.com/JBecera/Becera_LMS_backend/commit/) |
