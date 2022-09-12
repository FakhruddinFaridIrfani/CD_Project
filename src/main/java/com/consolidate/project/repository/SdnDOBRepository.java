package com.consolidate.project.repository;

import com.consolidate.project.model.SdnDOB;
import com.consolidate.project.model.SdnProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public interface SdnDOBRepository extends JpaRepository<SdnDOB, Integer> {

    @Modifying
    @Query(value = "DELETE FROM cd.sdn_dob WHERE sdn_entry_id=:sdn_entry_id ", nativeQuery = true)
    void deleteDOBBySdnEntryId(@Param("sdn_entry_id") int sdn_entry_id);

    @Query(value = "SELECT * FROM cd.sdn_dob WHERE sdn_entry_id=:sdn_entry_id ", nativeQuery = true)
    List<SdnDOB> searchDOBBySdnEntryId(@Param("sdn_entry_id") int sdn_entry_id);
}
