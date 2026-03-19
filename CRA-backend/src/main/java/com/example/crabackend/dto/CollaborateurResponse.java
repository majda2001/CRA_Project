package com.example.crabackend.dto;

import com.example.crabackend.model.Role;
import java.math.BigDecimal;

public record CollaborateurResponse(
    Long id,
    String firstName,
    String lastName,
    String email,
    Role role,
    String statut,
    String contrat,
    String seniorite,
    BigDecimal salaire,
    boolean active
) {}
