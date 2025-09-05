package com.fintra.stocktrading.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "system_date")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemDate {

    @Id
    private Integer id=1;

    @Column(name = "t_date", nullable = false)
    private LocalDate tDate;
}
