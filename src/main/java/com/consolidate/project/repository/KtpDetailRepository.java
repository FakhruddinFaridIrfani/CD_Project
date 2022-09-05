package com.consolidate.project.repository;

import com.consolidate.project.model.KTPDetail;
import com.consolidate.project.model.SdnAka;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
@Transactional
public interface KtpDetailRepository extends JpaRepository<KTPDetail, Integer> {

    @Modifying
    @Query(value = "DELETE FROM cd.ktp_detail WHERE ktpfile_id=:ktpfile_id ", nativeQuery = true)
    void deleteKtpDetailByFileId(@Param("ktpfile_id") int ktpfile_id);

}
