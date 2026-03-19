package com.example.crabackend.dto;

import com.example.crabackend.model.CraStatus;
import java.time.LocalDate;
import java.util.List;

public record CraDto(
    Long id,
    Long collaborateurId,
    String collaborateurName,
    String collaborateurEmail,
    Integer mois,
    Integer annee,
    CraStatus status,
    LocalDate dateSoumission,
    LocalDate dateValidation,
    String motifRejet,
    List<ActivityDto> activities
) {}
