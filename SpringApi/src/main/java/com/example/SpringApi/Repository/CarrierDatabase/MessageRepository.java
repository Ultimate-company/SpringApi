package com.example.SpringApi.Repository.CarrierDatabase;

import com.example.SpringApi.DatabaseModels.CarrierDatabase.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("select m, COUNT(distinct mum.userId), COUNT(distinct mugm.userGroupId) from Message m " +
            "left join MessageUserMap mum on m.messageId = mum.messageId " +
            "left join MessageUserGroupMap mugm on m.messageId = mugm.messageId " +
            "where (:includeDeleted = true OR m.deleted = false) " +
            "and (COALESCE(:filterExpr, '') = '' OR " +
            "(CASE :columnName " +
            "WHEN 'title' THEN CONCAT(m.title, '') " +
            "WHEN 'publishDate' THEN CONCAT(m.publishDate, '') " +
            "ELSE '' END) LIKE " +
            "(CASE :condition " +
            "WHEN 'contains' THEN CONCAT('%', :filterExpr, '%') " +
            "WHEN 'equals' THEN :filterExpr " +
            "WHEN 'startsWith' THEN CONCAT(:filterExpr, '%') " +
            "WHEN 'endsWith' THEN CONCAT('%', :filterExpr) " +
            "WHEN 'isEmpty' THEN '' " +
            "WHEN 'isNotEmpty' THEN '%' " +
            "ELSE '' END)) " +
            "GROUP BY m")
    Page<Object[]> findPaginatedMessages(@Param("columnName") String columnName,
                                            @Param("condition") String condition,
                                            @Param("filterExpr") String filterExpr,
                                            @Param("includeDeleted") boolean includeDeleted,
                                            Pageable pageable);
}
