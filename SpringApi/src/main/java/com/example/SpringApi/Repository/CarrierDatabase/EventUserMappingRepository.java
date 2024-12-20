package com.example.SpringApi.Repository.CarrierDatabase;

import com.example.SpringApi.DatabaseModels.CarrierDatabase.EventUserMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventUserMappingRepository extends JpaRepository<EventUserMapping, Long> {
    List<EventUserMapping> findEventUserMappingByUserId(long userId);

    @Query("SELECT eum FROM EventUserMapping eum WHERE eum.eventId in :eventIds")
    List<EventUserMapping> findEventUserMappingByEventIds(List<Long> eventIds);

    EventUserMapping findEventUserMappingByEventIdAndUserId(long eventId, long userId);
}