package com.example.crabackend.dto;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    String email,
    String role
) {}
