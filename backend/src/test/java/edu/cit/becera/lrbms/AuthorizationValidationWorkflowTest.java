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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end workflow #3: FR-004 access control. Demonstrates several validation scenarios in
 * one integrated path - privilege escalation on self-registration, unauthenticated access,
 * unauthorized (403) access to staff-only and other-member data, and a cross-feature business
 * rule (an unpaid fine blocking a new reservation, i.e. Fine/Transaction/Reservation
 * integration failure handling).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:workflow3;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
class AuthorizationValidationWorkflowTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @SuppressWarnings("unchecked")
    @Test
    void roleEscalationIsBlockedAndStaffOnlyDataIsProtected() {
        // Validation scenario: anonymous self-registration cannot elevate its own role.
        Map<String, Object> escalationAttempt = Map.of(
                "firstName", "Eve", "lastName", "Attacker", "email", "eve.attacker@example.com",
                "password", "pass1234", "role", "ADMIN");
        ResponseEntity<Map> registerResponse = restTemplate.postForEntity("/api/members", escalationAttempt, Map.class);
        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(registerResponse.getBody().get("role")).isEqualTo("MEMBER");
        Number eveId = (Number) registerResponse.getBody().get("id");

        // Validation scenario: unauthenticated requests to a protected endpoint are rejected.
        ResponseEntity<Map> noTokenResponse = restTemplate.getForEntity("/api/members", Map.class);
        assertThat(noTokenResponse.getStatusCode()).isIn(HttpStatus.FORBIDDEN, HttpStatus.UNAUTHORIZED);

        String eveToken = loginAndGetToken("eve.attacker@example.com", "pass1234");

        // Validation scenario: a MEMBER cannot list all accounts (staff-only endpoint).
        ResponseEntity<Map> staffOnlyResponse = restTemplate.exchange(
                "/api/members", HttpMethod.GET, new HttpEntity<>(authHeaders(eveToken)), Map.class);
        assertThat(staffOnlyResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Validation scenario: a MEMBER cannot create catalog resources (staff-only endpoint).
        ResponseEntity<Map> createBookAttempt = restTemplate.exchange(
                "/api/books", HttpMethod.POST,
                new HttpEntity<>(Map.of("title", "x", "author", "y", "isbn", "z", "category", "c", "availableCopies", 1), authHeaders(eveToken)),
                Map.class);
        assertThat(createBookAttempt.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Validation scenario: a MEMBER cannot read another member's profile (ownership check).
        Number otherMemberId = registerMember("frank.member@example.com");
        ResponseEntity<Map> crossAccountRead = restTemplate.exchange(
                "/api/members/" + otherMemberId, HttpMethod.GET, new HttpEntity<>(authHeaders(eveToken)), Map.class);
        assertThat(crossAccountRead.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Sanity check: the owner CAN read their own profile.
        ResponseEntity<Map> ownAccountRead = restTemplate.exchange(
                "/api/members/" + eveId, HttpMethod.GET, new HttpEntity<>(authHeaders(eveToken)), Map.class);
        assertThat(ownAccountRead.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Cross-feature integration validation: an unpaid fine blocks a new reservation.
        String librarianToken = loginAndGetToken("librarian@library.test", "librarian123");
        Number bookId = createBook(librarianToken, Map.of(
                "title", "Working Effectively with Legacy Code", "author", "Michael Feathers",
                "isbn", "ISBN-WORKFLOW-3", "category", "Programming", "availableCopies", 1));

        ResponseEntity<Map> checkout = restTemplate.exchange(
                "/api/transactions/checkout", HttpMethod.POST,
                new HttpEntity<>(Map.of("memberId", eveId, "resourceId", bookId, "dueDate", LocalDate.now().minusDays(2).toString()), authHeaders(librarianToken)),
                Map.class);
        assertThat(checkout.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Number transactionId = (Number) checkout.getBody().get("id");

        ResponseEntity<Map> checkin = restTemplate.exchange(
                "/api/transactions/" + transactionId + "/checkin", HttpMethod.POST, new HttpEntity<>(authHeaders(librarianToken)), Map.class);
        assertThat(checkin.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<List> fines = restTemplate.exchange(
                "/api/fines/member/" + eveId, HttpMethod.GET, new HttpEntity<>(authHeaders(eveToken)), List.class);
        assertThat(fines.getBody()).hasSize(1);

        // Book must legitimately reach 0 stock (task 2: new books always start fully available) -
        // a neutral third member checks out the only copy so it's genuinely out of stock for eve.
        Number secondBookId = createBook(librarianToken, Map.of(
                "title", "Domain-Driven Design", "author", "Eric Evans",
                "isbn", "ISBN-WORKFLOW-3B", "category", "Programming", "totalCopies", 1, "availableCopies", 1));
        Number neutralBorrowerId = registerMember("greta.borrower@example.com");
        ResponseEntity<Map> neutralCheckout = restTemplate.exchange(
                "/api/transactions/checkout", HttpMethod.POST,
                new HttpEntity<>(Map.of("memberId", neutralBorrowerId, "resourceId", secondBookId, "dueDate", LocalDate.now().plusDays(7).toString()), authHeaders(librarianToken)),
                Map.class);
        assertThat(neutralCheckout.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<Map> blockedReservation = restTemplate.exchange(
                "/api/reservations", HttpMethod.POST,
                new HttpEntity<>(Map.of("resourceId", secondBookId), authHeaders(eveToken)), Map.class);
        assertThat(blockedReservation.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat((String) blockedReservation.getBody().get("error")).containsIgnoringCase("unpaid fines");
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
