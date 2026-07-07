package edu.cit.becera.lrbms;

import edu.cit.becera.lrbms.entities.Member;
import edu.cit.becera.lrbms.repositories.MemberRepository;
import edu.cit.becera.lrbms.services.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthFlowTest {

    @Mock
    private MemberRepository memberRepository;

    private MemberService memberService;

    @BeforeEach
    void setUp() {
        memberService = new MemberService(memberRepository);
    }

    @Test
    void adminLoginShouldResolveRoleAndPersistAccount() {
        Member admin = new Member();
        admin.setFirstName("System");
        admin.setLastName("Admin");
        admin.setEmail("admin-role-flow@library.test");
        admin.setPassword("password123");
        admin.setRole("admin");

        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(memberRepository.findByEmail("admin-role-flow@library.test")).thenReturn(Optional.of(admin));

        Member saved = memberService.saveMember(admin);
        Member authenticated = memberService.login("admin-role-flow@library.test", "password123");

        assertNotNull(saved);
        assertNotNull(authenticated);
        assertEquals("ADMIN", saved.getRole());
        assertEquals("ADMIN", authenticated.getRole());
    }
}
