package com.consolidate.project.repository;

import com.consolidate.project.model.DMAFile;
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
public interface SdnFileRepository extends JpaRepository<SdnFile, Integer> {

    @Query(value = "SELECT * FROM cd.sdn_file where file_name_save = :file_name_save", nativeQuery = true)
    SdnFile getSdnFileBySavedFileName(@Param("file_name_save") String file_name_save);

    @Query(value = "SELECT * FROM cd.sdn_file where file_type =:file_type AND (status = 'uploading' OR status = 'matching') ", nativeQuery = true)
    List<SdnFile> getMatchingOrUploadingFile(@Param("file_type") String file_type);

    @Query(value = "SELECT * FROM cd.sdn_file where status <> 'deleted'", nativeQuery = true)
    List<SdnFile> getNotDeletedFile();

    @Query(value = "SELECT * FROM cd.sdn_file where status =:status and file_type=:file_type ", nativeQuery = true)
    List<SdnFile> getFileByStatus(@Param("status") String status, @Param("file_type") String file_type);

    @Modifying
    @Query(value = "UPDATE cd.sdn_file SET status =:status,updated_date = current_timestamp,remarks =(remarks || :remarks) where sdnfile_id =:sdnfile_id", nativeQuery = true)
    void updateFileStatus(@Param("sdnfile_id") int sdnfile_id, @Param("status") String status, @Param("remarks") String remarks);

    @Query(value = "SELECT * FROM cd.sdn_file where file_type=:file_type AND  status <> 'deleted'", nativeQuery = true)
    SdnFile getFileToBDeleted(@Param("file_type") String file_type);


    @Query(value = "SELECT * FROM cd.sdn_file where sdnfile_id = :sdnfile_id", nativeQuery = true)
    SdnFile getFileById(@Param("sdnfile_id") int sdnfile_id);

    @Query(value = "select * from cd.sdn_file sf " +
            "INNER JOIN cd.sdn_entry entry ON entry.sdnfile_id = sf.sdnfile_id " +
            "where entry.sdn_entry_id =:sdn_entry_id", nativeQuery = true)
    SdnFile getSdnFileByEntryId(@Param("sdn_entry_id") int sdn_entry_id);


}
