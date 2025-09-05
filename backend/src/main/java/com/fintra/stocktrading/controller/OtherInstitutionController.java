package com.fintra.stocktrading.controller;

import com.fintra.stocktrading.controller.doc.OtherInstitutionControllerDoc;
import com.fintra.stocktrading.model.entity.OtherInstitution;
import com.fintra.stocktrading.service.OtherInstitutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/other-institution")
@RequiredArgsConstructor
public class OtherInstitutionController implements OtherInstitutionControllerDoc {

    private final OtherInstitutionService otherInstitutionService;

    @PreAuthorize("hasAnyRole('ADMIN', 'TRADER')")
    @GetMapping
    public ResponseEntity<List<OtherInstitution>> getAllOtherInstitutions() {
        List<OtherInstitution> institutions = otherInstitutionService.getAllOtherInstitutions();
        return new ResponseEntity<>(institutions, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TRADER')")
    @GetMapping("/{id}")
    public ResponseEntity<OtherInstitution> getOtherInstitutionById(@PathVariable Integer id) {
        Optional<OtherInstitution> institution = otherInstitutionService.getOtherInstitutionById(id);

        return institution.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TRADER')")
    @PostMapping
    public ResponseEntity<OtherInstitution> createOtherInstitution(@RequestBody OtherInstitution otherInstitution) {
        OtherInstitution createdInstitution = otherInstitutionService.saveOtherInstitution(otherInstitution);
        return new ResponseEntity<>(createdInstitution, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TRADER')")
    @PutMapping("/{id}")
    public ResponseEntity<OtherInstitution> updateOtherInstitution(@PathVariable Integer id, @RequestBody OtherInstitution updatedInstitution) {
        if (!otherInstitutionService.getOtherInstitutionById(id).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        updatedInstitution.setId(id);
        OtherInstitution savedInstitution = otherInstitutionService.saveOtherInstitution(updatedInstitution);
        return new ResponseEntity<>(savedInstitution, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TRADER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOtherInstitution(@PathVariable Integer id) {
        if (!otherInstitutionService.getOtherInstitutionById(id).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        otherInstitutionService.deleteOtherInstitution(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
