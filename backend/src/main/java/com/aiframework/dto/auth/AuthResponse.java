package com.aiframework.dto.auth;

import com.aiframework.entity.User;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;
    private String tokenType = "Bearer";
    private UserInfo user;
    private boolean success;
    private String message;
    private LocalDateTime timestamp;

    public static AuthResponse success(String token, User user, String message) {
        return AuthResponse.builder()
                .token(token)
                .user(UserInfo.fromUser(user))
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static AuthResponse error(String message) {
        return AuthResponse.builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Data
    @Builder
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String fullName;
        private String role;
        private String profilePictureUrl;
        private LocalDateTime lastLogin;

        public static UserInfo fromUser(User user) {
            return UserInfo.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .fullName(user.getFullName())
                    .role(user.getRole().name())
                    .profilePictureUrl(user.getProfilePictureUrl())
                    .lastLogin(user.getLastLogin())
                    .build();
        }
    }
}
