package com.fintra.stocktrading.service.impl;

import com.fintra.stocktrading.model.entity.OtherInstitution;
import com.fintra.stocktrading.repository.OtherInstitutionRepository;
import com.fintra.stocktrading.service.OtherInstitutionService;
import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

@Service
public class OtherInstitutionServiceImpl implements OtherInstitutionService {

    private final OtherInstitutionRepository otherInstitutionRepository;

    public OtherInstitutionServiceImpl(
            OtherInstitutionRepository otherInstitutionRepository) {
        this.otherInstitutionRepository = otherInstitutionRepository;
    }

    @Override
    @Cacheable(value = "institutions", key = "'allInsts'")
    public List<OtherInstitution> getAllOtherInstitutions() {
        return otherInstitutionRepository.findAll();
    }

    @Override
    @Cacheable(value = "institutions", key = "#id")
    public Optional<OtherInstitution> getOtherInstitutionById(Integer id) {
        Optional<OtherInstitution> institution = otherInstitutionRepository.findById(id);
        if (institution.isEmpty()) {
            throw new IllegalArgumentException("Institution not found!");
        }
        return institution;
    }

    @Override
    @Caching(
            put = { @CachePut(value = "institutions", key = "#result.id") },
            evict = { @CacheEvict(value = "institutions", key = "'allInsts'") })
    public OtherInstitution saveOtherInstitution(OtherInstitution otherInstitution) {
        return otherInstitutionRepository.save(otherInstitution);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "institutions", key = "#id"),
            @CacheEvict(value = "institutions", key = "'allInsts'") })
    public void deleteOtherInstitution(Integer id) {
        otherInstitutionRepository.deleteById(id);
    }
}
