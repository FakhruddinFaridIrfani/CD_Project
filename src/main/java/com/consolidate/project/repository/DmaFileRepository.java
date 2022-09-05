package com.consolidate.project.repository;

import com.consolidate.project.model.DMAFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public interface DmaFileRepository extends JpaRepository<DMAFile, Integer> {

    @Query(value = "SELECT * FROM cd.dma_file where file_name_save = :file_name_save", nativeQuery = true)
    DMAFile getDMAFileBySavedFileName(@Param("file_name_save") String file_name_save);

    @Query(value = "SELECT * FROM cd.dma_file where status = 'uploading' OR status = 'matching'", nativeQuery = true)
    List<DMAFile> getMatchingOrUploadingFile(@Param("file_type") String file_type);

    @Modifying
    @Query(value = "UPDATE cd.dma_file SET status =:status,updated_date = current_timestamp,remarks =(remarks || :remarks) where dmafile_id =:dmafile_id", nativeQuery = true)
    void updateUploadedFileStatus(@Param("dmafile_id") int dmafile_id, @Param("status") String status, @Param("remarks") String remarks);

    @Query(value = "SELECT * FROM cd.dma_file where status <> 'deleted'", nativeQuery = true)
    DMAFile getFileToBDeleted();


}
