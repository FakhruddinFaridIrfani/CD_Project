package com.consolidate.project.repository;

import com.consolidate.project.model.Role;
import com.consolidate.project.model.SdnID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public interface RoleRepository extends JpaRepository<Role, Integer> {


    @Query(value = "SELECT * FROM ofac.Role WHERE " +
            "lower(role_name) like lower(:role_name) AND status <> 'deleted' " +
            "ORDER BY created_date DESC", nativeQuery = true)
    List<Role> getRole(@Param("role_name") String role_name);

    @Query(value = "SELECT * FROM ofac.role where role_id=:role_id AND status <> 'deleted'", nativeQuery = true)
    List<Role> getRoleById(@Param("role_id") int role_id);

    @Query(value = "SELECT * FROM ofac.Role WHERE lower(role_name)  = lower(:role_name) AND status <> 'deleted'", nativeQuery = true)
    List<Role> getRoleByName(@Param("role_name") String role_name);

    @Query(value = "SELECT * FROM ofac.Role WHERE lower(role_name) = lower(:role_name) AND role_id not in (:role_id) AND status <> 'deleted'", nativeQuery = true)
    List<Role> getRoleByNameExceptId(@Param("role_name") String role_name, @Param("role_id") int role_id);

    @Modifying
    @Query(value = "UPDATE ofac.Role SET role_name=:role_name,status=:status," +
            "updated_by=:updated_by,updated_date=current_timestamp WHERE role_id =:role_id ", nativeQuery = true)
    void updateRole(@Param("role_name") String role_name, @Param("status") String status,
                    @Param("updated_by") String updated_by, @Param("role_id") int role_id);

    @Modifying
    @Query(value = "UPDATE ofac.Role SET status = 'deleted',updated_by=:updated_by," +
            "updated_date=current_timestamp WHERE role_id=:role_id", nativeQuery = true)
    void deleteRole(@Param("role_id") int role_id, @Param("updated_by") String updated_by);


}
