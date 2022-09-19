package com.consolidate.project.repository;

import com.consolidate.project.model.SdnCitizenship;
import com.consolidate.project.model.SdnPOB;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public interface SdnCitizenshipRepository extends JpaRepository<SdnCitizenship, Integer> {

    @Modifying
    @Query(value = "DELETE FROM ofac.sdn_citizenship WHERE sdn_entry_id=:sdn_entry_id ", nativeQuery = true)
    void deleteCitizenshipBySdnEntryId(@Param("sdn_entry_id") int sdn_entry_id);

    @Query(value = "SELECT * FROM ofac.sdn_citizenship WHERE sdn_entry_id=:sdn_entry_id ", nativeQuery = true)
    List<SdnCitizenship> searchCitizenshipBySdnEntryId(@Param("sdn_entry_id") int sdn_entry_id);
}
