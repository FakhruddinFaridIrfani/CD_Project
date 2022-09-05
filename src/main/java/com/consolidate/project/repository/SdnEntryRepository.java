package com.consolidate.project.repository;

import com.consolidate.project.model.SdnEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public interface SdnEntryRepository extends JpaRepository<SdnEntry, Integer> {

    @Query(value = "SELECT * FROM cd.sdn_entry where uid = :uid AND sdnfile_id =:sdnfile_id", nativeQuery = true)
    public SdnEntry getSdnEntryBySavedUIDAndFIleID(@Param("uid") int uid, @Param("sdnfile_id") int sdnfile_id);

    @Query(value = "SELECT * FROM cd.sdn_entry WHERE sdnfile_id=:sdnfile_id", nativeQuery = true)
    List<SdnEntry> getSdnEntryByFileId(@Param("sdnfile_id") int sdnfile_id);

    @Modifying
    @Query(value = "DELETE FROM cd.sdn_entry WHERE sdnfile_id=:sdnfile_id ", nativeQuery = true)
    void deleteSdnEntryByFileId(@Param("sdnfile_id") int sdnfile_id);

}
