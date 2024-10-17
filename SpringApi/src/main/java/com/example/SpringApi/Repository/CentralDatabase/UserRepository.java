package com.example.SpringApi.Repository.CentralDatabase;

import com.example.SpringApi.DatabaseModels.CentralDatabase.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("select u from User u join UserCarrierMapping ucm on u.userId = ucm.userId " +
            "where ucm.carrierId = :carrierId " +
            "and (:selectedUsers IS NULL OR u.userId IN (:selectedUsers)) " +
            "and u.emailConfirmed " +
            "and (:includeDeleted = true OR u.deleted = false) " +
            "AND (COALESCE(:filterExpr, '') = '' OR " +
            "(CASE :columnName " +
            "WHEN 'firstName' THEN CONCAT(u.firstName, '') " +
            "WHEN 'lastName' THEN CONCAT(u.lastName, '') " +
            "WHEN 'loginName' THEN CONCAT(u.loginName, '') " +
            "WHEN 'role' THEN CONCAT(u.role, '') " +
            "WHEN 'dob' THEN CONCAT(u.dob, '') " +
            "WHEN 'phone' THEN CONCAT(u.phone, '') " +
            "ELSE '' END) LIKE " +
            "(CASE :condition " +
            "WHEN 'contains' THEN CONCAT('%', :filterExpr, '%') " +
            "WHEN 'equals' THEN :filterExpr " +
            "WHEN 'startsWith' THEN CONCAT(:filterExpr, '%') " +
            "WHEN 'endsWith' THEN CONCAT('%', :filterExpr) " +
            "WHEN 'isEmpty' THEN '' " +
            "WHEN 'isNotEmpty' THEN '%' " +
            "ELSE '' END))")
    Page<User> findPaginatedUsers(@Param("carrierId") long carrierId,
                                  @Param("selectedUsers") List<Long> selectedUsers,
                                  @Param("columnName") String columnName,
                                  @Param("condition") String condition,
                                  @Param("filterExpr") String filterExpr,
                                  @Param("includeDeleted") boolean includeDeleted,
                                  Pageable pageable);

    User findByLoginName(String loginName);

    @Query(value = "SELECT u from User u where (:includeDeleted = true or u.deleted = true)")
    List<User> findAllWithIncludeDeleted(@Param("includeDeleted") boolean includeDeleted);

    @Query(value = "SELECT u from User u JOIN UserCarrierMapping ucm on u.userId = ucm.userId where ucm.carrierId = :carrierId and (:includeDeleted = true or u.deleted = false)")
    List<User> findAllWithIncludeDeletedInCarrier(@Param("includeDeleted") boolean includeDeleted, @Param("carrierId")long carrierId);
}
