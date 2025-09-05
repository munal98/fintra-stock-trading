package com.fintra.stocktrading.model.entity;

import com.fintra.stocktrading.model.enums.TransferType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "equity_transfers")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EquityTransfer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transfer_id")
    private Integer transferId;

    @ManyToOne
    @JoinColumn(name = "from_account_id")
    private Account fromAccount;

    @ManyToOne
    @JoinColumn(name = "to_account_id")
    private Account toAccount;

    @ManyToOne
    @JoinColumn(name = "equity_id", nullable = false)
    private Equity equity;

    @ManyToOne
    @JoinColumn(name = "oth_inst_id", nullable = true)
    private OtherInstitution otherInstitution;

    @Column(name = "transfer_quantity", nullable = false)
    @Min(value = 1, message = "Transfer quantity must be positive")
    private Integer transferQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "transfer_type", nullable = false)
    private TransferType transferType;

    @Column(name = "total_cost", precision = 18, scale = 4)
    private BigDecimal totalCost;

    @Column(name = "tckn_vergi_no", length = 11)
    private Long tcknOrVergiNo;

    @Column(name = "transaction_time")
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
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
