package com.aiframework.service;

import com.aiframework.dto.auth.AuthResponse;
import com.aiframework.dto.auth.LoginRequest;
import com.aiframework.dto.auth.RegisterRequest;
import com.aiframework.entity.User;
import com.aiframework.repository.UserRepository;
import com.aiframework.security.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {
        try {
            log.info("Attempting login for user: {}", loginRequest.getUsernameOrEmail());

            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsernameOrEmail(),
                    loginRequest.getPassword()
                )
            );

            String jwt = jwtUtils.generateJwtToken(authentication);
            User user = (User) authentication.getPrincipal();
            
            // Update last login time
            user.updateLastLogin();
            userRepository.save(user);

            log.info("User {} logged in successfully", user.getUsername());
            return AuthResponse.success(jwt, user, "Login successful");

        } catch (AuthenticationException e) {
            log.warn("Login failed for user: {} - {}", loginRequest.getUsernameOrEmail(), e.getMessage());
            return AuthResponse.error("Invalid username/email or password");
        } catch (Exception e) {
            log.error("Login error for user: {}", loginRequest.getUsernameOrEmail(), e);
            return AuthResponse.error("An error occurred during login");
        }
    }

    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        try {
            log.info("Attempting registration for user: {}", registerRequest.getUsername());

            // Validate password match
            if (!registerRequest.isPasswordMatching()) {
                return AuthResponse.error("Passwords do not match");
            }

            // Check if username already exists
            if (userRepository.existsByUsername(registerRequest.getUsername())) {
                return AuthResponse.error("Username is already taken");
            }

            // Check if email already exists
            if (userRepository.existsByEmail(registerRequest.getEmail())) {
                return AuthResponse.error("Email is already registered");
            }

            // Create new user
            User user = User.builder()
                    .username(registerRequest.getUsername())
                    .email(registerRequest.getEmail())
                    .password(passwordEncoder.encode(registerRequest.getPassword()))
                    .firstName(registerRequest.getFirstName())
                    .lastName(registerRequest.getLastName())
                    .phoneNumber(registerRequest.getPhoneNumber())
                    .role(User.Role.USER)
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .build();

            User savedUser = userRepository.save(user);
            log.info("User {} registered successfully", savedUser.getUsername());

            // Generate JWT token for immediate login after registration
            String jwt = jwtUtils.generateJwtToken(savedUser.getUsername());
            
            return AuthResponse.success(jwt, savedUser, "Registration successful");

        } catch (Exception e) {
            log.error("Registration error for user: {}", registerRequest.getUsername(), e);
            return AuthResponse.error("An error occurred during registration");
        }
    }

    public AuthResponse getCurrentUser(String username) {
        try {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                return AuthResponse.builder()
                        .user(AuthResponse.UserInfo.fromUser(user))
                        .success(true)
                        .message("User found")
                        .timestamp(LocalDateTime.now())
                        .build();
            } else {
                return AuthResponse.error("User not found");
            }
        } catch (Exception e) {
            log.error("Error fetching current user: {}", username, e);
            return AuthResponse.error("Error fetching user information");
        }
    }

    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }
}
