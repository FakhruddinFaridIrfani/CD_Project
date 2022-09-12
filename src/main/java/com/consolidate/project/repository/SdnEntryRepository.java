package com.consolidate.project.repository;

import com.consolidate.project.model.SdnEntry;
import com.consolidate.project.model.SdnFile;
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


    @Query(value = "select distinct(se.*) " +
            "FROM cd.sdn_entry se " +
            "INNER JOIN cd.sdn_file sf ON sf.sdnfile_id = se.sdnfile_id " +
            "INNER JOIN cd.sdn_aka aka ON aka.sdn_entry_id = se.sdn_entry_id " +
            "INNER JOIN cd.sdn_id si ON si.sdn_entry_id = se.sdn_entry_id " +
            "INNER JOIN cd.sdn_dob sd ON sd.sdn_entry_id = se.sdn_entry_id " +
            "WHERE sf.file_type = :file_type  " +
            "AND (lower(se.first_name) = lower(:first_name) OR lower(se.last_name) = lower(:first_name) OR lower(aka.first_name) = lower(:first_name) OR lower(aka.last_name) = lower(:first_name) " +
            "    OR lower(se.first_name) = lower(:last_name) OR lower(se.last_name) = lower(:last_name) OR lower(aka.first_name) = lower(:last_name) OR lower(aka.last_name) =lower(:last_name) OR si.id_number = :id OR sd.dob = :dob) ORDER BY se.first_name ASC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<SdnEntry> searchDataNameIdDob(@Param("file_type") String file_type, @Param("first_name") String first_name,
                                       @Param("last_name") String last_name, @Param("id") String id, @Param("dob") String dob, @Param("limit") int limit, @Param("offset") int offset);


    @Query(value = "select distinct(se.*) " +
            "FROM cd.sdn_entry se " +
            "INNER JOIN cd.sdn_file sf ON sf.sdnfile_id = se.sdnfile_id " +
            "INNER JOIN cd.sdn_aka aka ON aka.sdn_entry_id = se.sdn_entry_id " +
            "INNER JOIN cd.sdn_id si ON si.sdn_entry_id = se.sdn_entry_id " +
            "WHERE sf.file_type = :file_type  " +
            "AND (lower(se.first_name) = lower(:first_name) OR lower(se.last_name) = lower(:first_name) OR lower(aka.first_name) = lower(:first_name) OR lower(aka.last_name) = lower(:first_name) " +
            "    OR lower(se.first_name) = lower(:last_name) OR lower(se.last_name) = lower(:last_name) OR lower(aka.first_name) = lower(:last_name) OR lower(aka.last_name) =lower(:last_name) OR si.id_number = :id) ORDER BY se.first_name ASC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<SdnEntry> searchDataNameId(@Param("file_type") String file_type, @Param("first_name") String first_name,
                                    @Param("last_name") String last_name, @Param("id") String id, @Param("limit") int limit, @Param("offset") int offset);

}
