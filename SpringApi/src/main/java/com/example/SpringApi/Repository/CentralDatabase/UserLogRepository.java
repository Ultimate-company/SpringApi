package com.example.SpringApi.Repository.CentralDatabase;

import com.example.SpringApi.DatabaseModels.CentralDatabase.UserLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserLogRepository extends JpaRepository<UserLog, Long> {

    @Query("select ul from UserLog ul join UserCarrierMapping ucm on ul.userId = ucm.userId " +
            "where ul.userId = :userId and ucm.carrierId = :carrierId " +
            "AND (COALESCE(:filterExpr, '') = '' OR " +
            "(CASE :columnName " +
            "WHEN 'change' THEN CONCAT(ul.change, '') " +
            "WHEN 'oldValue' THEN CONCAT(ul.oldValue, '') " +
            "WHEN 'newValue' THEN CONCAT(ul.newValue, '') " +
            "ELSE '' END) LIKE " +
            "(CASE :condition " +
            "WHEN 'contains' THEN CONCAT('%', :filterExpr, '%') " +
            "WHEN 'equals' THEN :filterExpr " +
            "WHEN 'startsWith' THEN CONCAT(:filterExpr, '%') " +
            "WHEN 'endsWith' THEN CONCAT('%', :filterExpr) " +
            "WHEN 'isEmpty' THEN '' " +
            "WHEN 'isNotEmpty' THEN '%' " +
            "ELSE '' END))")
    Page<UserLog> findPaginatedUserLogs(@Param("userId") long userId,
                                        @Param("carrierId") long carrierId,
                                        @Param("columnName") String columnName,
                                        @Param("condition") String condition,
                                        @Param("filterExpr") String filterExpr,
                                        Pageable pageable);
}
