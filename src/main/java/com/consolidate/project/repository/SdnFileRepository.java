package com.consolidate.project.repository;

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

    @Modifying
    @Query(value = "UPDATE cd.sdn_file SET status =:status,updated_date = current_timestamp,remarks =(remarks || :remarks) where sdnfile_id =:sdnfile_id", nativeQuery = true)
    void updateUploadedFileStatus(@Param("sdnfile_id") int sdnfile_id, @Param("status") String status, @Param("remarks") String remarks);

    @Query(value = "SELECT * FROM cd.sdn_file where file_type=:file_type AND  status <> 'deleted'", nativeQuery = true)
    SdnFile getFileToBDeleted(@Param("file_type") String file_type);


}
