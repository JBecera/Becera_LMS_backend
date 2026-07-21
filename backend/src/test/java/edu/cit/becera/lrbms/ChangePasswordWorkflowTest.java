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
 * Self-service password changes must prove the caller knows the current password, while a staff
 * member resetting someone else's account is trusted on role alone. Covers the security fix that
 * separated password changes from the general profile-update endpoint.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:workflow4;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
class ChangePasswordWorkflowTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @SuppressWarnings("unchecked")
    @Test
    void selfServiceRequiresCurrentPasswordAndStaffCanResetWithoutIt() {
        Number memberId = registerMember("dana.member@example.com", "original1");
        String memberToken = loginAndGetToken("dana.member@example.com", "original1");

        // Wrong current password is rejected.
        ResponseEntity<Map> wrongCurrent = restTemplate.exchange(
                "/api/members/" + memberId + "/password", HttpMethod.PUT,
                new HttpEntity<>(Map.of("currentPassword", "not-the-password", "newPassword", "newpass1", "confirmPassword", "newpass1"), authHeaders(memberToken)),
                Map.class);
        assertThat(wrongCurrent.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat((String) wrongCurrent.getBody().get("error")).containsIgnoringCase("current password is incorrect");

        // Mismatched confirmation is rejected.
        ResponseEntity<Map> mismatch = restTemplate.exchange(
                "/api/members/" + memberId + "/password", HttpMethod.PUT,
                new HttpEntity<>(Map.of("currentPassword", "original1", "newPassword", "newpass1", "confirmPassword", "different1"), authHeaders(memberToken)),
                Map.class);
        assertThat(mismatch.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat((String) mismatch.getBody().get("error")).containsIgnoringCase("do not match");

        // Too-short password is rejected.
        ResponseEntity<Map> tooShort = restTemplate.exchange(
                "/api/members/" + memberId + "/password", HttpMethod.PUT,
                new HttpEntity<>(Map.of("currentPassword", "original1", "newPassword", "short", "confirmPassword", "short"), authHeaders(memberToken)),
                Map.class);
        assertThat(tooShort.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // Correct current password + matching confirmation succeeds.
        ResponseEntity<Map> success = restTemplate.exchange(
                "/api/members/" + memberId + "/password", HttpMethod.PUT,
                new HttpEntity<>(Map.of("currentPassword", "original1", "newPassword", "newpass1", "confirmPassword", "newpass1"), authHeaders(memberToken)),
                Map.class);
        assertThat(success.getStatusCode()).isEqualTo(HttpStatus.OK);

        // The old password no longer works; the new one does.
        assertLoginFails("dana.member@example.com", "original1");
        String refreshedToken = loginAndGetToken("dana.member@example.com", "newpass1");
        assertThat(refreshedToken).isNotBlank();

        // A plain member cannot change someone else's password even with a correct-looking payload.
        Number otherMemberId = registerMember("other.member@example.com", "otherpass1");
        ResponseEntity<Map> crossAccountAttempt = restTemplate.exchange(
                "/api/members/" + otherMemberId + "/password", HttpMethod.PUT,
                new HttpEntity<>(Map.of("currentPassword", "otherpass1", "newPassword", "hijacked1", "confirmPassword", "hijacked1"), authHeaders(refreshedToken)),
                Map.class);
        assertThat(crossAccountAttempt.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // A librarian (staff) can reset that member's password without knowing the current one.
        String librarianToken = loginAndGetToken("librarian@library.test", "librarian123");
        ResponseEntity<Map> staffReset = restTemplate.exchange(
                "/api/members/" + otherMemberId + "/password", HttpMethod.PUT,
                new HttpEntity<>(Map.of("newPassword", "resetbystaff1", "confirmPassword", "resetbystaff1"), authHeaders(librarianToken)),
                Map.class);
        assertThat(staffReset.getStatusCode()).isEqualTo(HttpStatus.OK);
        String resetToken = loginAndGetToken("other.member@example.com", "resetbystaff1");
        assertThat(resetToken).isNotBlank();

        // The general profile-update endpoint no longer accepts a password field at all.
        ResponseEntity<Map> profileUpdateAttempt = restTemplate.exchange(
                "/api/members/" + memberId, HttpMethod.PUT,
                new HttpEntity<>(Map.of("firstName", "Dana", "lastName", "Member", "password", "sneakychange1"), authHeaders(refreshedToken)),
                Map.class);
        assertThat(profileUpdateAttempt.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertLoginFails("dana.member@example.com", "sneakychange1");
    }

    /**
     * The JDK's HttpURLConnection (used under TestRestTemplate here) tries to retry-with-auth on any
     * 401 response to a streamed POST body and can't resend it, surfacing as a ResourceAccessException
     * instead of a clean 401 response. Either outcome proves the credentials were rejected.
     */
    @SuppressWarnings("unchecked")
    private void assertLoginFails(String email, String password) {
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "/api/auth/login", Map.of("email", email, "password", password), Map.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        } catch (org.springframework.web.client.ResourceAccessException expected) {
            assertThat(expected.getMessage()).containsIgnoringCase("authentication");
        }
    }

    private Number registerMember(String email, String password) {
        Map<String, Object> request = Map.of(
                "firstName", "Test", "lastName", "Member", "email", email, "password", password);
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
