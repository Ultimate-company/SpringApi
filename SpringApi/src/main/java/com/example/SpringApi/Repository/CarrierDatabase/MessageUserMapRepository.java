package com.example.SpringApi.Repository.CarrierDatabase;

import com.example.SpringApi.DatabaseModels.CarrierDatabase.MessageUserMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageUserMapRepository  extends JpaRepository<MessageUserMap, Long> {
    List<MessageUserMap> findByMessageId(long messageId);
    List<MessageUserMap> findByUserId(long userId);
}
