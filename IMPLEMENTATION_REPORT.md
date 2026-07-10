# Implementation Report – Core Feature: Book Catalog Management and Reservation

## 1. Core Feature Overview
The implemented core feature of the Library Resource Booking and Management System is the book catalog management and reservation workflow. It allows librarians to maintain the library inventory and enables members to search for books, view availability, and reserve titles when copies are still available.

This feature is important because it connects three major concerns of the system:
- catalog maintenance,
- inventory availability tracking, and
- booking/reservation processing.

In the current implementation, the workflow is handled by the catalog slice in the backend and consumed by the member and librarian dashboards in the frontend.

## 2. List of Functional Requirements Supporting the Core Feature
The following functional requirements are supported by the current implementation:

1. Librarians can add new books to the catalog.
2. Librarians can update existing book details.
3. Librarians can remove books from the catalog.
4. Members can search books by title or author.
5. The system can display a book’s metadata, description, and availability.
6. Members can reserve books only when at least one copy is available.
7. Successful reservation reduces the remaining number of available copies.
8. The system stores booking-related records and links them to a resource and member.
9. The system returns clear validation and error messages to the user.

## 3. Core Feature Workflow
1. A librarian submits book details through the librarian dashboard.
2. The frontend sends the data to the backend catalog API.
3. The backend validates the payload and persists the book entity.
4. A member opens the dashboard, searches the catalog, and views available titles.
5. The member clicks Reserve on a book with available copies.
6. The backend verifies availability and decreases the available copy count.
7. The updated catalog data is returned to the frontend and shown immediately.
8. The booking/ reservation flow is also reflected through the booking slice and persistence layer.

## 4. Sequence of Actions

| User action | System response | Backend processing | Database operation | Expected output |
|---|---|---|---|---|
| Librarian enters book details and submits the form | The form is sent to the server | CatalogController receives the request and delegates to CatalogService | BookRepository saves a new Book record | A new book appears in the catalog |
| Librarian edits an existing book | The updated information is saved | CatalogService validates the request and updates the existing entity | BookRepository updates the existing record | The catalog shows the updated book details |
| Member searches for a title | The catalog list is filtered and returned | CatalogService searches by title or author | BookRepository executes a search query | Matching books are displayed in the dashboard |
| Member clicks Reserve on an available book | A reservation request is processed | CatalogService checks availability and decrements copies | BookRepository updates the availableCopies value | Availability is updated and a success message is shown |
| Member tries to reserve an unavailable book | The request is rejected with a conflict message | CatalogService throws an IllegalStateException | No database update occurs | The user sees an error explaining that no copies are available |

## 5. Implementation Explanation

### Backend components
- [backend/src/main/java/edu/cit/becera/lrbms/features/catalog/controller/CatalogController.java](backend/src/main/java/edu/cit/becera/lrbms/features/catalog/controller/CatalogController.java)
  - Exposes REST endpoints for listing, searching, creating, updating, deleting, and reserving books.
  - Converts service exceptions into HTTP responses with meaningful error messages.

- [backend/src/main/java/edu/cit/becera/lrbms/features/catalog/service/CatalogService.java](backend/src/main/java/edu/cit/becera/lrbms/features/catalog/service/CatalogService.java)
  - Contains the core business logic for the feature.
  - Validates incoming book data, creates and updates books, searches the catalog, and handles reservation logic.
  - Implements the inventory rule that a reservation cannot be made when available copies are zero or negative.

- [backend/src/main/java/edu/cit/becera/lrbms/features/catalog/dto/BookRequest.java](backend/src/main/java/edu/cit/becera/lrbms/features/catalog/dto/BookRequest.java)
  - Defines the request payload used by the catalog endpoints.
  - Carries title, author, ISBN, category, description, cover image, and available copies.

- [backend/src/main/java/edu/cit/becera/lrbms/entities/Book.java](backend/src/main/java/edu/cit/becera/lrbms/entities/Book.java)
  - Represents the persisted book entity.
  - Stores title, author, ISBN, category, description, cover image, and available copy count.
  - Includes JPA annotations so the object is stored in the books table.

- [backend/src/main/java/edu/cit/becera/lrbms/repositories/BookRepository.java](backend/src/main/java/edu/cit/becera/lrbms/repositories/BookRepository.java)
  - Provides database access for books.
  - Includes a custom query to search books by title or author.

### Booking-related components
- [backend/src/main/java/edu/cit/becera/lrbms/features/booking/controller/BookingSliceController.java](backend/src/main/java/edu/cit/becera/lrbms/features/booking/controller/BookingSliceController.java)
  - Handles booking-related HTTP endpoints.
  - Exposes endpoints for listing, creating, and updating bookings.

- [backend/src/main/java/edu/cit/becera/lrbms/features/booking/service/BookingSliceService.java](backend/src/main/java/edu/cit/becera/lrbms/features/booking/service/BookingSliceService.java)
  - Implements booking lifecycle logic.
  - Validates that the booking request contains a resource and a member and persists the booking entity.

- [backend/src/main/java/edu/cit/becera/lrbms/features/booking/dto/CreateBookingRequest.java](backend/src/main/java/edu/cit/becera/lrbms/features/booking/dto/CreateBookingRequest.java)
  - Defines the request body for booking creation and update.

- [backend/src/main/java/edu/cit/becera/lrbms/entities/Booking.java](backend/src/main/java/edu/cit/becera/lrbms/entities/Booking.java)
  - Represents a booking record and links a booking to a resource and member.

### Frontend components
- [frontend/src/pages/Dashboard.jsx](frontend/src/pages/Dashboard.jsx)
  - Implements the member-facing catalog experience.
  - Loads books, supports search, and triggers reservation requests.
  - Displays availability and success/error messages.

- [frontend/src/pages/LibrarianDashboard.jsx](frontend/src/pages/LibrarianDashboard.jsx)
  - Implements the librarian-facing catalog management experience.
  - Handles book creation, editing, and deletion.
  - Sends data to the catalog API and refreshes the catalog list.

- [frontend/src/services/bookService.js](frontend/src/services/bookService.js)
  - Wraps the frontend API calls for listing, creating, updating, deleting, and reserving books.

## 6. Validation and Error-Handling Mechanisms
The implementation uses both input validation and domain-rule validation:

- Required fields are checked before a book is created or updated.
- Title, author, ISBN, and category must not be empty.
- Available copies must be zero or greater.
- The system ensures that a book exists before updating, deleting, or reserving it.
- Reservation is blocked when the book has no remaining copies.
- Exceptions are caught in the controllers and translated into HTTP responses such as 400 Bad Request, 404 Not Found, or 409 Conflict.
- Error payloads follow a simple structure such as {"error": "..."}.

## 7. Commit History Table
The table below lists the main files that should be included in the commit for each feature area.

| Feature | Summary | Files to include in the commit |
|---|---|---|
| Catalog CRUD implementation | Added book creation, update, and delete workflow for librarians | [backend/src/main/java/edu/cit/becera/lrbms/features/catalog/controller/CatalogController.java](backend/src/main/java/edu/cit/becera/lrbms/features/catalog/controller/CatalogController.java), [backend/src/main/java/edu/cit/becera/lrbms/features/catalog/service/CatalogService.java](backend/src/main/java/edu/cit/becera/lrbms/features/catalog/service/CatalogService.java), [backend/src/main/java/edu/cit/becera/lrbms/features/catalog/dto/BookRequest.java](backend/src/main/java/edu/cit/becera/lrbms/features/catalog/dto/BookRequest.java), [backend/src/main/java/edu/cit/becera/lrbms/entities/Book.java](backend/src/main/java/edu/cit/becera/lrbms/entities/Book.java), [backend/src/main/java/edu/cit/becera/lrbms/repositories/BookRepository.java](backend/src/main/java/edu/cit/becera/lrbms/repositories/BookRepository.java), [frontend/src/pages/LibrarianDashboard.jsx](frontend/src/pages/LibrarianDashboard.jsx), [frontend/src/services/bookService.js](frontend/src/services/bookService.js) |
| Reservation workflow | Implemented availability-based book reservation and inventory decrement | [backend/src/main/java/edu/cit/becera/lrbms/features/catalog/controller/CatalogController.java](backend/src/main/java/edu/cit/becera/lrbms/features/catalog/controller/CatalogController.java), [backend/src/main/java/edu/cit/becera/lrbms/features/catalog/service/CatalogService.java](backend/src/main/java/edu/cit/becera/lrbms/features/catalog/service/CatalogService.java), [frontend/src/pages/Dashboard.jsx](frontend/src/pages/Dashboard.jsx), [frontend/src/services/bookService.js](frontend/src/services/bookService.js) |
| Booking management slice | Added booking endpoints and persistence for resource/member booking records | [backend/src/main/java/edu/cit/becera/lrbms/features/booking/controller/BookingSliceController.java](backend/src/main/java/edu/cit/becera/lrbms/features/booking/controller/BookingSliceController.java), [backend/src/main/java/edu/cit/becera/lrbms/features/booking/service/BookingSliceService.java](backend/src/main/java/edu/cit/becera/lrbms/features/booking/service/BookingSliceService.java), [backend/src/main/java/edu/cit/becera/lrbms/features/booking/dto/CreateBookingRequest.java](backend/src/main/java/edu/cit/becera/lrbms/features/booking/dto/CreateBookingRequest.java), [backend/src/main/java/edu/cit/becera/lrbms/entities/Booking.java](backend/src/main/java/edu/cit/becera/lrbms/entities/Booking.java) |
| Frontend catalog integration | Connected the member and librarian dashboards to the catalog and reservation APIs | [frontend/src/pages/Dashboard.jsx](frontend/src/pages/Dashboard.jsx), [frontend/src/pages/LibrarianDashboard.jsx](frontend/src/pages/LibrarianDashboard.jsx), [frontend/src/services/bookService.js](frontend/src/services/bookService.js) |
