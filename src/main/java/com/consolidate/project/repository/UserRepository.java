package com.consolidate.project.repository;

import com.consolidate.project.model.Role;
import com.consolidate.project.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public interface UserRepository extends JpaRepository<Users, Integer> {

    @Query(value = "SELECT * FROM ofac.Users WHERE " +
            "lower(user_name) like lower(:user_name) " +
            "AND lower(user_organization) like lower(:user_organization) " +
            "AND CAST(role_id AS VARCHAR) like :role_id " +
            "AND status like :status " +
            "AND status <> 'deleted' " +
            "AND lower(user_full_name) like lower(:user_full_name) " +
            "ORDER BY user_name ASC", nativeQuery = true)
    List<Users> getUsersList(@Param("user_name") String user_name,
                             @Param("user_organization") String user_organization,
                             @Param("role_id") String role_id,
                             @Param("status") String status,
                             @Param("user_full_name") String user_full_name);

    @Modifying
    @Query(value = "INSERT INTO ofac.Users(user_name,user_password,role_id,user_organization,status,user_full_name,created_by,created_date,updated_by,updated_date,user_token) " +
            "VALUES(:user_name,crypt(:user_password, gen_salt('bf')),:role_id,:user_organization,'active',:user_full_name,:created_by,current_timestamp,:created_by,current_timestamp,:user_token)", nativeQuery = true)
    void save(@Param("user_name") String user_name, @Param("user_password") String user_password,
              @Param("role_id") int role_id, @Param("user_organization") String user_organization,
              @Param("user_full_name") String user_full_name, @Param("created_by") String created_by, @Param("user_token") String user_token);

    @Modifying
    @Query(value = "UPDATE ofac.Users SET role_id=:role_id,user_organization=:user_organization," +
            "status=:status,user_full_name=:user_full_name,updated_by=:updated_by,updated_date=current_timestamp WHERE user_id =:user_id ", nativeQuery = true)
    void updateUser(@Param("role_id") int role_id, @Param("user_organization") String user_organization,
                    @Param("status") String status, @Param("user_full_name") String user_full_name,
                    @Param("updated_by") String updated_by, @Param("user_id") int user_id);

    @Modifying
    @Query(value = "UPDATE ofac.Users SET status = 'deleted',updated_by=:updated_by,updated_date=current_timestamp WHERE user_id=:user_id", nativeQuery = true)
    void deleteUser(@Param("user_id") int user_id, @Param("updated_by") String updated_by);

    @Query(value = "SELECT * FROM ofac.Users WHERE lower(user_name) = lower(:user_name) AND status <> 'deleted'", nativeQuery = true)
    List<Users> getUsersByName(@Param("user_name") String user_name);

    @Query(value = "SELECT * FROM ofac.Users WHERE lower(user_name) = lower(:user_name) AND user_id not in(:user_id) AND status <> 'deleted'", nativeQuery = true)
    List<Users> getUsersByNameExceptId(@Param("user_name") String user_name, @Param("user_id") int user_id);

    @Query(value = "SELECT * FROM cms.user_role WHERE role_id =:role_id and status <>'deleted'", nativeQuery = true)
    List<Users> getUserByRoleId(@Param("role_id") int role_id);

    @Query(value = "SELECT * FROM cms.user_role WHERE user_id =:user_id and status <>'deleted'", nativeQuery = true)
    List<Users> getUserById(@Param("user_id") int user_id);

    @Query(value = "SELECT * FROM ofac.Users " +
            "where user_name =:user_name AND user_password = crypt(:user_password, user_password)", nativeQuery = true)
    List<Users> loginUser(@Param("user_name") String user_name, @Param("user_password") String user_password);


    @Modifying
    @Query(value = "UPDATE ofac.Users SET user_password = crypt(:user_password, gen_salt('bf')) WHERE user_id = :user_id ", nativeQuery = true)
    void changeUsersPassword(@Param("user_id") int user_id, @Param("user_password") String newPassword);

    @Modifying
    @Query(value = "UPDATE ofac.Users SET user_token=:user_token " +
            "where user_name =:user_name AND user_password = crypt(:user_password, user_password)", nativeQuery = true)
    void updateUserToken(@Param("user_name") String user_name, @Param("user_password") String user_password, @Param("user_token") String user_token);

    @Query(value = "SELECT * FROM ofac.Users WHERE user_token =:user_token", nativeQuery = true)
    List<Users> tokenAuth(@Param("user_token") String user_token);
}
