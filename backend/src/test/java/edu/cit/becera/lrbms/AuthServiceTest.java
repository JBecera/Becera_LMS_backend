package edu.cit.becera.lrbms;

import edu.cit.becera.lrbms.entities.Member;
import edu.cit.becera.lrbms.features.auth.dto.LoginRequest;
import edu.cit.becera.lrbms.features.auth.model.AuthResponse;
import edu.cit.becera.lrbms.features.auth.service.AuthService;
import edu.cit.becera.lrbms.repositories.MemberRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    @Test
    void shouldAcceptTrimmedAndCaseInsensitiveEmailAndPassword() {
        MemberRepository repository = mock(MemberRepository.class);
        Member member = new Member();
        member.setEmail("jane@example.com");
        member.setPassword(new BCryptPasswordEncoder().encode("secret123"));
        member.setFirstName("Jane");
        member.setLastName("Doe");
        member.setRole("MEMBER");

        when(repository.findByEmail("jane@example.com")).thenReturn(java.util.Optional.of(member));

        AuthService service = new AuthService(repository, null);
        LoginRequest request = new LoginRequest();
        request.setEmail("  JANE@EXAMPLE.COM  ");
        request.setPassword(" secret123 ");

        AuthResponse response = service.login(request);

        assertNotNull(response);
        assertEquals("jane@example.com", response.getEmail());
        assertEquals("Jane", response.getFirstName());
    }
}
