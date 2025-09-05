package com.fintra.stocktrading.model.entity;

import com.fintra.stocktrading.model.enums.OrderStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "equity_order_logs")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EquityOrderLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Integer logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @NotNull(message = "Equity order is required")
    private EquityOrder equityOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    @NotNull(message = "Order status is required")
    private OrderStatus orderStatus;

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
