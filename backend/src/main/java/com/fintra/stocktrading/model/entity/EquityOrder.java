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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "equity_orders")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EquityOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Integer orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equity_id", nullable = false)
    private Equity equity;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_side", nullable = false)
    @NotNull(message = "Order side is required")
    private OrderSide orderSide;

    @Column(name = "order_quantity", nullable = false)
    @NotNull
    @Positive(message = "Order quantity must be positive")
    private Integer orderQuantity;

    @Column(name = "price", precision = 18, scale = 4, nullable = false)
    @NotNull
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "entry_date")
    private LocalDateTime entryDate;

    @Column(name = "order_date",nullable = false)
    private LocalDate orderDate;

    @Column(name = "remaining_quantity", nullable = false)
    private Integer remainingQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "final_status", nullable = false)
    @Builder.Default
    private OrderStatus finalStatus = OrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "combined_status", nullable = false)
    @Builder.Default
    private OrderStatus combinedStatus = OrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false)
    private OrderType orderType;

    @OneToMany(mappedBy = "equityOrder", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private List<Trade> trades = new ArrayList<>();

    @OneToMany(mappedBy = "equityOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EquityOrderLog> orderLogs = new ArrayList<>();

    @OneToMany(mappedBy = "equityOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EquityOrderHistory> orderHistories = new ArrayList<>();

    @OneToMany(mappedBy = "equityOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EquityDistribution> distributions = new ArrayList<>();

    @OneToMany(mappedBy = "buyOrder", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private List<EquityOrderMatch> buyOrderMatches = new ArrayList<>();

    @OneToMany(mappedBy = "sellOrder", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private List<EquityOrderMatch> sellOrderMatches = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.entryDate == null) {
            this.entryDate = now;
        }
        if (this.orderDate == null) {
            this.orderDate = now.toLocalDate();
        }
        if (this.remainingQuantity == null) {
            this.remainingQuantity = this.orderQuantity;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
