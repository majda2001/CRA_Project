package com.example.crabackend.controller;

import com.example.crabackend.dto.CraDto;
import com.example.crabackend.dto.CraValidationRequest;
import com.example.crabackend.model.CraStatus;
import com.example.crabackend.service.CraAlertScheduler;
import com.example.crabackend.service.CraService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/cras")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCraController {

    private final CraService craService;
    private final CraAlertScheduler craAlertScheduler;

    @Value("${app.timezone:Europe/Paris}")
    private String appTimezone;

    @GetMapping
    public ResponseEntity<List<CraDto>> getAllCras(
            @RequestParam(required = false) Integer mois,
            @RequestParam(required = false) Integer annee,
            @RequestParam(required = false) CraStatus status
    ) {
        return ResponseEntity.ok(craService.getAllCras(mois, annee, status));
    }

    @PutMapping("/{id}/validate")
    public ResponseEntity<CraDto> validateCra(@PathVariable Long id) {
        return ResponseEntity.ok(craService.validerCra(id));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<CraDto> rejectCra(@PathVariable Long id, @RequestBody CraValidationRequest request) {
        return ResponseEntity.ok(craService.rejeterCra(id, request.motif()));
    }

    @PutMapping("/{id}/invalidate")
    public ResponseEntity<CraDto> invalidateCra(@PathVariable Long id, @RequestBody CraValidationRequest request) {
        return ResponseEntity.ok(craService.invaliderCra(id, request.motif()));
    }

    @GetMapping("/export")
    public ResponseEntity<String> exportCsv(
            @RequestParam(required = false) Integer mois,
            @RequestParam(required = false) Integer annee
    ) {
        String csvContent = craService.exportCrasToCsv(mois, annee);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=cras_export.csv")
                .header("Content-Type", "text/csv; charset=UTF-8")
                .body(csvContent);
    }

    @PostMapping("/alerts/check")
    public ResponseEntity<Map<String, Object>> triggerAlertsCheck(
            @RequestParam(required = false) Integer mois,
            @RequestParam(required = false) Integer annee
    ) {
        LocalDate now = LocalDate.now(ZoneId.of(appTimezone));
        int targetMonth = mois != null ? mois : now.getMonthValue();
        int targetYear = annee != null ? annee : now.getYear();

        List<String> notSubmitted = craAlertScheduler.verificationSoumissionCra(targetMonth, targetYear);

        return ResponseEntity.ok(Map.of(
                "month", targetMonth,
                "year", targetYear,
                "missingCount", notSubmitted.size(),
                "missingCollaborateurs", notSubmitted
        ));
    }
}
