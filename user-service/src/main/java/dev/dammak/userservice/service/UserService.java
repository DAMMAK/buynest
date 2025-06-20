package dev.dammak.userservice.service;


import dev.dammak.userservice.dto.UserProfileDto;
import dev.dammak.userservice.dto.UserRegistrationDto;
import dev.dammak.userservice.entity.User;
import dev.dammak.userservice.exception.UserException;
import dev.dammak.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public UserProfileDto registerUser(UserRegistrationDto registrationDto) {
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new UserException("Email already exists");
        }

        String emailVerificationToken = UUID.randomUUID().toString();

        User user = User.builder()
                .email(registrationDto.getEmail())
                .password(passwordEncoder.encode(registrationDto.getPassword()))
                .firstName(registrationDto.getFirstName())
                .lastName(registrationDto.getLastName())
                .phoneNumber(registrationDto.getPhoneNumber())
                .role(User.UserRole.USER)
                .isEmailVerified(false)
                .emailVerificationToken(emailVerificationToken)
                .emailVerificationTokenExpiry(LocalDateTime.now().plusHours(24))
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);

        // Send verification email
        emailService.sendEmailVerification(savedUser.getEmail(), emailVerificationToken);

        // Publish user registered event
        publishUserEvent("USER_REGISTERED", savedUser);

        log.info("User registered successfully: {}", savedUser.getEmail());

        return mapToUserProfileDto(savedUser);
    }

    @Transactional
    public void verifyEmail(String token) {
        User user = userRepository.findByValidEmailVerificationToken(token, LocalDateTime.now())
                .orElseThrow(() -> new UserException("Invalid or expired verification token"));

        user.setIsEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationTokenExpiry(null);

        userRepository.save(user);

        log.info("Email verified successfully for user: {}", user.getEmail());
    }

    public UserProfileDto getUserProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("User not found"));

        return mapToUserProfileDto(user);
    }

    @Transactional
    public UserProfileDto updateUserProfile(UUID userId, UserProfileDto profileDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("User not found"));

        user.setFirstName(profileDto.getFirstName());
        user.setLastName(profileDto.getLastName());
        user.setPhoneNumber(profileDto.getPhoneNumber());

        User updatedUser = userRepository.save(user);

        // Publish user updated event
        publishUserEvent("USER_PROFILE_UPDATED", updatedUser);

        log.info("User profile updated successfully: {}", updatedUser.getEmail());

        return mapToUserProfileDto(updatedUser);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    private UserProfileDto mapToUserProfileDto(User user) {
        return UserProfileDto.builder()
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
    }

    private void publishUserEvent(String eventType, User user) {
        try {
            UserEventDto event = UserEventDto.builder()
                    .eventType(eventType)
                    .userId(user.getId())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .timestamp(LocalDateTime.now())
                    .build();

            kafkaTemplate.send("user-events", event);
        } catch (Exception e) {
            log.error("Failed to publish user event: {}", e.getMessage(), e);
        }
    }

    @lombok.Data
    @lombok.Builder
    public static class UserEventDto {
        private String eventType;
        private UUID userId;
        private String email;
        private String firstName;
        private String lastName;
        private LocalDateTime timestamp;
    }
}