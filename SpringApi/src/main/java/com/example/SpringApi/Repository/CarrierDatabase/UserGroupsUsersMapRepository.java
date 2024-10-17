package com.example.SpringApi.Repository.CarrierDatabase;

import com.example.SpringApi.DatabaseModels.CarrierDatabase.UserGroupsUsersMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserGroupsUsersMapRepository extends JpaRepository<UserGroupsUsersMap, Long> {
    @Query("SELECT ugm FROM UserGroupsUsersMap ugm WHERE ugm.userGroupId IN :userGroupIds and ugm.userId = :userId")
    List<UserGroupsUsersMap> findUserGroupsUsersMapByGroupIdAndUserId(@Param("userGroupIds") List<Long> userGroupIds, @Param("userId") long userId);
    List<UserGroupsUsersMap> findByUserGroupId(long userGroupId);
}
