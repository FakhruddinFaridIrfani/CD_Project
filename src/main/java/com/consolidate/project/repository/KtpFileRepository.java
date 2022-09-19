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

    @Query(value = "SELECT * FROM ofac.ktp_file where file_name_save = :file_name_save", nativeQuery = true)
    KTPFile getKTPFileBySavedFileName(@Param("file_name_save") String file_name_save);

    @Query(value = "SELECT * FROM ofac.ktp_file where status = 'uploading' OR status = 'matching' ", nativeQuery = true)
    List<KTPFile> getMatchingOrUploadingFile();

    @Query(value = "SELECT * FROM ofac.ktp_file where status =:status ", nativeQuery = true)
    List<KTPFile> getFileByStatus(@Param("status") String status);

    @Modifying
    @Query(value = "UPDATE ofac.ktp_file SET status =:status,updated_date = current_timestamp,remarks =(remarks || :remarks) where ktp_file_id =:ktp_file_id", nativeQuery = true)
    void updateFileStatus(@Param("ktp_file_id") int ktp_file_id, @Param("status") String status, @Param("remarks") String remarks);

    @Query(value = "SELECT * FROM ofac.ktp_file where status <> 'deleted'", nativeQuery = true)
    KTPFile getFileToBDeleted();


}
