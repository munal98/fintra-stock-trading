package com.fintra.stocktrading.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "equity_price_histories",
        indexes = {
                @Index(name = "idx_equity_data_date", columnList = "equity_id, data_date"),
                @Index(name = "idx_data_date", columnList = "data_date")
        },
        uniqueConstraints = @UniqueConstraint(columnNames = {"equity_id", "data_date"}))
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EquityPriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "price_history_id")
    private Integer priceHistoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equity_id", nullable = false)
    @NotNull(message = "Equity is required")
    private Equity equity;

    @Column(name = "data_date", nullable = false)
    @NotNull(message = "Data date is required")
    private LocalDate dataDate;

    @Column(name = "open_price", precision = 18, scale = 4)
    @Positive(message = "Open price must be positive")
    private BigDecimal openPrice;

    @Column(name = "close_price", precision = 18, scale = 4)
    @Positive(message = "Close price must be positive")
    private BigDecimal closePrice;

    @Column(name = "high_price", precision = 18, scale = 4)
    @Positive(message = "High price must be positive")
    private BigDecimal highPrice;

    @Column(name = "low_price", precision = 18, scale = 4)
    @Positive(message = "Low price must be positive")
    private BigDecimal lowPrice;

    @Column(name = "record_time")
    private LocalDateTime recordTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.recordTime == null) {
            this.recordTime = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
