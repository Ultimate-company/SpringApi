package com.example.SpringApi.Repository.CentralDatabase;

import com.example.SpringApi.DatabaseModels.CentralDatabase.UserCarrierMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserCarrierMappingRepository extends JpaRepository<UserCarrierMapping, Long> {

    @Query("SELECT u FROM UserCarrierMapping u WHERE u.userId IN :userIds AND u.carrierId = :carrierId")
    List<UserCarrierMapping> findByUserIdsAndCarrierId(@Param("userIds") List<Long> userIds,
                                                       @Param("carrierId") Long carrierId);
}


