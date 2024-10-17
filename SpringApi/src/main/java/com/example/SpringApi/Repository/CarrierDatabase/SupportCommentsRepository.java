package com.example.SpringApi.Repository.CarrierDatabase;

import com.example.SpringApi.DatabaseModels.CarrierDatabase.SupportComments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportCommentsRepository extends JpaRepository<SupportComments, String> {
    List<SupportComments> findByTicketId(String ticketId);
}
