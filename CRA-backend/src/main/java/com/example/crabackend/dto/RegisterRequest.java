package com.example.crabackend.dto;

import com.example.crabackend.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RegisterRequest(
    @NotBlank String firstName,
    @NotBlank String lastName,
    @NotBlank @Email String email,
    @NotBlank String password,
    @NotNull Role role,
    String statut,
    String contrat,
    String seniorite,
    BigDecimal salaire
) {}
