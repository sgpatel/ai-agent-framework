package com.aiframework.controller;

import com.aiframework.entity.User;
import com.aiframework.service.UserManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
public class AdminController {

    @Autowired
    private UserManagementService userManagementService;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userManagementService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/users/{username}/promote")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> promoteToAdmin(@PathVariable String username) {
        boolean success = userManagementService.promoteUserToAdmin(username);

        return ResponseEntity.ok(Map.of(
            "success", success,
            "message", success ? "User promoted to admin successfully" : "User not found",
            "username", username
        ));
    }

    @PostMapping("/users/{username}/moderator")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> grantModeratorRole(@PathVariable String username) {
        boolean success = userManagementService.grantModeratorRole(username);

        return ResponseEntity.ok(Map.of(
            "success", success,
            "message", success ? "Moderator role granted successfully" : "User not found",
            "username", username
        ));
    }

    @PostMapping("/users/{username}/toggle-enabled")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> toggleUserEnabled(
            @PathVariable String username,
            @RequestBody Map<String, Boolean> request) {

        boolean enabled = request.getOrDefault("enabled", true);
        boolean success = userManagementService.toggleUserEnabled(username, enabled);

        return ResponseEntity.ok(Map.of(
            "success", success,
            "message", success ?
                (enabled ? "User enabled successfully" : "User disabled successfully") :
                "User not found",
            "username", username,
            "enabled", enabled
        ));
    }

    @PostMapping("/users/{username}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> resetPassword(
            @PathVariable String username,
            @RequestBody Map<String, String> request) {

        String newPassword = request.get("newPassword");
        if (newPassword == null || newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Password must be at least 6 characters long"
            ));
        }

        boolean success = userManagementService.resetUserPassword(username, newPassword);

        return ResponseEntity.ok(Map.of(
            "success", success,
            "message", success ? "Password reset successfully" : "User not found",
            "username", username
        ));
    }

    @GetMapping("/users/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable String role) {
        try {
            User.Role userRole = User.Role.valueOf(role.toUpperCase());
            List<User> users = userManagementService.getUsersByRole(userRole);
            return ResponseEntity.ok(users);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
