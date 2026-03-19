package com.example.crabackend.dto;

import com.example.crabackend.model.ActivityType;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ActivityUpdateRequest(
    @NotNull LocalDate date,
    @NotNull Double duree,
    @NotNull ActivityType type,
    Long missionId // Optional if type != MISSION
) {}
