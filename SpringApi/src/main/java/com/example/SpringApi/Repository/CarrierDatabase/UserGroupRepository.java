package com.example.SpringApi.Repository.CarrierDatabase;

import com.example.SpringApi.DatabaseModels.CarrierDatabase.UserGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserGroupRepository extends JpaRepository<UserGroup, Long> {
    UserGroup findByName(String name);

    @Query("SELECT ugm.userId FROM UserGroupsUsersMap ugm WHERE ugm.userGroupId = :userGroupId")
    List<Long> getUserIdsInGroup(@Param("userGroupId") long userGroupId);

    @Query("SELECT ug, COUNT(ugm.userId) " +
            "FROM UserGroup ug " +
            "LEFT JOIN UserGroupsUsersMap ugm ON ug.userGroupId = ugm.userGroupId " +
            "WHERE (:includeDeleted = true OR ug.deleted = false) " +
            "AND (COALESCE(:filterExpr, '') = '' OR " +
            "(CASE :columnName " +
            "WHEN 'name' THEN CONCAT(ug.name, '') " +
            "WHEN 'description' THEN CONCAT(ug.description, '') " +
            "ELSE '' END) LIKE " +
            "(CASE :condition " +
            "WHEN 'contains' THEN CONCAT('%', :filterExpr, '%') " +
            "WHEN 'equals' THEN :filterExpr " +
            "WHEN 'startsWith' THEN CONCAT(:filterExpr, '%') " +
            "WHEN 'endsWith' THEN CONCAT('%', :filterExpr) " +
            "WHEN 'isEmpty' THEN '' " +
            "WHEN 'isNotEmpty' THEN '%' " +
            "ELSE '' END)) " +
            "GROUP BY ug")
    List<Object[]> findUserGroups(@Param("columnName") String columnName,
                                        @Param("condition") String condition,
                                        @Param("filterExpr") String filterExpr,
                                        @Param("includeDeleted") boolean includeDeleted);

    default Page<Object[]> findPaginatedUserGroups(String columnName,
                                                   String condition,
                                                   String filterExpr,
                                                   boolean includeDeleted,
                                                   List<Long> groupIds,
                                                   Pageable pageable) {
        // Fetch sorted data
        List<Object[]> data = findUserGroups(columnName, condition, filterExpr, includeDeleted);
        data.sort((obj1, obj2) -> {
            UserGroup group1 = (UserGroup) obj1[0];
            UserGroup group2 = (UserGroup) obj2[0];

            // Check if group1's userGroupId is present in groupIds
            boolean group1InGroupIds = groupIds != null && !groupIds.isEmpty() && groupIds.contains(group1.getUserGroupId());
            // Check if group2's userGroupId is present in groupIds
            boolean group2InGroupIds = groupIds != null && !groupIds.isEmpty() && groupIds.contains(group2.getUserGroupId());

            // If group1 is in groupIds but group2 is not, group1 should come before group2
            if (group1InGroupIds && !group2InGroupIds) {
                return -1;
            }
            // If group2 is in groupIds but group1 is not, group2 should come before group1
            else if (!group1InGroupIds && group2InGroupIds) {
                return 1;
            }
            // If both groups are in groupIds or both are not, compare their userGroupIds
            else {
                return Long.compare(group2.getUserGroupId(), group1.getUserGroupId());
            }
        });

        // Apply pagination to the sorted data
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), data.size());
        return new PageImpl<>(data.subList(start, end), pageable, data.size());
    }


    @Query("SELECT ugm.userGroupId FROM UserGroupsUsersMap ugm WHERE ugm.userId = :userId")
    List<Long> getUserGroupIdsFromUserId(@Param("userId") long userId);
}