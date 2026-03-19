package com.example.crabackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record MissionRequest(
    @NotBlank(message = "Le titre est obligatoire")
    String titre,

    @NotBlank(message = "La description est obligatoire")
    String description,

    @NotNull(message = "La date de debut est obligatoire")
    LocalDate dateDebut,

    LocalDate dateFin,

    String motif,

    @NotNull(message = "Le TJM est obligatoire")
    @Positive(message = "Le TJM doit etre positif")
    Double tjm
) {}
