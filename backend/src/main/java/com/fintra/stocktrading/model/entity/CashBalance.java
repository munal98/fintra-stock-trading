package com.fintra.stocktrading.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cash_balances",
        uniqueConstraints = @UniqueConstraint(columnNames = {"account_id"}))
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CashBalance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "balance_id")
    private Integer balanceId;

    @OneToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "free_balance", precision = 18, scale = 4, nullable = false)
    @Builder.Default
    private BigDecimal freeBalance = BigDecimal.ZERO;

    @Column(name = "blocked_balance", precision = 18, scale = 4, nullable = false)
    @Builder.Default
    private BigDecimal blockedBalance = BigDecimal.ZERO;

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
