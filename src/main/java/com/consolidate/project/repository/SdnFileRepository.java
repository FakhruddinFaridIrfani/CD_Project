package com.consolidate.project.repository;

import com.consolidate.project.model.SdnFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
@Transactional
public interface SdnFileRepository extends JpaRepository<SdnFile, Integer> {

    @Query(value = "SELECT * FROM cd.sdn_file where file_name_save = :file_name_save", nativeQuery = true)
    public SdnFile getSdnFileBySavedFileName(@Param("file_name_save") String file_name_save);

}
