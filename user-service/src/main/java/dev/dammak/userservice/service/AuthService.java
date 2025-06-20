package dev.dammak.userservice.service;


import dev.dammak.userservice.dto.AuthResponseDto;
import dev.dammak.userservice.dto.UserLoginDto;
import dev.dammak.userservice.dto.UserProfileDto;
import dev.dammak.userservice.entity.User;
import dev.dammak.userservice.exception.UserException;
import dev.dammak.userservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthResponseDto login(UserLoginDto loginDto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDto.getEmail(),
                            loginDto.getPassword()
                    )
            );

            User user = userService.findByEmail(loginDto.getEmail())
                    .orElseThrow(() -> new UserException("User not found"));

            if (!user.getIsEmailVerified()) {
                throw new UserException("Email not verified. Please verify your email before logging in.");
            }

            if (!user.getIsActive()) {
                throw new UserException("Account is deactivated. Please contact support.");
            }

            String token = jwtUtil.generateToken(user);
            Long expirationTime = jwtUtil.getExpirationTime();

            UserProfileDto userProfile = UserProfileDto.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .phoneNumber(user.getPhoneNumber())
                    .role(user.getRole())
                    .isEmailVerified(user.getIsEmailVerified())
                    .isActive(user.getIsActive())
                    .createdAt(user.getCreatedAt())
                    .updatedAt(user.getUpdatedAt())
                    .build();

            log.info("User logged in successfully: {}", user.getEmail());

            return AuthResponseDto.builder()
                    .token(token)
                    .tokenType("Bearer")
                    .expiresIn(expirationTime)
                    .user(userProfile)
                    .build();

        } catch (AuthenticationException e) {
            log.error("Authentication failed for user: {}", loginDto.getEmail());
            throw new UserException("Invalid email or password");
        }
    }
}