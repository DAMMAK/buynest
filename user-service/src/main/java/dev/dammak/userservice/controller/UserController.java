package dev.dammak.userservice.controller;


import dev.dammak.userservice.dto.UserProfileDto;
import dev.dammak.userservice.entity.User;
import dev.dammak.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<UserProfileDto> getUserProfile(@AuthenticationPrincipal UserDetails userDetails) {
        // Extract user ID from JWT token or use email to find user
        // For simplicity, we'll use email here
        UserProfileDto profile = userService.findByEmail(userDetails.getUsername())
                .map(user -> userService.getUserProfile(user.getId()))
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<UserProfileDto> updateUserProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UserProfileDto profileDto) {

        UUID userId = userService.findByEmail(userDetails.getUsername())
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfileDto updatedProfile = userService.updateUserProfile(userId, profileDto);
        return ResponseEntity.ok(updatedProfile);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserProfileDto> getUserById(@PathVariable UUID userId) {
        UserProfileDto profile = userService.getUserProfile(userId);
        return ResponseEntity.ok(profile);
    }
}