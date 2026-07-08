package edu.cit.becera.lrbms.features.membership.service;

import edu.cit.becera.lrbms.entities.Member;
import edu.cit.becera.lrbms.features.membership.dto.CreateMemberRequest;
import edu.cit.becera.lrbms.repositories.MemberRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MembershipService {
    private final MemberRepository memberRepository;

    public MembershipService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    public List<Member> getMembersByRole(String role) {
        String normalized = normalizeRole(role);
        return memberRepository.findAll().stream()
                .filter(member -> normalized.equalsIgnoreCase(member.getRole()))
                .toList();
    }

    public Member createMember(CreateMemberRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Member data is required");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (request.getFirstName() == null || request.getFirstName().isBlank()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (request.getLastName() == null || request.getLastName().isBlank()) {
            throw new IllegalArgumentException("Last name is required");
        }

        Member member = new Member();
        member.setFirstName(request.getFirstName());
        member.setLastName(request.getLastName());
        member.setEmail(request.getEmail());
        member.setPassword(request.getPassword());
        member.setRole(normalizeRole(request.getRole()));

        try {
            return memberRepository.save(member);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("A user with this email already exists", ex);
        }
    }

    public Optional<Member> getMember(Long id) {
        return memberRepository.findById(id);
    }

    public Member updateMember(Long id, CreateMemberRequest request) {
        Member member = memberRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Member not found"));
        member.setFirstName(request.getFirstName());
        member.setLastName(request.getLastName());
        member.setEmail(request.getEmail());
        member.setPassword(request.getPassword());
        member.setRole(normalizeRole(request.getRole()));
        return memberRepository.save(member);
    }

    public void deleteMember(Long id) {
        memberRepository.deleteById(id);
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "MEMBER";
        }
        String normalized = role.trim().toUpperCase();
        return switch (normalized) {
            case "ADMIN", "LIBRARIAN", "MEMBER" -> normalized;
            default -> "MEMBER";
        };
    }
}
