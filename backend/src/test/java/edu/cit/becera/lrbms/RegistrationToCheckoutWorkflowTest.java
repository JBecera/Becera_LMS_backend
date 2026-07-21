package edu.cit.becera.lrbms;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
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
 * End-to-end workflow #1: a new member self-registers, logs in, searches the public catalog,
 * and a librarian checks a book out to them and later checks it back in. Exercises FR-001
 * (login), FR-002 (search), FR-005 (registration), FR-006 (catalog CRUD) and FR-007
 * (check-in/check-out) as one integrated path through Member, Book and Transaction.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:workflow1;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
class RegistrationToCheckoutWorkflowTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @SuppressWarnings("unchecked")
    @Test
    void memberRegistersLogsInAndLibrarianChecksOutAndInABook() {
        // Step 1: self-registration (FR-005) - anonymous, no auth required
        Map<String, Object> registerRequest = Map.of(
                "firstName", "Wanda",
                "lastName", "Reader",
                "email", "wanda.reader@example.com",
                "password", "pass1234"
        );
        ResponseEntity<Map> registerResponse = restTemplate.postForEntity("/api/members", registerRequest, Map.class);
        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(registerResponse.getBody().get("role")).isEqualTo("MEMBER");
        assertThat(registerResponse.getBody().get("memberId")).isNotNull();
        Number memberId = (Number) registerResponse.getBody().get("id");

        // Step 2: login (FR-001)
        Map<String, Object> loginRequest = Map.of("email", "wanda.reader@example.com", "password", "pass1234");
        ResponseEntity<Map> memberLogin = restTemplate.postForEntity("/api/auth/login", loginRequest, Map.class);
        assertThat(memberLogin.getStatusCode()).isEqualTo(HttpStatus.OK);
        String memberToken = (String) memberLogin.getBody().get("token");
        assertThat(memberToken).isNotBlank();

        // Step 3: public catalog search works without authentication (FR-002)
        ResponseEntity<List> searchResponse = restTemplate.getForEntity("/api/books/search?query=", List.class);
        assertThat(searchResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Step 4: librarian logs in (seeded dev account) and adds a book (FR-006)
        Map<String, Object> librarianLoginRequest = Map.of("email", "librarian@library.test", "password", "librarian123");
        ResponseEntity<Map> librarianLogin = restTemplate.postForEntity("/api/auth/login", librarianLoginRequest, Map.class);
        assertThat(librarianLogin.getStatusCode()).isEqualTo(HttpStatus.OK);
        String librarianToken = (String) librarianLogin.getBody().get("token");

        Map<String, Object> bookRequest = Map.of(
                "title", "Clean Architecture",
                "author", "Robert C. Martin",
                "isbn", "ISBN-WORKFLOW-1",
                "category", "Programming",
                "availableCopies", 1
        );
        ResponseEntity<Map> bookResponse = restTemplate.exchange(
                "/api/books", HttpMethod.POST, new HttpEntity<>(bookRequest, authHeaders(librarianToken)), Map.class);
        assertThat(bookResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Number bookId = (Number) bookResponse.getBody().get("id");

        // Step 5: librarian checks the book out to the member (FR-007)
        Map<String, Object> checkoutRequest = Map.of(
                "memberId", memberId,
                "resourceId", bookId,
                "dueDate", LocalDate.now().plusDays(7).toString()
        );
        ResponseEntity<Map> checkoutResponse = restTemplate.exchange(
                "/api/transactions/checkout", HttpMethod.POST, new HttpEntity<>(checkoutRequest, authHeaders(librarianToken)), Map.class);
        assertThat(checkoutResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(checkoutResponse.getBody().get("status")).isEqualTo("ACTIVE");
        Number transactionId = (Number) checkoutResponse.getBody().get("id");

        // Step 6: the member sees the loan on their own account (ownership-scoped read)
        ResponseEntity<List> memberTransactions = restTemplate.exchange(
                "/api/transactions/member/" + memberId, HttpMethod.GET, new HttpEntity<>(authHeaders(memberToken)), List.class);
        assertThat(memberTransactions.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(memberTransactions.getBody()).hasSize(1);
        Map<String, Object> loan = (Map<String, Object>) memberTransactions.getBody().get(0);
        assertThat(loan.get("resourceTitle")).isEqualTo("Clean Architecture");

        // Step 7: librarian checks the book back in (FR-007) and availability is restored
        ResponseEntity<Map> checkinResponse = restTemplate.exchange(
                "/api/transactions/" + transactionId + "/checkin", HttpMethod.POST, new HttpEntity<>(authHeaders(librarianToken)), Map.class);
        assertThat(checkinResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(checkinResponse.getBody().get("status")).isEqualTo("RETURNED");

        ResponseEntity<List> catalogAfterReturn = restTemplate.getForEntity("/api/books/search?query=Clean Architecture", List.class);
        Map<String, Object> returnedBook = (Map<String, Object>) catalogAfterReturn.getBody().get(0);
        assertThat(((Number) returnedBook.get("availableCopies")).intValue()).isEqualTo(1);
    }

    private HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }
}
