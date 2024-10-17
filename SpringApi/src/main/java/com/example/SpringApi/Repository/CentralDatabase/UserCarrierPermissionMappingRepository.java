package com.example.SpringApi.Repository.CentralDatabase;

import com.example.SpringApi.DatabaseModels.CentralDatabase.UserCarrierPermissionMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserCarrierPermissionMappingRepository extends JpaRepository<UserCarrierPermissionMapping, Long> {
    @Query("SELECT DISTINCT uc " +
            "FROM UserCarrierPermissionMapping uc " +
            "WHERE uc.userId = :userId")
    List<UserCarrierPermissionMapping> findCarrierPermissionMappingByUserId(@Param("userId") long userId);
}
