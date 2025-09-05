package com.fintra.stocktrading.service;

import com.fintra.stocktrading.model.entity.OtherInstitution;
import java.util.List;
import java.util.Optional;

public interface OtherInstitutionService {
    
    /**
     * Retrieves all other institutions registered in the system.
     *
     * @return list of all other institutions
     */
    List<OtherInstitution> getAllOtherInstitutions();

    /**
     * Retrieves a specific other institution by its ID.
     *
     * @param id the institution ID
     * @return optional other institution record
     */
    Optional<OtherInstitution> getOtherInstitutionById(Integer id);

    /**
     * Saves a new other institution or updates an existing one.
     *
     * @param otherInstitution the institution to save
     * @return saved other institution record
     */
    OtherInstitution saveOtherInstitution(OtherInstitution otherInstitution);

    /**
     * Deletes an other institution by its ID.
     *
     * @param id the institution ID to delete
     */
    void deleteOtherInstitution(Integer id);
}
