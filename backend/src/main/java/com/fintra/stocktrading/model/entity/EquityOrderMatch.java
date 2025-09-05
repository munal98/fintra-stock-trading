package com.fintra.stocktrading.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "equity_order_matches")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EquityOrderMatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "match_record_id")
    private Integer matchRecordId;

    @Column(name = "match_id", nullable = false)
    @NotNull(message = "Match ID is required")
    private Integer matchId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buy_order_id", nullable = false)
    @NotNull(message = "Buy order is required")
    private EquityOrder buyOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sell_order_id", nullable = false)
    @NotNull(message = "Sell order is required")
    private EquityOrder sellOrder;

    @Column(name = "match_time", nullable = false)
    @NotNull(message = "Match time is required")
    private LocalDateTime matchTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.matchTime == null) {
            this.matchTime = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
