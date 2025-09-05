package com.fintra.stocktrading.repository;

import com.fintra.stocktrading.model.entity.EquitySession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;

public interface EquitySessionRepository extends JpaRepository<EquitySession, Long> {

    Optional<EquitySession> findBySessionDate(LocalDate sessionDate);
    boolean existsBySessionDate(LocalDate sessionDate);
}
