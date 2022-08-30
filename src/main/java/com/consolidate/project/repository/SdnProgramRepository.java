package com.consolidate.project.repository;

import com.consolidate.project.model.SdnFile;
import com.consolidate.project.model.SdnProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
@Transactional
public interface SdnProgramRepository extends JpaRepository<SdnProgram, Integer> {


}
