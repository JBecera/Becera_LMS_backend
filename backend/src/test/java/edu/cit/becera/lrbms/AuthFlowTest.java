package edu.cit.becera.lrbms;

import edu.cit.becera.lrbms.entities.Member;
import edu.cit.becera.lrbms.features.auth.dto.LoginRequest;
import edu.cit.becera.lrbms.features.auth.model.AuthResponse;
import edu.cit.becera.lrbms.features.auth.service.AuthService;
import edu.cit.becera.lrbms.features.membership.dto.CreateMemberRequest;
import edu.cit.becera.lrbms.features.membership.service.MembershipService;
import edu.cit.becera.lrbms.repositories.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthFlowTest {

    @Mock
    private MemberRepository memberRepository;

    private MembershipService membershipService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        membershipService = new MembershipService(memberRepository, encoder);
        authService = new AuthService(memberRepository, encoder);
    }

    @Test
    void adminLoginShouldResolveRoleAndPersistAccount() {
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> {
            Member member = invocation.getArgument(0);
            if (member.getId() == null) {
                member.setId(1L);
            }
            return member;
        });

        CreateMemberRequest request = new CreateMemberRequest();
        request.setFirstName("System");
        request.setLastName("Admin");
        request.setEmail("admin-role-flow@library.test");
        request.setPassword("password123");
        request.setRole("admin");

        // Only an ADMIN caller may create another admin/librarian account (FR-004/FR-005).
        Member saved = membershipService.createMember(request, "ADMIN");

        when(memberRepository.findByEmail("admin-role-flow@library.test")).thenReturn(Optional.of(saved));

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("admin-role-flow@library.test");
        loginRequest.setPassword("password123");
        AuthResponse authenticated = authService.login(loginRequest);

        assertNotNull(saved);
        assertNotNull(authenticated);
        assertEquals("ADMIN", saved.getRole());
        assertEquals("ADMIN", authenticated.getRole());
    }
}
