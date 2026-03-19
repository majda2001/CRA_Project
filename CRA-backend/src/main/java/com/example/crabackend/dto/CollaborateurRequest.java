package com.example.crabackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CollaborateurRequest(
    @NotBlank(message = "Le prénom est obligatoire")
    String firstName,

    @NotBlank(message = "Le nom est obligatoire")
    String lastName,

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Email invalide")
    String email,

    String password, 

    @NotNull(message = "Le statut est obligatoire")
    String statut,   // MISSION, INTERCONTRACT

    @NotNull(message = "Le contrat est obligatoire")
    String contrat,  // CDI, INDEP, PORTAGE

    @NotNull(message = "La séniorité est obligatoire")
    String seniorite, // JUNIOR, CONFIRME, EXPERT

    @NotNull(message = "Le salaire est obligatoire")
    BigDecimal salaire
) {}
