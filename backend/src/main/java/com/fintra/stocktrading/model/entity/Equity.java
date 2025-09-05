package com.fintra.stocktrading.model.entity;

import com.fintra.stocktrading.model.enums.EquityType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "equities")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Equity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "equity_id")
    private Integer equityId;

    @Column(name = "equity_name", length = 200, nullable = false)
    @NotNull(message = "Equity name is required")
    private String equityName;

    @Column(name = "equity_code", length = 20, unique = true, nullable = false)
    @NotNull(message = "Equity code is required")
    private String equityCode;

    @Column(name = "ticker", length = 30, nullable = false)
    @NotNull(message = "Ticker is required")
    private String ticker;

    @Column(name = "market", length = 1000)
    private String market;

    @Column(name = "country", length = 10)
    private String country;

    @Column(name = "index_info", length = 2000)
    private String indexInfo;

    @Column(name = "participation", nullable = false)
    @Builder.Default
    private Boolean participation = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "equity_type", nullable = false)
    @Builder.Default
    private EquityType equityType = EquityType.STOCK;

    @OneToMany(mappedBy = "equity", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private List<EquityStock> equityStocks = new ArrayList<>();

    @OneToMany(mappedBy = "equity", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private List<EquityTransfer> equityTransfers = new ArrayList<>();

    @OneToMany(mappedBy = "equity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EquityPriceHistory> priceHistories = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
