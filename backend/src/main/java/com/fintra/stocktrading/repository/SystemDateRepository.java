package com.fintra.stocktrading.repository;

import com.fintra.stocktrading.model.entity.SystemDate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemDateRepository extends JpaRepository<SystemDate, Integer> {
}
