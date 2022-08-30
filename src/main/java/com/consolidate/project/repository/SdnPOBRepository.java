package com.consolidate.project.repository;

import com.consolidate.project.model.SdnDOB;
import com.consolidate.project.model.SdnPOB;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
@Transactional
public interface SdnPOBRepository extends JpaRepository<SdnPOB, Integer> {


}
