package com.example.crabackend.dto;

import java.time.LocalDate;

public record AssignmentResponse(
    Long id,
    Long collaborateurId,
    String collaborateurName,
    Long missionId,
    String missionTitle,
    LocalDate dateDebut,
    LocalDate dateFin
) {}
