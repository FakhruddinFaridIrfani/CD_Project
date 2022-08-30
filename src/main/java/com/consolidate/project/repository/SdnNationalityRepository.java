package com.consolidate.project.repository;

import com.consolidate.project.model.SdnCitizenship;
import com.consolidate.project.model.SdnNationality;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
@Transactional
public interface SdnNationalityRepository extends JpaRepository<SdnNationality, Integer> {


}
