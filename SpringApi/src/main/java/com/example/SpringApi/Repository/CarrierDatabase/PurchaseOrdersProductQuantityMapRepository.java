package com.example.SpringApi.Repository.CarrierDatabase;

import com.example.SpringApi.DatabaseModels.CarrierDatabase.PurchaseOrdersProductQuantityMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseOrdersProductQuantityMapRepository extends JpaRepository<PurchaseOrdersProductQuantityMap, Long> {
    List<PurchaseOrdersProductQuantityMap> findByPurchaseOrderId(long purchaseOrderId);
}
