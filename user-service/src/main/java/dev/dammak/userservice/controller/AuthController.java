package dev.dammak.userservice.controller;

import dev.dammak.userservice.dto.AuthResponseDto;
import dev.dammak.userservice.dto.UserLoginDto;
import dev.dammak.userservice.dto.UserProfileDto;
import dev.dammak.userservice.dto.UserRegistrationDto;
import dev.dammak.userservice.service.AuthService;
import dev.dammak.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserProfileDto> register(@Valid @RequestBody UserRegistrationDto registrationDto) {
        UserProfileDto userProfile = userService.registerUser(registrationDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(userProfile);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody UserLoginDto loginDto) {
        AuthResponseDto authResponse = authService.login(loginDto);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestParam String token) {
        userService.verifyEmail(token);
        return ResponseEntity.ok(Map.of("message", "Email verified successfully"));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        // Since we're using stateless JWT, logout is handled on the client side
        // In a production environment, you might want to implement a token blacklist
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}