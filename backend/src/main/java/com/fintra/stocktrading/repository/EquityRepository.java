package com.fintra.stocktrading.repository;

import com.fintra.stocktrading.model.entity.Equity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EquityRepository extends JpaRepository<Equity, Integer> {
    Optional<Equity> findByEquityCode(String equityCode);
    Optional<Equity> findByEquityName(String equityName);
    Optional<Equity> findByTicker(String ticker);
    
    @Query("SELECT e FROM Equity e WHERE e.ticker = :code OR e.equityCode = :code")
    Optional<Equity> findByTickerOrEquityCode(@Param("code") String code);
    
    @Query("SELECT e FROM Equity e WHERE e.participation = true")
    java.util.List<Equity> findParticipationEquities();
    
    @Query("SELECT e FROM Equity e WHERE e.participation = false")
    java.util.List<Equity> findNonParticipationEquities();

    @Query("""
        SELECT e FROM Equity e 
        WHERE (:filter IS NULL OR :filter = '' OR 
               LOWER(e.equityCode) LIKE LOWER(CONCAT('%', :filter, '%')) OR 
               LOWER(e.equityName) LIKE LOWER(CONCAT('%', :filter, '%')))
        ORDER BY e.equityCode
        """)
    Page<Equity> findEquitiesWithFilter(@Param("filter") String filter, Pageable pageable);
}
