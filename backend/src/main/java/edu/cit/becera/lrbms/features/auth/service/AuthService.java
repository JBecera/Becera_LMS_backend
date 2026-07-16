package edu.cit.becera.lrbms.features.auth.service;

import edu.cit.becera.lrbms.entities.Member;
import edu.cit.becera.lrbms.features.auth.dto.LoginRequest;
import edu.cit.becera.lrbms.features.auth.model.AuthResponse;
import edu.cit.becera.lrbms.repositories.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder != null ? passwordEncoder : new BCryptPasswordEncoder();
    }

    public AuthResponse login(LoginRequest request) {
        if (request == null || request.getEmail() == null || request.getPassword() == null) {
            throw new IllegalArgumentException("Email and password are required");
        }

        String normalizedEmail = request.getEmail().trim().toLowerCase();
        String normalizedPassword = request.getPassword().trim();

        Optional<Member> candidate = memberRepository.findByEmail(normalizedEmail);
        if (candidate.isEmpty()) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        Member authenticated = candidate.get();
        String storedPassword = authenticated.getPassword();
        boolean passwordMatches = passwordEncoder.matches(normalizedPassword, storedPassword);

        if (!passwordMatches) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        return new AuthResponse(
                authenticated.getId(),
                authenticated.getFirstName(),
                authenticated.getLastName(),
                authenticated.getEmail(),
                authenticated.getRole()
        );
    }
}
