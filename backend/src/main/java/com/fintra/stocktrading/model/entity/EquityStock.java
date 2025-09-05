package com.fintra.stocktrading.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "equity_stocks",
        uniqueConstraints = @UniqueConstraint(columnNames = {"account_id", "equity_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquityStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stock_id")
    private Integer stockId;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne
    @JoinColumn(name = "equity_id", nullable = false)
    private Equity equity;

    @Column(name = "free_quantity", nullable = false)
    @Builder.Default
    @Min(value = 0, message = "Free quantity cannot be negative")
    private Integer freeQuantity = 0;

    @Column(name = "blocked_quantity", nullable = false)
    @Builder.Default
    @Min(value = 0, message = "Blocked quantity cannot be negative")
    private Integer blockedQuantity = 0;

    @Column(name = "avg_cost", precision = 18, scale = 4)
    private BigDecimal avgCost;

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
