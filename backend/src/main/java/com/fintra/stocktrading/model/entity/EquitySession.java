package com.fintra.stocktrading.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Entity
@Table(name = "equity_session")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquitySession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "session_date", nullable = false, unique = true)
    private LocalDate sessionDate;

    @Column(name = "open_time", nullable = false)
    private LocalTime openTime;

    @Column(name = "close_time", nullable = false)
    private LocalTime closeTime;
}
