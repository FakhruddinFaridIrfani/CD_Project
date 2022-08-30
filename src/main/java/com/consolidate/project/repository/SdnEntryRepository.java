package com.consolidate.project.repository;

import com.consolidate.project.model.SdnEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
@Transactional
public interface SdnEntryRepository extends JpaRepository<SdnEntry, Integer> {

    @Query(value = "SELECT * FROM cd.sdn_entry where uid = :uid", nativeQuery = true)
    public SdnEntry getSdnEntryBySavedUID(@Param("uid") int uid);

}
