package com.consolidate.project.repository;

import com.consolidate.project.model.SdnAka;
import com.consolidate.project.model.SdnProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
@Transactional
public interface SdnAkaRepository extends JpaRepository<SdnAka, Integer> {


}
