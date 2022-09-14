package com.consolidate.project.repository;

import com.consolidate.project.model.DMADetail;
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
public interface DmaDetailRepository extends JpaRepository<DMADetail, Integer> {


    @Query(value = "SELECT COUNT(*) FROM cd.dma_detail",nativeQuery = true)
    int getDmaEntryCount();

    @Query(value = "select count(dma.*) from " +
            "cd.dma_detail dma " +
            "INNER JOIN cd.ktp_detail ktp ON dma.merchant_no = ktp.merchant_no ",nativeQuery = true)
    int getScreenData();
}
