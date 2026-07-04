package edu.cit.becera.lrbms.services;

import edu.cit.becera.lrbms.entities.Member;
import edu.cit.becera.lrbms.repositories.MemberRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MemberService {

    private final MemberRepository repository;

    public MemberService(MemberRepository repository) {
        this.repository = repository;
    }

    public List<Member> getAllMembers() {
        return repository.findAll();
    }

    public Member saveMember(Member member) {
        if (member == null) {
            throw new IllegalArgumentException("Member data is required");
        }

        if (member.getEmail() == null || member.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (member.getPassword() == null || member.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        try {
            return repository.save(member);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("A user with this email already exists", ex);
        }
    }

    public Optional<Member> getMember(Long id) {
        return repository.findById(id);
    }

    public Member updateMember(Long id, Member updatedMember) {

        Member member = repository.findById(id).orElseThrow();

        member.setFirstName(updatedMember.getFirstName());
        member.setLastName(updatedMember.getLastName());
        member.setEmail(updatedMember.getEmail());
        member.setPassword(updatedMember.getPassword());

        return repository.save(member);
    }

    public void deleteMember(Long id) {
        repository.deleteById(id);
    }

    public Member login(String email, String password) {

        Optional<Member> member = repository.findByEmail(email);

        if (member.isPresent() && member.get().getPassword().equals(password)) {
            return member.get();
        }

        return null;
    }
}