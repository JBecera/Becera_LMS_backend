package edu.cit.becera.lrbms.controllers;

import edu.cit.becera.lrbms.entities.Member;
import edu.cit.becera.lrbms.services.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class AuthController {

    private final MemberService service;

    public AuthController(MemberService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Member member) {
        Member authenticated = service.authenticate(member.getEmail(), member.getPassword());
        if (authenticated == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials"));
        }

        return ResponseEntity.ok(Map.of(
                "id", authenticated.getId(),
                "firstName", authenticated.getFirstName(),
                "lastName", authenticated.getLastName(),
                "email", authenticated.getEmail(),
                "role", authenticated.getRole()
        ));
    }
}
