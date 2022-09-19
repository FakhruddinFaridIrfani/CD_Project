package com.consolidate.project.repository;

import com.consolidate.project.model.Report;
import com.consolidate.project.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public interface ReportRepository extends JpaRepository<Report, Integer> {

    @Query(value = "SELECT * FROM ofac.report where report_id = :report_id", nativeQuery = true)
    List<Report> getReportById(@Param("report_id") int report_id);


}
