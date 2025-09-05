package com.fintra.stocktrading.service.impl;

import com.fintra.stocktrading.model.entity.SystemDate;
import com.fintra.stocktrading.repository.SystemDateRepository;
import com.fintra.stocktrading.service.SystemDateService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemDateServiceImpl implements SystemDateService {

    private final SystemDateRepository repo;

    @Override
    @Transactional
    public LocalDate getTDate() {
        return repo.findById(1)
                .orElseGet(() -> repo.save(SystemDate.builder().id(1).tDate(LocalDate.now()).build()))
                .getTDate();
    }

    @Override
    public LocalDate updateTDate(LocalDate nextDate) {
        SystemDate sd = repo.findById(1)
                .orElseThrow(() -> new IllegalStateException("SystemDate not found in DB"));
        sd.setTDate(nextDate);
        return repo.save(sd).getTDate();
    }
}
