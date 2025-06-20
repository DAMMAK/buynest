package dev.dammak.userservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponseDto {
    private String token;
    private String tokenType;
    private Long expiresIn;
    private UserProfileDto user;
}