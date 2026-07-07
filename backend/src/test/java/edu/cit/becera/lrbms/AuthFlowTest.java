package edu.cit.becera.lrbms;

import edu.cit.becera.lrbms.entities.Member;
import edu.cit.becera.lrbms.services.MemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class AuthFlowTest {

    @Autowired
    private MemberService memberService;

    @Test
    void adminLoginShouldResolveRoleAndPersistAccount() {
        Member admin = new Member();
        admin.setFirstName("System");
        admin.setLastName("Admin");
        admin.setEmail("admin@library.test");
        admin.setPassword("password123");
        admin.setRole("ADMIN");

        Member saved = memberService.saveMember(admin);
        Member authenticated = memberService.login("admin@library.test", "password123");

        assertNotNull(saved);
        assertNotNull(authenticated);
        assertEquals("ADMIN", authenticated.getRole());
    }
}
