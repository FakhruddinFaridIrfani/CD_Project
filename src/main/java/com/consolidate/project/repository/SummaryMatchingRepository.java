package com.consolidate.project.repository;

import com.consolidate.project.model.SummaryMatching;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
@Transactional
public interface SummaryMatchingRepository extends JpaRepository<SummaryMatching, Integer> {

    @Modifying
    @Query(value = "UPDATE cd.summary_matching SET status = 'matched',end_matching = current_timestamp,count_positive = :count_positive,count_potential = :count_potential " +
            "WHERE sdnfile_id_sdn =:sdnfile_id_sdn AND sdnfile_id_consolidate =:sdnfile_id_consolidate AND dma_file_id =:dma_file_id AND ktp_file_id=:ktp_file_id", nativeQuery = true)
    void updateSummaryMatching(@Param("count_positive") int count_positive, @Param("count_potential") int count_potential,
                               @Param("sdnfile_id_sdn") int sdnfile_id_sdn, @Param("sdnfile_id_consolidate") int sdnfile_id_consolidate,
                               @Param("dma_file_id") int dma_file_id, @Param("ktp_file_id") int ktp_file_id);

}
