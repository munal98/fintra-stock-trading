package com.fintra.stocktrading.model.entity;

import com.fintra.stocktrading.model.enums.TradingPermission;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customers")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Integer customerId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @Column(name = "first_name", nullable = false)
    @NotNull(message = "First name is required")
    private String firstName;

    @Column(name = "last_name", nullable = false)
    @NotNull(message = "Last name is required")
    private String lastName;

    @Column(name = "email", nullable = false, unique = true)
    @NotNull(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @Column(name = "identity_number", nullable = false, unique = true)
    @NotNull(message = "Identity number is required")
    @Pattern(regexp = "^\\d{10,11}$", message = "Identity number must be 10 digits (Tax Number) for corporate or 11 digits (TC Identity) for individual customers")
    private String identityNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "trading_permission", nullable = false)
    private TradingPermission tradingPermission;

    @Column(name = "trading_enabled", nullable = false)
    @Builder.Default
    private Boolean tradingEnabled = true;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private List<Account> accounts = new ArrayList<>();

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
