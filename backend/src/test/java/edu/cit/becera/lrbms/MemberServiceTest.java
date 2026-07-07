package edu.cit.becera.lrbms;

import edu.cit.becera.lrbms.entities.Member;
import edu.cit.becera.lrbms.repositories.MemberRepository;
import edu.cit.becera.lrbms.services.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    private MemberService memberService;

    @BeforeEach
    void setUp() {
        memberService = new MemberService(memberRepository);
    }

    @Test
    void getMembersByRoleShouldReturnOnlyMatchingAccounts() {
        Member admin = new Member();
        admin.setRole("ADMIN");
        Member librarian = new Member();
        librarian.setRole("LIBRARIAN");
        Member member = new Member();
        member.setRole("MEMBER");

        when(memberRepository.findAll()).thenReturn(List.of(admin, librarian, member));

        List<Member> result = memberService.getMembersByRole("LIBRARIAN");

        assertEquals(1, result.size());
        assertEquals("LIBRARIAN", result.get(0).getRole());
    }
}
