package com.fintra.stocktrading.model.entity;

import com.fintra.stocktrading.model.enums.OrderSide;
import com.fintra.stocktrading.model.enums.OrderStatus;
import com.fintra.stocktrading.model.enums.OrderType;
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
@Table(name = "equity_order_histories")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EquityOrderHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @NotNull(message = "Equity order is required")
    private EquityOrder equityOrder;

    @Column(name = "old_order_quantity")
    @Positive(message = "Old order quantity must be positive")
    private Integer oldOrderQuantity;

    @Column(name = "order_status", nullable = false)
    @NotNull(message = "Order status is required")
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_side", nullable = false)
    private OrderSide orderSide;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false)
    private OrderType orderType;

    @Column(name = "old_price", precision = 18, scale = 4)
    @Positive(message = "Old price must be positive")
    private BigDecimal oldPrice;

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
