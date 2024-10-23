package com.example.SpringApi.Repository.CentralDatabase;

import com.example.SpringApi.DatabaseModels.CentralDatabase.Carrier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarrierRepository extends JpaRepository<Carrier, Long> {
    @Query(value = "SELECT c FROM Carrier c JOIN UserCarrierMapping ucm " +
            "ON c.carrierId = ucm.carrierId " +
            "WHERE c.isDeleted = false " +
            "AND ucm.userId = :userId " +
            "AND (:filteredText IS NULL OR :filteredText = '' OR TRIM(:filteredText) = '' OR c.name LIKE CONCAT('%', :filteredText, '%'))")
    Page<Carrier> findByUserIdAndNameContains(@Param("userId") Long userId,
                                              @Param("filteredText") String filteredText,
                                              Pageable pageable);

    @Query(value = "SELECT COUNT(c) FROM Carrier c JOIN UserCarrierMapping ucm  On c.carrierId = ucm.carrierId WHERE c.isDeleted = false AND ucm.userId = :userId and c.name LIKE CONCAT('%', :filteredText, '%')")
    long countByUserIdAndNameContains(@Param("userId") Long userId, @Param("filteredText") String filteredText);
}
