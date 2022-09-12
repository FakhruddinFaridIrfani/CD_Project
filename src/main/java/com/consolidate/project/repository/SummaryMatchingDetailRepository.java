package com.consolidate.project.repository;

import com.consolidate.project.model.SummaryMatching;
import com.consolidate.project.model.SummaryMatchingDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public interface SummaryMatchingDetailRepository extends JpaRepository<SummaryMatchingDetail, Integer> {

    @Modifying
    @Query(value = "INSERT INTO cd.summary_matching_detail (status,summary_matching_id,ktp_detail_id,sdn_entry_id,matching_status,created_by,updated_by,created_date,updated_date) " +
            "    SELECT distinct 'active',suma.summary_matching_id,ktp.ktp_detail_id,entrysdn.sdn_entry_id,'potential','SYSTEM-AUTO','SYSTEM-AUTO',current_timestamp,current_timestamp " +
            "    FROM cd.ktp_detail ktp " +
            "    INNER JOIN cd.dma_detail dma ON dma.merchant_no = ktp.merchant_no " +
            "    INNER JOIN cd.summary_matching suma ON suma.ktp_file_id = ktp.ktp_file_id " +
            "    LEFT JOIN cd.sdn_entry entrysdn ON entrysdn.sdnfile_id = suma.sdnfile_id_sdn " +
            "    LEFT JOIN cd.sdn_aka aka ON aka.sdn_entry_id = entrysdn.sdn_entry_id " +
            "    LEFT JOIN cd.sdn_id si ON si.sdn_entry_id = entrysdn.sdn_entry_id " +
            "    LEFT JOIN cd.summary_matching_detail sumad ON sumad.ktp_detail_id = ktp.ktp_detail_id " +
            "    WHERE  " +
            "   (lower(entrysdn.first_name) = lower(ktp.name_1) OR " +
            "    lower(entrysdn.first_name) = lower(ktp.name_2) OR " +
            "    lower(entrysdn.last_name) = lower(ktp.name_1) OR " +
            "    lower(entrysdn.last_name) = lower(ktp.name_2) OR " +
            "    lower(aka.first_name) = lower(ktp.name_1) OR " +
            "    lower(aka.first_name) = lower(ktp.name_2) OR " +
            "    lower(aka.last_name) = lower(ktp.name_1) OR " +
            "    lower(aka.last_name) = lower(ktp.name_2) OR " +
            "    si.id_number = ktp.ktp_1 OR " +
            "    si.id_number = ktp.ktp_2 )  " +
            "    AND sumad.ktp_detail_id isnull AND suma.status = 'matching' " +
            "    UNION ALL " +
            "    SELECT distinct'active',suma.summary_matching_id,ktp.ktp_detail_id,entrysdn.sdn_entry_id,'potensial','SYSTEM-AUTO','SYSTEM-AUTO',current_timestamp,current_timestamp\n" +
            "    FROM cd.ktp_detail ktp " +
            "    INNER JOIN cd.dma_detail dma ON dma.merchant_no = ktp.merchant_no " +
            "    INNER JOIN cd.summary_matching suma ON suma.ktp_file_id = ktp.ktp_file_id " +
            "    LEFT JOIN cd.sdn_entry entrysdn ON entrysdn.sdnfile_id = suma.sdnfile_id_consolidate " +
            "    LEFT JOIN cd.sdn_aka aka ON aka.sdn_entry_id = entrysdn.sdn_entry_id " +
            "    LEFT JOIN cd.sdn_id si ON si.sdn_entry_id = entrysdn.sdn_entry_id " +
            "    LEFT JOIN cd.summary_matching_detail sumad ON sumad.ktp_detail_id = ktp.ktp_detail_id " +
            "    WHERE  " +
            "   (lower(entrysdn.first_name) = lower(ktp.name_1) OR " +
            "    lower(entrysdn.first_name) = lower(ktp.name_2) OR " +
            "    lower(entrysdn.last_name) = lower(ktp.name_1) OR " +
            "    lower(entrysdn.last_name) = lower(ktp.name_2) OR " +
            "    lower(aka.first_name) = lower(ktp.name_1) OR " +
            "    lower(aka.first_name) = lower(ktp.name_2) OR " +
            "    lower(aka.last_name) = lower(ktp.name_1) OR " +
            "    lower(aka.last_name) = lower(ktp.name_2) OR " +
            "    si.id_number = ktp.ktp_1 OR " +
            "    si.id_number = ktp.ktp_2 )  " +
            "    AND sumad.ktp_detail_id isnull AND suma.status = 'matching' ", nativeQuery = true)
    void matchingPotential();


    @Modifying
    @Query(value = "INSERT INTO cd.summary_matching_detail (status,summary_matching_id,ktp_detail_id,sdn_entry_id,matching_status,created_by,updated_by,created_date,updated_date) " +
            "    SELECT distinct 'active',suma.summary_matching_id,ktp.ktp_detail_id,entrysdn.sdn_entry_id,'positive','SYSTEM-AUTO','SYSTEM-AUTO',current_timestamp,current_timestamp " +
            "    FROM cd.ktp_detail ktp " +
            "    INNER JOIN cd.dma_detail dma ON dma.merchant_no = ktp.merchant_no " +
            "    INNER JOIN cd.summary_matching suma ON suma.ktp_file_id = ktp.ktp_file_id " +
            "    LEFT JOIN cd.sdn_entry entrysdn ON entrysdn.sdnfile_id = suma.sdnfile_id_sdn " +
            "    LEFT JOIN cd.sdn_aka aka ON aka.sdn_entry_id = entrysdn.sdn_entry_id " +
            "    LEFT JOIN cd.sdn_id si ON si.sdn_entry_id = entrysdn.sdn_entry_id " +
            "    LEFT JOIN cd.sdn_dob dob ON dob.sdn_entry_id = entrysdn.sdn_entry_id " +
            "    WHERE  " +
            "   (lower(entrysdn.first_name) = lower(ktp.name_1) OR " +
            "    lower(entrysdn.first_name) = lower(ktp.name_2) OR " +
            "    lower(entrysdn.last_name) = lower(ktp.name_1) OR " +
            "    lower(entrysdn.last_name) = lower(ktp.name_2) OR " +
            "    lower(aka.first_name) = lower(ktp.name_1) OR " +
            "    lower(aka.first_name) = lower(ktp.name_2) OR " +
            "    lower(aka.last_name) = lower(ktp.name_1) OR " +
            "    lower(aka.last_name) = lower(ktp.name_2) ) AND " +
            "    (si.id_number = ktp.ktp_1 OR " +
            "    si.id_number = ktp.ktp_2 ) AND " +
            "    (dob.dob=ktp.dob_1 OR dob.dob=dob_2) " +
            "    AND suma.status = 'matching' " +
            "    UNION ALL " +
            "    SELECT distinct'active',suma.summary_matching_id,ktp.ktp_detail_id,entrysdn.sdn_entry_id,'potensial','SYSTEM-AUTO','SYSTEM-AUTO',current_timestamp,current_timestamp\n" +
            "    FROM cd.ktp_detail ktp " +
            "    INNER JOIN cd.dma_detail dma ON dma.merchant_no = ktp.merchant_no " +
            "    INNER JOIN cd.summary_matching suma ON suma.ktp_file_id = ktp.ktp_file_id " +
            "    LEFT JOIN cd.sdn_entry entrysdn ON entrysdn.sdnfile_id = suma.sdnfile_id_consolidate " +
            "    LEFT JOIN cd.sdn_aka aka ON aka.sdn_entry_id = entrysdn.sdn_entry_id " +
            "    LEFT JOIN cd.sdn_id si ON si.sdn_entry_id = entrysdn.sdn_entry_id " +
            "    LEFT JOIN cd.sdn_dob dob ON dob.sdn_entry_id = entrysdn.sdn_entry_id " +
            "    WHERE  " +
            "   (lower(entrysdn.first_name) = lower(ktp.name_1) OR " +
            "    lower(entrysdn.first_name) = lower(ktp.name_2) OR " +
            "    lower(entrysdn.last_name) = lower(ktp.name_1) OR " +
            "    lower(entrysdn.last_name) = lower(ktp.name_2) OR " +
            "    lower(aka.first_name) = lower(ktp.name_1) OR " +
            "    lower(aka.first_name) = lower(ktp.name_2) OR " +
            "    lower(aka.last_name) = lower(ktp.name_1) OR " +
            "    lower(aka.last_name) = lower(ktp.name_2) ) AND " +
            "    (si.id_number = ktp.ktp_1 OR " +
            "    si.id_number = ktp.ktp_2 ) AND " +
            "    (dob.dob=ktp.dob_1 OR dob.dob=dob_2) " +
            "    AND suma.status = 'matching' ", nativeQuery = true)
    void matchingPositive();


    @Query(value = " select count(distinct(ktp_detail_id)) from cd.summary_matching_detail where matching_status =:status", nativeQuery = true)
    int getDistinctMatchingDetailByStatus(@Param("status") String status);

    @Query(value = " select * from cd.summary_matching_detail where matching_status =:status order by sdn_entry_id ASC", nativeQuery = true)
    List<SummaryMatchingDetail> getMatchingDetailByStatus(@Param("status") String status);

}
