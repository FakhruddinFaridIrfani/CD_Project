package com.consolidate.project.repository;

import com.consolidate.project.model.Role;
import com.consolidate.project.model.SystemParameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public interface SystemParameterRepository extends JpaRepository<SystemParameter, Integer> {

    @Query(value = "SELECT * FROM cd.system_parameter where status <> 'deleted'", nativeQuery = true)
    List<SystemParameter> getSystemParameter();

    @Modifying
    @Query(value = "UPDATE cd.system_parameter SET parameter_name = :parameter_name,parameter_value=:parameter_value,updated_by=:updated_by,updated_date=current_timestamp WHERE parametersystem_id =:parametersystem_id ", nativeQuery = true)
    void updateSystemParameter(@Param("parameter_name") String parameter_name, @Param("parameter_value") String parameter_value,
                               @Param("updated_by") String updated_by, @Param("parametersystem_id") int parametersystem_id);

    @Modifying
    @Query(value = "UPDATE cd.system_parameter SET status = 'deleted',updated_by=:updated_by,updated_date=current_timestamp WHERE parametersystem_id=:parametersystem_id", nativeQuery = true)
    void deleteSystemParameter(@Param("parametersystem_id") int parametersystem_id, @Param("updated_by") String updated_by);

    @Query(value = "SELECT * FROM cd.system_parameter WHERE lower(parameter_name) = lower(:parameter_name) AND status <> 'deleted'", nativeQuery = true)
    List<SystemParameter> getSystemParameterByName(@Param("parameter_name") String parameter_name);

    @Query(value = "SELECT * FROM cd.system_parameter WHERE lower(parameter_name) like lower(:parameter_name) AND parametersystem_id not in (:parametersystem_id) AND status <> 'deleted'", nativeQuery = true)
    List<SystemParameter> getRoleByNameExceptId(@Param("parameter_name") String parameter_name, @Param("parametersystem_id") int parametersystem_id);


}
