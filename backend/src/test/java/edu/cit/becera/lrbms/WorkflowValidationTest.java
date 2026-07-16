package edu.cit.becera.lrbms;

import edu.cit.becera.lrbms.entities.Book;
import edu.cit.becera.lrbms.entities.Member;
import edu.cit.becera.lrbms.features.auth.dto.LoginRequest;
import edu.cit.becera.lrbms.features.auth.service.AuthService;
import edu.cit.becera.lrbms.features.catalog.dto.BookRequest;
import edu.cit.becera.lrbms.features.catalog.service.CatalogService;
import edu.cit.becera.lrbms.features.membership.dto.CreateMemberRequest;
import edu.cit.becera.lrbms.features.membership.service.MembershipService;
import edu.cit.becera.lrbms.repositories.BookRepository;
import edu.cit.becera.lrbms.repositories.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkflowValidationTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private MembershipService membershipService;

    @Test
    void shouldHashPasswordAndRejectDuplicateEmail() {
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateMemberRequest request = new CreateMemberRequest();
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setEmail("jane@example.com");
        request.setPassword("secret123");
        request.setRole("MEMBER");

        Member saved = membershipService.createMember(request, null);

        assertNotNull(saved);
        assertNotEquals("secret123", saved.getPassword());
        assertTrue(saved.getPassword().startsWith("$2a$"));
    }

    @Test
    void shouldValidateBookCreation() {
        BookRequest request = new BookRequest();
        request.setTitle("Clean Code");
        request.setAuthor("Robert C. Martin");
        request.setIsbn("9780132350884");
        request.setCategory("Programming");
        request.setDescription("Classic software engineering reference");
        request.setAvailableCopies(1);

        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CatalogService catalogService = new CatalogService(bookRepository);
        Book created = catalogService.createBook(request);

        assertEquals(1, created.getAvailableCopies());
        assertEquals("Clean Code", created.getTitle());
    }

    @Test
    void shouldRejectInvalidLoginCredentials() {
        Member member = new Member();
        member.setEmail("jane@example.com");
        member.setPassword(new BCryptPasswordEncoder().encode("secret123"));
        member.setFirstName("Jane");
        member.setLastName("Doe");
        member.setRole("MEMBER");

        when(memberRepository.findByEmail("jane@example.com")).thenReturn(java.util.Optional.of(member));

        AuthService authService = new AuthService(memberRepository, new BCryptPasswordEncoder());
        LoginRequest request = new LoginRequest();
        request.setEmail("jane@example.com");
        request.setPassword("wrong-password");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> authService.login(request));
        assertEquals("Invalid credentials", exception.getMessage());
    }
}
