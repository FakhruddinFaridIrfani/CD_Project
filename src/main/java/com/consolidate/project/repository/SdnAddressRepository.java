package com.consolidate.project.repository;

import com.consolidate.project.model.SdnAddress;
import com.consolidate.project.model.SdnAka;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
@Transactional
public interface SdnAddressRepository extends JpaRepository<SdnAddress, Integer> {

    @Modifying
    @Query(value = "DELETE FROM cd.sdn_address WHERE sdn_entry_id=:sdn_entry_id ", nativeQuery = true)
    void deleteAddressBySdnEntryId(@Param("sdn_entry_id") int sdn_entry_id);
}
