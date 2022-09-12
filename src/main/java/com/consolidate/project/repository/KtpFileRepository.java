package com.consolidate.project.repository;

import com.consolidate.project.model.KTPFile;
import com.consolidate.project.model.KTPFile;
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
public interface KtpFileRepository extends JpaRepository<KTPFile, Integer> {

    @Query(value = "SELECT * FROM cd.ktp_file where file_name_save = :file_name_save", nativeQuery = true)
    KTPFile getKTPFileBySavedFileName(@Param("file_name_save") String file_name_save);

    @Query(value = "SELECT * FROM cd.ktp_file where status = 'uploading' OR status = 'matching' ", nativeQuery = true)
    List<KTPFile> getMatchingOrUploadingFile();

    @Query(value = "SELECT * FROM cd.ktp_file where status <> 'deleted'", nativeQuery = true)
    List<KTPFile> getNotDeletedFile();

    @Modifying
    @Query(value = "UPDATE cd.ktp_file SET status =:status,updated_date = current_timestamp,remarks =(remarks || :remarks) where ktpfile_id =:ktpfile_id", nativeQuery = true)
    void updateUploadedFileStatus(@Param("ktpfile_id") int ktpfile_id, @Param("status") String status, @Param("remarks") String remarks);

    @Query(value = "SELECT * FROM cd.ktp_file where status <> 'deleted'", nativeQuery = true)
    KTPFile getFileToBDeleted();


}
