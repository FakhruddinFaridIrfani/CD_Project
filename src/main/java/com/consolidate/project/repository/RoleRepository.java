package com.consolidate.project.repository;

import com.consolidate.project.model.Role;
import com.consolidate.project.model.SdnID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
@Transactional
public interface RoleRepository extends JpaRepository<Role, Integer> {


}
