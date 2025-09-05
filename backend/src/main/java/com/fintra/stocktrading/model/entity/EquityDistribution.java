package com.fintra.stocktrading.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "equity_distributions")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EquityDistribution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dist_id")
    private Integer distId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @NotNull(message = "Equity order is required")
    private EquityOrder equityOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    @NotNull(message = "Account is required")
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equity_id", nullable = false)
    @NotNull(message = "Equity is required")
    private Equity equity;

    @Column(name = "side", nullable = false)
    @NotNull(message = "Side is required")
    private String side;

    @Column(name = "distribution_quantity", nullable = false)
    @NotNull(message = "Distribution quantity is required")
    @Positive(message = "Distribution quantity must be positive")
    private Integer distributionQuantity;

    @Column(name = "price", precision = 18, scale = 4, nullable = false)
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @Column(name = "transaction_time", nullable = false)
    @NotNull(message = "Transaction time is required")
    private LocalDateTime transactionTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.transactionTime == null) {
            this.transactionTime = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
