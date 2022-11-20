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
    @Query(value = "INSERT INTO ofac.summary_matching_detail (status,summary_matching_id,ktp_detail_id,sdn_entry_id,matching_status,created_by,updated_by,created_date,updated_date)   " +
            "SELECT distinct 'active',t.summary_matching_id,t.ktp_detail_id,t.sdn_entry_id,'potential','SYSTEM-AUTO','SYSTEM-AUTO',current_timestamp,current_timestamp  " +
            "from ( " +
            "              ---------------- SDN -------------- " +
            "      " +
            "             /*compare sdn entry name*/ " +
            "    SELECT suma.summary_matching_id,ktp.ktp_detail_id,entrysdn.sdn_entry_id " +
            "             FROM ofac.ktp_detail ktp   " +
            "             INNER JOIN ofac.dma_detail dma ON dma.merchant_no = ktp.merchant_no   " +
            "             INNER JOIN ofac.summary_matching suma ON suma.ktp_file_id = ktp.ktp_file_id  and suma.status = 'matching'   " +
            "             INNER JOIN ofac.sdn_entry entrysdn ON entrysdn.sdnfile_id = suma.sdn_file_id_sdn  and   " +
            "             (entrysdn.first_name = ktp.name_1 OR   " +
            "             entrysdn.first_name = ktp.name_2 OR   " +
            "             entrysdn.last_name = ktp.name_1 OR   " +
            "             entrysdn.last_name =ktp.name_2)    " +
            "              " +
            "             union all " +
            "              " +
            "             /*compare sdn aka name*/              " +
            "             SELECT suma.summary_matching_id,ktp.ktp_detail_id,entrysdn.sdn_entry_id " +
            "             FROM ofac.ktp_detail ktp   " +
            "             INNER JOIN ofac.dma_detail dma ON dma.merchant_no = ktp.merchant_no   " +
            "             INNER JOIN ofac.summary_matching suma ON suma.ktp_file_id = ktp.ktp_file_id  and suma.status = 'matching'  " +
            "             INNER JOIN ofac.sdn_entry entrysdn ON entrysdn.sdnfile_id = suma.sdn_file_id_sdn   " +
            "             INNER JOIN ofac.sdn_aka aka ON aka.sdn_entry_id = entrysdn.sdn_entry_id  and " +
            "             (   " +
            "             aka.first_name = ktp.name_1 OR   " +
            "             aka.first_name =ktp.name_2 OR   " +
            "             aka.last_name = ktp.name_1 OR   " +
            "             aka.last_name =ktp.name_2   )     " +
            "              " +
            "             union all " +
            "              " +
            "             /*compare sdn id*/                " +
            "             SELECT suma.summary_matching_id,ktp.ktp_detail_id,entrysdn.sdn_entry_id   " +
            "             FROM ofac.ktp_detail ktp   " +
            "             INNER JOIN ofac.dma_detail dma ON dma.merchant_no = ktp.merchant_no   " +
            "             INNER JOIN ofac.summary_matching suma ON suma.ktp_file_id = ktp.ktp_file_id  and suma.status = 'matching'  " +
            "             INNER JOIN ofac.sdn_entry entrysdn ON entrysdn.sdnfile_id = suma.sdn_file_id_sdn   " +
            "             INNER JOIN ofac.sdn_id si ON si.sdn_entry_id = entrysdn.sdn_entry_id  and  " +
            "             (si.id_number = ktp.ktp_1 OR   " +
            "             si.id_number = ktp.ktp_2 )     " +
            "              " +
            "              " +
            "             ---------------- CONSOLIDATE -------------- " +
            "             union all " +
            "              " +
            "             /*compare sdn entry name*/ " +
            "             SELECT suma.summary_matching_id,ktp.ktp_detail_id,entrysdn.sdn_entry_id " +
            "             FROM ofac.ktp_detail ktp   " +
            "             INNER JOIN ofac.dma_detail dma ON dma.merchant_no = ktp.merchant_no   " +
            "             INNER JOIN ofac.summary_matching suma ON suma.ktp_file_id = ktp.ktp_file_id  and suma.status = 'matching'   " +
            "             INNER JOIN ofac.sdn_entry entrysdn ON entrysdn.sdnfile_id = suma.sdn_file_id_consolidate  and   " +
            "             (entrysdn.first_name = ktp.name_1 OR   " +
            "             entrysdn.first_name = ktp.name_2 OR   " +
            "             entrysdn.last_name = ktp.name_1 OR   " +
            "             entrysdn.last_name =ktp.name_2)    " +
            "              " +
            "             union all " +
            "              " +
            "             /*compare sdn aka name*/     " +
            "             SELECT suma.summary_matching_id,ktp.ktp_detail_id,entrysdn.sdn_entry_id " +
            "             FROM ofac.ktp_detail ktp   " +
            "             INNER JOIN ofac.dma_detail dma ON dma.merchant_no = ktp.merchant_no   " +
            "             INNER JOIN ofac.summary_matching suma ON suma.ktp_file_id = ktp.ktp_file_id  and suma.status = 'matching'  " +
            "             INNER JOIN ofac.sdn_entry entrysdn ON entrysdn.sdnfile_id = suma.sdn_file_id_consolidate   " +
            "             INNER JOIN ofac.sdn_aka aka ON aka.sdn_entry_id = entrysdn.sdn_entry_id  and " +
            "             (   " +
            "             aka.first_name = ktp.name_1 OR   " +
            "             aka.first_name =ktp.name_2 OR   " +
            "             aka.last_name = ktp.name_1 OR   " +
            "             aka.last_name =ktp.name_2   )     " +
            "              " +
            "             union all " +
            "              " +
            "             /*compare sdn id*/    " +
            "             SELECT suma.summary_matching_id,ktp.ktp_detail_id,entrysdn.sdn_entry_id   " +
            "             FROM ofac.ktp_detail ktp   " +
            "             INNER JOIN ofac.dma_detail dma ON dma.merchant_no = ktp.merchant_no   " +
            "             INNER JOIN ofac.summary_matching suma ON suma.ktp_file_id = ktp.ktp_file_id  and suma.status = 'matching'  " +
            "             INNER JOIN ofac.sdn_entry entrysdn ON entrysdn.sdnfile_id = suma.sdn_file_id_consolidate   " +
            "             INNER JOIN ofac.sdn_id si ON si.sdn_entry_id = entrysdn.sdn_entry_id  and  " +
            "             (si.id_number = ktp.ktp_1 OR   " +
            "             si.id_number = ktp.ktp_2 )     " +
            "              " +
            "  ) t " +
            "  where not exists ( " +
            "   select 1  " +
            "    from ofac.summary_matching_detail sumad  " +
            "    where sumad.ktp_detail_id = t.ktp_detail_id   " +
            "  )  ", nativeQuery = true)
    void matchingPotential();


    @Modifying
    @Query(value = "INSERT INTO ofac.summary_matching_detail (status,summary_matching_id,ktp_detail_id,sdn_entry_id,matching_status,created_by,updated_by,created_date,updated_date)    " +
            "SELECT distinct 'active',t.summary_matching_id,t.ktp_detail_id,t.sdn_entry_id,'positive','SYSTEM-AUTO','SYSTEM-AUTO',current_timestamp,current_timestamp   " +
            "from (  " +
            "              ---------------- SDN --------------  " +
            "       " +
            "             /*compare sdn entry, sdn id, sdn dob*/     " +
            "    SELECT suma.summary_matching_id,ktp.ktp_detail_id,entrysdn.sdn_entry_id  " +
            "             FROM ofac.ktp_detail ktp    " +
            "             INNER JOIN ofac.dma_detail dma ON dma.merchant_no = ktp.merchant_no    " +
            "             INNER JOIN ofac.summary_matching suma ON suma.ktp_file_id = ktp.ktp_file_id  and suma.status = 'matching'    " +
            "             INNER JOIN ofac.sdn_entry entrysdn ON entrysdn.sdnfile_id = suma.sdn_file_id_sdn  and    " +
            "             (entrysdn.first_name = ktp.name_1 OR    " +
            "             entrysdn.first_name = ktp.name_2 OR    " +
            "             entrysdn.last_name = ktp.name_1 OR    " +
            "             entrysdn.last_name =ktp.name_2)     " +
            "             INNER JOIN ofac.sdn_id si ON si.sdn_entry_id = entrysdn.sdn_entry_id  and   " +
            "             (si.id_number = ktp.ktp_1 OR    " +
            "             si.id_number = ktp.ktp_2 )      " +
            "             INNER JOIN ofac.sdn_dob dob ON dob.sdn_entry_id = entrysdn.sdn_entry_id and  " +
            "             (dob.dob=ktp.dob_1 OR dob.dob=dob_2)  " +
            "               " +
            "             union all  " +
            "               " +
            "             /*compare sdn aka, sdn id, sdn dob*/   " +
            "             SELECT suma.summary_matching_id,ktp.ktp_detail_id,entrysdn.sdn_entry_id  " +
            "             FROM ofac.ktp_detail ktp    " +
            "             INNER JOIN ofac.dma_detail dma ON dma.merchant_no = ktp.merchant_no    " +
            "             INNER JOIN ofac.summary_matching suma ON suma.ktp_file_id = ktp.ktp_file_id  and suma.status = 'matching'   " +
            "             INNER JOIN ofac.sdn_entry entrysdn ON entrysdn.sdnfile_id = suma.sdn_file_id_sdn    " +
            "             INNER JOIN ofac.sdn_aka aka ON aka.sdn_entry_id = entrysdn.sdn_entry_id  and  " +
            "             (    " +
            "             aka.first_name = ktp.name_1 OR    " +
            "             aka.first_name =ktp.name_2 OR    " +
            "             aka.last_name = ktp.name_1 OR    " +
            "             aka.last_name =ktp.name_2   )      " +
            "             INNER JOIN ofac.sdn_id si ON si.sdn_entry_id = entrysdn.sdn_entry_id  and   " +
            "             (si.id_number = ktp.ktp_1 OR    " +
            "             si.id_number = ktp.ktp_2 )      " +
            "             INNER JOIN ofac.sdn_dob dob ON dob.sdn_entry_id = entrysdn.sdn_entry_id and  " +
            "             (dob.dob=ktp.dob_1 OR dob.dob=dob_2)               " +
            "            " +
            "             ---------------- CONSOLIDATE --------------  " +
            "             union all  " +
            "               " +
            "             /*compare sdn entry, sdn id, sdn dob*/   " +
            "    SELECT suma.summary_matching_id,ktp.ktp_detail_id,entrysdn.sdn_entry_id  " +
            "             FROM ofac.ktp_detail ktp    " +
            "             INNER JOIN ofac.dma_detail dma ON dma.merchant_no = ktp.merchant_no    " +
            "             INNER JOIN ofac.summary_matching suma ON suma.ktp_file_id = ktp.ktp_file_id  and suma.status = 'matching'    " +
            "             INNER JOIN ofac.sdn_entry entrysdn ON entrysdn.sdnfile_id = suma.sdn_file_id_consolidate  and    " +
            "             (entrysdn.first_name = ktp.name_1 OR    " +
            "             entrysdn.first_name = ktp.name_2 OR    " +
            "             entrysdn.last_name = ktp.name_1 OR    " +
            "             entrysdn.last_name =ktp.name_2)     " +
            "             INNER JOIN ofac.sdn_id si ON si.sdn_entry_id = entrysdn.sdn_entry_id  and   " +
            "             (si.id_number = ktp.ktp_1 OR    " +
            "             si.id_number = ktp.ktp_2 )      " +
            "             INNER JOIN ofac.sdn_dob dob ON dob.sdn_entry_id = entrysdn.sdn_entry_id and  " +
            "             (dob.dob=ktp.dob_1 OR dob.dob=dob_2)  " +
            "               " +
            "             union all  " +
            "               " +
            "             /*compare sdn aka, sdn id, sdn dob*/   " +
            "             SELECT suma.summary_matching_id,ktp.ktp_detail_id,entrysdn.sdn_entry_id  " +
            "             FROM ofac.ktp_detail ktp    " +
            "             INNER JOIN ofac.dma_detail dma ON dma.merchant_no = ktp.merchant_no    " +
            "             INNER JOIN ofac.summary_matching suma ON suma.ktp_file_id = ktp.ktp_file_id  and suma.status = 'matching'   " +
            "             INNER JOIN ofac.sdn_entry entrysdn ON entrysdn.sdnfile_id = suma.sdn_file_id_consolidate  " +
            "             INNER JOIN ofac.sdn_aka aka ON aka.sdn_entry_id = entrysdn.sdn_entry_id  and  " +
            "             (    " +
            "             aka.first_name = ktp.name_1 OR    " +
            "             aka.first_name =ktp.name_2 OR    " +
            "             aka.last_name = ktp.name_1 OR    " +
            "             aka.last_name =ktp.name_2   )      " +
            "             INNER JOIN ofac.sdn_id si ON si.sdn_entry_id = entrysdn.sdn_entry_id  and   " +
            "             (si.id_number = ktp.ktp_1 OR    " +
            "             si.id_number = ktp.ktp_2 )      " +
            "             INNER JOIN ofac.sdn_dob dob ON dob.sdn_entry_id = entrysdn.sdn_entry_id and  " +
            "             (dob.dob=ktp.dob_1 OR dob.dob=dob_2)        " +
            "               " +
            "  ) t  ", nativeQuery = true)
    void matchingPositive();


    @Query(value = "select distinct(ktp_detail_id) from ofac.summary_matching_detail where matching_status =:status", nativeQuery = true)
    List<Integer> getDistinctMatchingDetailByStatus(@Param("status") String status);

    @Query(value = "select * from ofac.summary_matching_detail where ktp_detail_id = :ktp_detail_id limit 1", nativeQuery = true)
    SummaryMatchingDetail getSummaryDetailByKtpDetailId(@Param("ktp_detail_id") int ktp_detail_id);

    @Query(value = " select * from ofac.summary_matching_detail where matching_status =:status order by sdn_entry_id ASC", nativeQuery = true)
    List<SummaryMatchingDetail> getMatchingDetailByStatus(@Param("status") String status);

    @Modifying
    @Query(value ="TRUNCATE TABLE ofac.summary_matching_detail RESTART IDENTITY CASCADE;" ,nativeQuery = true)
    void deleteAllAndResetIdentity();

}
