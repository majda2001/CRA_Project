package com.example.crabackend.dto;

import java.time.LocalDate;

public record MissionResponse(
    Long id,
    String titre,
    String description,
    LocalDate dateDebut,
    LocalDate dateFin,
    String motif,
    Double tjm
) {}
