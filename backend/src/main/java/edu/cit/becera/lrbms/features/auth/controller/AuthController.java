package edu.cit.becera.lrbms.features.auth.controller;

import edu.cit.becera.lrbms.features.auth.dto.LoginRequest;
import edu.cit.becera.lrbms.features.auth.model.AuthResponse;
import edu.cit.becera.lrbms.features.auth.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(Map.of(
                    "id", response.getId(),
                    "firstName", response.getFirstName(),
                    "lastName", response.getLastName(),
                    "email", response.getEmail(),
                    "role", response.getRole()
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", ex.getMessage()));
        }
    }
}
