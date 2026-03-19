package com.example.crabackend.dto;

import com.example.crabackend.model.ActivityType;
import java.time.LocalDate;

public record ActivityDto(
    Long id,
    LocalDate date,
    Double duree,
    ActivityType type,
    Long missionId,
    String missionTitle
) {}
