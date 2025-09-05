package com.fintra.stocktrading.model.entity;

import com.fintra.stocktrading.model.enums.TradeStatus;
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
@Table(name = "trades")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Trade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trade_id")
    private Integer tradeId;

    @Column(name = "match_id", nullable = false)
    @NotNull(message = "Match ID is required")
    private Integer matchId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @NotNull(message = "Equity order is required")
    private EquityOrder equityOrder;

    @Column(name = "trade_quantity", nullable = false)
    @NotNull(message = "Trade quantity is required")
    @Positive(message = "Trade quantity must be positive")
    private Integer tradeQuantity;

    @Column(name = "price", precision = 18, scale = 4, nullable = false)
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @Column(name = "commission", precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal commission = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "trade_status", nullable = false)
    private TradeStatus status;

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
