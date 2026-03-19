package com.example.crabackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record InvitationCompletionRequest(
        @NotBlank(message = "Le mot de passe est obligatoire")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$",
                message = "Le mot de passe doit contenir 8 caracteres minimum avec majuscule, minuscule, chiffre et caractere special"
        )
        String password
) {}
