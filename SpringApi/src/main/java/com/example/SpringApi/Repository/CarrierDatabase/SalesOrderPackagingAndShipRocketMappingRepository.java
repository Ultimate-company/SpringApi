package com.example.SpringApi.Repository.CarrierDatabase;

import com.example.SpringApi.DatabaseModels.CarrierDatabase.SalesOrderPackagingAndShipRocketMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalesOrderPackagingAndShipRocketMappingRepository extends JpaRepository<SalesOrderPackagingAndShipRocketMapping, Long> {
    List<SalesOrderPackagingAndShipRocketMapping> findAllBySalesOrderId(long salesOrderId);
}
