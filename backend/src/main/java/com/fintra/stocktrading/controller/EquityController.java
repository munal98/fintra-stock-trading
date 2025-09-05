package com.fintra.stocktrading.controller;

import com.fintra.stocktrading.controller.doc.EquityControllerDoc;
import com.fintra.stocktrading.model.dto.response.EquityInfoResponse;
import com.fintra.stocktrading.model.dto.response.EquityPriceResponse;
import com.fintra.stocktrading.service.EquityService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/equities")
@RequiredArgsConstructor
@Slf4j
@Validated
public class EquityController implements EquityControllerDoc {

    private final EquityService equityService;

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TRADER','ROLE_ANALYST')")
    @GetMapping
    public ResponseEntity<Page<EquityPriceResponse>> getEquities(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size,
            @RequestParam(required = false) String filter) {

        log.info("Fetching equities from database - page: {}, size: {}, filter: '{}'", page, size, filter);

        Page<EquityPriceResponse> equities = equityService.getEquitiesForFrontend(page, size, filter);

        log.info("Retrieved {} equities from database (page {} of {})",
                equities.getNumberOfElements(), page + 1, equities.getTotalPages());

        return ResponseEntity.ok(equities);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TRADER','ROLE_ANALYST')")
    @GetMapping("/{assetCode}/prices")
    public ResponseEntity<List<EquityPriceResponse>> getPriceHistory(
            @PathVariable @NotBlank @Pattern(regexp = "^[A-Z0-9.]+$", message = "Asset code must contain only uppercase letters, numbers, and dots")
            String assetCode,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {

        log.info("Fetching price history from database for asset code: {} from {} to {}", assetCode, startDate, endDate);

        List<EquityPriceResponse> priceHistory = equityService.getPriceHistoryByAssetCode(assetCode, startDate, endDate);

        log.info("Retrieved {} price history records from database for {}", priceHistory.size(), assetCode);

        return ResponseEntity.ok(priceHistory);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TRADER','ROLE_ANALYST')")
    @GetMapping("/{assetCode}/info")
    public ResponseEntity<EquityInfoResponse> getEquityInfo(
            @PathVariable @NotBlank @Pattern(regexp = "^[A-Z0-9.]+$", message = "Asset code must contain only uppercase letters, numbers, and dots")
            String assetCode) {

        log.info("Fetching equity info from database for asset code: {}", assetCode);

        EquityInfoResponse equityInfo = equityService.getEquityInfoByAssetCode(assetCode);

        log.info("Retrieved equity info from database for: {}", assetCode);

        return ResponseEntity.ok(equityInfo);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_TRADER','ROLE_ANALYST')")
    @GetMapping("/{assetCode}/latest")
    public ResponseEntity<EquityPriceResponse> getLatestPrice(
            @PathVariable @NotBlank @Pattern(regexp = "^[A-Z0-9.]+$", message = "Asset code must contain only uppercase letters, numbers, and dots")
            String assetCode) {

        log.info("Fetching latest price from database for asset code: {}", assetCode);

        EquityPriceResponse latestPrice = equityService.getLatestPriceByAssetCode(assetCode);

        log.info("Retrieved latest price from database for: {}", assetCode);

        return ResponseEntity.ok(latestPrice);
    }
}
