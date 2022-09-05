package com.consolidate.project.repository;

import com.consolidate.project.model.SdnAka;
import com.consolidate.project.model.SdnLogger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
@Transactional
public interface SdnLoggerRepository extends JpaRepository<SdnLogger, Integer> {



}
