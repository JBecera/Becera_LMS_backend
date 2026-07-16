package edu.cit.becera.lrbms;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end workflow #2: FR-008 reservations, including the SRS business rule that a title may
 * only be reserved once every copy is checked out, and the duplicate-reservation-prevention
 * validation scenario.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:workflow2;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
class ReservationWaitlistWorkflowTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @SuppressWarnings("unchecked")
    @Test
    void memberJoinsWaitlistAndDuplicateReservationIsRejected() {
        String librarianToken = loginAndGetToken("librarian@library.test", "librarian123");

        // A fully-checked-out title (0 copies) - the only case a reservation is allowed for.
        Map<String, Object> outOfStockBook = Map.of(
                "title", "The Pragmatic Programmer",
                "author", "Hunt & Thomas",
                "isbn", "ISBN-WORKFLOW-2A",
                "category", "Programming",
                "availableCopies", 0
        );
        Number outOfStockBookId = createBook(librarianToken, outOfStockBook);

        // An available title (1 copy) - reservation must be rejected for this one.
        Map<String, Object> availableBook = Map.of(
                "title", "Refactoring",
                "author", "Martin Fowler",
                "isbn", "ISBN-WORKFLOW-2B",
                "category", "Programming",
                "availableCopies", 1
        );
        Number availableBookId = createBook(librarianToken, availableBook);

        Number memberId = registerMember("priya.member@example.com");
        String memberToken = loginAndGetToken("priya.member@example.com", "pass1234");

        // Reserve the out-of-stock title - should succeed and land on the PENDING queue.
        ResponseEntity<Map> firstReservation = restTemplate.exchange(
                "/api/reservations", HttpMethod.POST,
                new HttpEntity<>(Map.of("resourceId", outOfStockBookId), authHeaders(memberToken)), Map.class);
        assertThat(firstReservation.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(firstReservation.getBody().get("status")).isEqualTo("PENDING");
        Number reservationId = (Number) firstReservation.getBody().get("id");

        // Validation scenario: duplicate reservation of the same title by the same member is rejected.
        ResponseEntity<Map> duplicateReservation = restTemplate.exchange(
                "/api/reservations", HttpMethod.POST,
                new HttpEntity<>(Map.of("resourceId", outOfStockBookId), authHeaders(memberToken)), Map.class);
        assertThat(duplicateReservation.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat((String) duplicateReservation.getBody().get("error")).containsIgnoringCase("already reserved");

        // Business rule: a title with copies available cannot be reserved (would-be instant grab).
        ResponseEntity<Map> availableReservation = restTemplate.exchange(
                "/api/reservations", HttpMethod.POST,
                new HttpEntity<>(Map.of("resourceId", availableBookId), authHeaders(memberToken)), Map.class);
        assertThat(availableReservation.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        // Librarian approves the legitimate reservation.
        ResponseEntity<Map> approval = restTemplate.exchange(
                "/api/reservations/" + reservationId, HttpMethod.PUT,
                new HttpEntity<>(Map.of("status", "APPROVED"), authHeaders(librarianToken)), Map.class);
        assertThat(approval.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(approval.getBody().get("status")).isEqualTo("APPROVED");

        // A second member's reservation on the same title is rejected by the librarian.
        Number secondMemberId = registerMember("noah.member@example.com");
        String secondMemberToken = loginAndGetToken("noah.member@example.com", "pass1234");
        ResponseEntity<Map> secondReservation = restTemplate.exchange(
                "/api/reservations", HttpMethod.POST,
                new HttpEntity<>(Map.of("resourceId", outOfStockBookId), authHeaders(secondMemberToken)), Map.class);
        assertThat(secondReservation.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Number secondReservationId = (Number) secondReservation.getBody().get("id");

        ResponseEntity<Map> rejection = restTemplate.exchange(
                "/api/reservations/" + secondReservationId, HttpMethod.PUT,
                new HttpEntity<>(Map.of("status", "REJECTED"), authHeaders(librarianToken)), Map.class);
        assertThat(rejection.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(rejection.getBody().get("status")).isEqualTo("REJECTED");

        assertThat(memberId).isNotNull();
        assertThat(secondMemberId).isNotNull();
    }

    private Number createBook(String librarianToken, Map<String, Object> bookRequest) {
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/books", HttpMethod.POST, new HttpEntity<>(bookRequest, authHeaders(librarianToken)), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return (Number) response.getBody().get("id");
    }

    private Number registerMember(String email) {
        Map<String, Object> request = Map.of(
                "firstName", "Test", "lastName", "Member", "email", email, "password", "pass1234");
        ResponseEntity<Map> response = restTemplate.postForEntity("/api/members", request, Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return (Number) response.getBody().get("id");
    }

    @SuppressWarnings("unchecked")
    private String loginAndGetToken(String email, String password) {
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/auth/login", Map.of("email", email, "password", password), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return (String) response.getBody().get("token");
    }

    private HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }
}
