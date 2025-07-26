package com.aiframework.controller;

import com.aiframework.dto.auth.AuthResponse;
import com.aiframework.dto.auth.LoginRequest;
import com.aiframework.dto.auth.RegisterRequest;
import com.aiframework.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
//@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Login attempt for user: {}", loginRequest.getUsernameOrEmail());

        AuthResponse response = authService.login(loginRequest);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("Registration attempt for user: {}", registerRequest.getUsername());

        AuthResponse response = authService.register(registerRequest);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() &&
            !authentication.getPrincipal().equals("anonymousUser")) {

            String username = authentication.getName();
            AuthResponse response = authService.getCurrentUser(username);

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(AuthResponse.error("User not authenticated"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        // Since we're using stateless JWT, logout is handled on the client side
        // by simply removing the token from storage
        log.info("User logout requested");

        return ResponseEntity.ok(Map.of(
            "message", "Logout successful",
            "success", "true"
        ));
    }

    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Boolean>> checkUsernameAvailability(@RequestParam String username) {
        boolean available = authService.isUsernameAvailable(username);

        return ResponseEntity.ok(Map.of(
            "available", available
        ));
    }

    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmailAvailability(@RequestParam String email) {
        boolean available = authService.isEmailAvailable(email);

        return ResponseEntity.ok(Map.of(
            "available", available
        ));
    }

    @GetMapping("/validate-token")
    public ResponseEntity<Map<String, Object>> validateToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        boolean isValid = authentication != null && authentication.isAuthenticated() &&
                         !authentication.getPrincipal().equals("anonymousUser");

        return ResponseEntity.ok(Map.of(
            "valid", isValid,
            "authenticated", isValid,
            "username", isValid ? authentication.getName() : null
        ));
    }
}
