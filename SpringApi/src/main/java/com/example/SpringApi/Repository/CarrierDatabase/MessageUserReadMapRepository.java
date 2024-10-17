package com.example.SpringApi.Repository.CarrierDatabase;

import com.example.SpringApi.DatabaseModels.CarrierDatabase.MessageUserReadMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageUserReadMapRepository  extends JpaRepository<MessageUserReadMap, Long> {
    @Query("select murm from MessageUserReadMap murm where murm.userId = :userId and murm.messageId = :messageId")
    MessageUserReadMap findMessageUserReadMapByUserIdAndMessageId(@Param("userId") long userId, @Param("messageId") long messageId);
}
