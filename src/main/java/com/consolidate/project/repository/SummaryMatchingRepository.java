package com.consolidate.project.repository;

import com.consolidate.project.model.SummaryMatching;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public interface SummaryMatchingRepository extends JpaRepository<SummaryMatching, Integer> {

    @Modifying
    @Query(value = "UPDATE ofac.summary_matching SET status = 'matched',end_matching = current_timestamp,count_positive_sdn = :count_positive_sdn,count_positive_consolidate = :count_positive_consolidate,count_potential_sdn = :count_potential_sdn,count_potential_consolidate=:count_potential_consolidate " +
            "WHERE sdn_file_id_sdn =:sdn_file_id_sdn AND sdn_file_id_consolidate =:sdn_file_id_consolidate AND dma_file_id =:dma_file_id AND ktp_file_id=:ktp_file_id", nativeQuery = true)
    void updateSummaryMatching(@Param("count_positive_sdn") int count_positive_sdn, @Param("count_positive_consolidate") int count_positive_consolidate,
                               @Param("count_potential_sdn") int count_potential_sdn, @Param("count_potential_consolidate") int count_potential_consolidate,
                               @Param("sdn_file_id_sdn") int sdn_file_id_sdn, @Param("sdn_file_id_consolidate") int sdn_file_id_consolidate,
                               @Param("dma_file_id") int dma_file_id, @Param("ktp_file_id") int ktp_file_id);

    @Query(value = "SELECT * FROM ofac.summary_matching where (CAST(start_matching AS DATE) between  CAST(:start_matching AS DATE) and CAST(:end_matching AS DATE)) AND (CAST(end_matching AS DATE) between  CAST(:start_matching AS DATE) and CAST(:end_matching AS DATE) )", nativeQuery = true)
    List<SummaryMatching> getSummaryMatchingByStartdEndDate(@Param("start_matching") String start_matching, @Param("end_matching") String end_matching);
}
