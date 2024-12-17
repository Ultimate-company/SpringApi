package com.example.SpringApi.Repository.CarrierDatabase;

import com.example.SpringApi.DatabaseModels.CarrierDatabase.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    @Query("SELECT e FROM Event e " +
            "WHERE e.eventId IN :eventIds " +
            "AND FUNCTION('MONTH', e.startDateTime) = :month")
    List<Event> findEventsByIdsAndMonth(@Param("eventIds") List<Long> eventIds, @Param("month") int month);
}