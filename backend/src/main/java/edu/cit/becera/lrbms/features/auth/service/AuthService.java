package edu.cit.becera.lrbms.features.auth.service;

import edu.cit.becera.lrbms.entities.Member;
import edu.cit.becera.lrbms.features.auth.dto.LoginRequest;
import edu.cit.becera.lrbms.features.auth.model.AuthResponse;
import edu.cit.becera.lrbms.repositories.MemberRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final MemberRepository memberRepository;

    public AuthService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public AuthResponse login(LoginRequest request) {
        if (request == null || request.getEmail() == null || request.getPassword() == null) {
            throw new IllegalArgumentException("Email and password are required");
        }

        Member authenticated = memberRepository.findByEmail(request.getEmail())
                .filter(member -> member.getPassword().equals(request.getPassword()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        return new AuthResponse(
                authenticated.getId(),
                authenticated.getFirstName(),
                authenticated.getLastName(),
                authenticated.getEmail(),
                authenticated.getRole()
        );
    }
}
