package com.aiframework.service;

import com.aiframework.entity.User;
import com.aiframework.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserManagementService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Create default admin user if none exists
     */
    @PostConstruct
    @Transactional
    public void createDefaultAdminUser() {
        if (userRepository.count() == 0) {
            log.info("No users found in database. Creating default admin user...");

            User adminUser = User.builder()
                    .username("admin")
                    .email("admin@aiframework.com")
                    .password(passwordEncoder.encode("admin123"))
                    .firstName("System")
                    .lastName("Administrator")
                    .role(User.Role.ADMIN)
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .build();

            userRepository.save(adminUser);
            log.info("Default admin user created: username=admin, password=admin123");
        }
    }

    /**
     * Promote a user to admin role
     */
    @Transactional
    public boolean promoteUserToAdmin(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setRole(User.Role.ADMIN);
            userRepository.save(user);
            log.info("User {} promoted to ADMIN role", username);
            return true;
        }
        return false;
    }

    /**
     * Grant moderator role to user
     */
    @Transactional
    public boolean grantModeratorRole(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setRole(User.Role.MODERATOR);
            userRepository.save(user);
            log.info("User {} granted MODERATOR role", username);
            return true;
        }
        return false;
    }

    /**
     * Enable/disable user account
     */
    @Transactional
    public boolean toggleUserEnabled(String username, boolean enabled) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setEnabled(enabled);
            userRepository.save(user);
            log.info("User {} account {}", username, enabled ? "enabled" : "disabled");
            return true;
        }
        return false;
    }

    /**
     * Get all users with their roles
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Get users by role
     */
    public List<User> getUsersByRole(User.Role role) {
        return userRepository.findByRole(role);
    }

    /**
     * Reset user password (admin function)
     */
    @Transactional
    public boolean resetUserPassword(String username, String newPassword) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            log.info("Password reset for user {}", username);
            return true;
        }
        return false;
    }
}
