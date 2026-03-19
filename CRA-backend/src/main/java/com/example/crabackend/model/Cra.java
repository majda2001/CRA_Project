package com.example.crabackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Cra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer mois;
    private Integer annee;

    @ManyToOne(fetch = FetchType.LAZY)
    private User collaborateur;

    @Enumerated(EnumType.STRING)
    private CraStatus status;

    private LocalDate dateSoumission;
    private LocalDate dateValidation;
    private String motifRejet;

    @OneToMany(mappedBy = "cra", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Activity> activities = new ArrayList<>();
}
