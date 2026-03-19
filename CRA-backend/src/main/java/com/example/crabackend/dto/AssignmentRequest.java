package com.example.crabackend.dto;

import jakarta.validation.constraints.NotNull;

public record AssignmentRequest(
    @NotNull Long collaborateurId,
    @NotNull Long missionId
) {}
