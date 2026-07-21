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
 * End-to-end workflow: a member books an in-stock title entirely online via self-checkout - no
 * librarian mediation - and the loan immediately shows up in their borrowing history. Also covers
 * the "no copies available", "already has an active loan for this title", and bad-return-date
 * rejections.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:workflow5;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
class SelfCheckoutWorkflowTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @SuppressWarnings("unchecked")
    @Test
    void memberSelfChecksOutAnInStockTitleWithoutALibrarian() {
        String librarianToken = loginAndGetToken("librarian@library.test", "librarian123");

        // A title-unique-enough name to not collide with the BSIT demo catalog seeded at startup.
        Map<String, Object> bookRequest = Map.of(
                "title", "Zephyr Systems Handbook", "author", "Test Author", "isbn", "ISBN-WORKFLOW-5A",
                "category", "Software Design", "totalCopies", 1, "availableCopies", 1);
        ResponseEntity<Map> bookResponse = restTemplate.exchange(
                "/api/books", HttpMethod.POST, new HttpEntity<>(bookRequest, authHeaders(librarianToken)), Map.class);
        assertThat(bookResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Number bookId = (Number) bookResponse.getBody().get("id");

        Number memberId = registerMember("kiko.member@example.com");
        String memberToken = loginAndGetToken("kiko.member@example.com", "pass1234");

        // Self-checkout succeeds, decrements stock, and needs no librarian involvement at all.
        ResponseEntity<Map> selfCheckout = restTemplate.exchange(
                "/api/transactions/self-checkout", HttpMethod.POST,
                new HttpEntity<>(Map.of("resourceId", bookId, "dueDate", LocalDate.now().plusDays(7).toString()), authHeaders(memberToken)),
                Map.class);
        assertThat(selfCheckout.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(selfCheckout.getBody().get("status")).isEqualTo("ACTIVE");

        ResponseEntity<List> catalogAfterCheckout = restTemplate.getForEntity("/api/books/search?query=Zephyr Systems Handbook", List.class);
        Map<String, Object> updatedBook = (Map<String, Object>) catalogAfterCheckout.getBody().get(0);
        assertThat(((Number) updatedBook.get("availableCopies")).intValue()).isEqualTo(0);

        // It immediately shows up in the member's own borrowing history.
        ResponseEntity<List> history = restTemplate.exchange(
                "/api/transactions/member/" + memberId, HttpMethod.GET, new HttpEntity<>(authHeaders(memberToken)), List.class);
        assertThat(history.getBody()).hasSize(1);

        // The same member cannot self-checkout the exact same title again while already borrowing it.
        ResponseEntity<Map> duplicateSelfCheckout = restTemplate.exchange(
                "/api/transactions/self-checkout", HttpMethod.POST,
                new HttpEntity<>(Map.of("resourceId", bookId, "dueDate", LocalDate.now().plusDays(7).toString()), authHeaders(memberToken)),
                Map.class);
        assertThat(duplicateSelfCheckout.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat((String) duplicateSelfCheckout.getBody().get("error")).containsIgnoringCase("active loan");

        // No copies left - a different member is rejected outright (no waitlist fallback here).
        Number secondMemberId = registerMember("second.member@example.com");
        String secondMemberToken = loginAndGetToken("second.member@example.com", "pass1234");
        ResponseEntity<Map> secondSelfCheckout = restTemplate.exchange(
                "/api/transactions/self-checkout", HttpMethod.POST,
                new HttpEntity<>(Map.of("resourceId", bookId, "dueDate", LocalDate.now().plusDays(7).toString()), authHeaders(secondMemberToken)),
                Map.class);
        assertThat(secondSelfCheckout.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        // Sanity check: a bad return date (today, not after) is rejected with a 400.
        ResponseEntity<Map> badDueDate = restTemplate.exchange(
                "/api/transactions/self-checkout", HttpMethod.POST,
                new HttpEntity<>(Map.of("resourceId", bookId, "dueDate", LocalDate.now().toString()), authHeaders(secondMemberToken)),
                Map.class);
        assertThat(badDueDate.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(memberId).isNotNull();
        assertThat(secondMemberId).isNotNull();
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
