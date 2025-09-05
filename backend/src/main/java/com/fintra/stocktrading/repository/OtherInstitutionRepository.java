package com.fintra.stocktrading.repository;
import com.fintra.stocktrading.model.entity.OtherInstitution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OtherInstitutionRepository  extends JpaRepository<OtherInstitution, Integer>{
}