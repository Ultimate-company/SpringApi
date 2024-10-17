package com.example.SpringApi.Repository.CarrierDatabase;

import com.example.SpringApi.DatabaseModels.CarrierDatabase.SalesOrdersProductQuantityMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalesOrdersProductQuantityMapRepository extends JpaRepository<SalesOrdersProductQuantityMap, Long> {
    List<SalesOrdersProductQuantityMap> findBySalesOrderId(long salesOrderId);
}