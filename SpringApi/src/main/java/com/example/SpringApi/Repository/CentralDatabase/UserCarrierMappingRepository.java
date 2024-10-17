package com.example.SpringApi.Repository.CentralDatabase;

import com.example.SpringApi.DatabaseModels.CentralDatabase.UserCarrierMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCarrierMappingRepository extends JpaRepository<UserCarrierMapping, Long> {
    UserCarrierMapping findByUserIdAndCarrierId(Long userId, Long carrierId);
}

