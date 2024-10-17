package com.example.SpringApi.Repository.CarrierDatabase;

import com.example.SpringApi.DatabaseModels.CarrierDatabase.MessageUserGroupMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageUserGroupMapRepository  extends JpaRepository<MessageUserGroupMap, Long> {
    List<MessageUserGroupMap> findByMessageId(long messageId);

    @Query("SELECT mugm FROM MessageUserGroupMap mugm WHERE mugm.userGroupId in :userGroupIds")
    List<MessageUserGroupMap> findByUserGroupIds(@Param("userGroupIds") List<Long> userGroupIds);
}
