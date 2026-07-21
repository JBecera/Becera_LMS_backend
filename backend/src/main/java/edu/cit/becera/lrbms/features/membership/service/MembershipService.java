package edu.cit.becera.lrbms.features.membership.service;

import edu.cit.becera.lrbms.entities.Member;
import edu.cit.becera.lrbms.features.membership.dto.ChangePasswordRequest;
import edu.cit.becera.lrbms.features.membership.dto.CreateMemberRequest;
import edu.cit.becera.lrbms.repositories.MemberRepository;
import edu.cit.becera.lrbms.security.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MembershipService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public MembershipService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder != null ? passwordEncoder : new BCryptPasswordEncoder();
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

    /**
     * creatorRole is null for anonymous self-registration. Only an ADMIN caller may request a
     * role other than MEMBER (creating LIBRARIAN/ADMIN accounts) — everyone else is forced to MEMBER,
     * regardless of what the request body asks for.
     */
    public Member createMember(CreateMemberRequest request, String creatorRole) {
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

        String role = "ADMIN".equalsIgnoreCase(creatorRole) ? normalizeRole(request.getRole()) : "MEMBER";

        Member member = new Member();
        member.setFirstName(request.getFirstName());
        member.setLastName(request.getLastName());
        member.setEmail(request.getEmail().trim().toLowerCase());
        member.setPassword(hashPassword(request.getPassword()));
        member.setRole(role);
        member.setPhoneNumber(request.getPhoneNumber());
        member.setAddress(request.getAddress());

        Member saved;
        try {
            saved = memberRepository.save(member);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("A user with this email already exists", ex);
        }

        if ("MEMBER".equals(saved.getRole()) && saved.getMemberId() == null) {
            saved.setMemberId(String.format("MEM-%06d", saved.getId()));
            saved = memberRepository.save(saved);
        }

        return saved;
    }

    public Optional<Member> getMember(Long id) {
        return memberRepository.findById(id);
    }

    public Member updateMember(Long id, CreateMemberRequest request, CurrentUser requester) {
        Member member = memberRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Member not found"));

        boolean isSelf = requester != null && requester.owns(id);
        boolean isStaff = requester != null && requester.isStaff();
        if (!isSelf && !isStaff) {
            throw new AccessDeniedException("Not allowed to update this account");
        }

        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            member.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            member.setLastName(request.getLastName());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            member.setEmail(request.getEmail().trim().toLowerCase());
        }
        if (request.getPhoneNumber() != null) {
            member.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getAddress() != null) {
            member.setAddress(request.getAddress());
        }
        if (requester != null && requester.isAdmin() && request.getRole() != null && !request.getRole().isBlank()) {
            member.setRole(normalizeRole(request.getRole()));
        }

        try {
            return memberRepository.save(member);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("A user with this email already exists", ex);
        }
    }

    /**
     * Self-service changes must prove knowledge of the current password; a staff member resetting
     * someone else's password is trusted on their role instead (no current password on file to check).
     */
    public Member changePassword(Long id, ChangePasswordRequest request, CurrentUser requester) {
        Member member = memberRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Member not found"));

        boolean isSelf = requester != null && requester.owns(id);
        boolean isStaff = requester != null && requester.isStaff();
        if (!isSelf && !isStaff) {
            throw new AccessDeniedException("Not allowed to change this password");
        }

        if (request == null || request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            throw new IllegalArgumentException("New password is required");
        }
        if (request.getNewPassword().length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters long");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirmation do not match");
        }

        if (isSelf) {
            if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()) {
                throw new IllegalArgumentException("Current password is required");
            }
            if (!passwordEncoder.matches(request.getCurrentPassword(), member.getPassword())) {
                throw new IllegalArgumentException("Current password is incorrect");
            }
        }

        member.setPassword(hashPassword(request.getNewPassword()));
        return memberRepository.save(member);
    }

    public void deleteMember(Long id) {
        memberRepository.deleteById(id);
    }

    private String hashPassword(String password) {
        if (password == null || password.isBlank()) {
            return "";
        }
        if (passwordEncoder != null) {
            return passwordEncoder.encode(password);
        }
        return password;
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
