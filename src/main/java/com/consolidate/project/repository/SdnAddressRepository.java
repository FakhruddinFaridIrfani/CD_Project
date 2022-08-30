package com.consolidate.project.repository;

import com.consolidate.project.model.SdnAddress;
import com.consolidate.project.model.SdnAka;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
@Transactional
public interface SdnAddressRepository extends JpaRepository<SdnAddress, Integer> {


}
