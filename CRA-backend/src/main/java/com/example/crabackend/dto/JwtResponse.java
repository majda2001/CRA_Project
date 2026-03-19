package com.example.crabackend.dto;

public record JwtResponse(
        String accessToken,
        String refreshToken,
        Long id,
        String email,
        String role
) {}
