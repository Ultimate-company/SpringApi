package com.example.SpringApi.Repository.CarrierDatabase;

import com.example.SpringApi.DatabaseModels.CarrierDatabase.PurchaseOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    @Query("select p,l,a from PurchaseOrder p " +
            "join Lead l on p.assignedLeadId = l.leadId " +
            "join Address a on p.purchaseOrderAddressId = a.addressId " +
            "where (:includeDeleted = true OR p.deleted = false) " +
            "and (COALESCE(:filterExpr, '') = '' OR " +
            "(CASE :columnName " +
            "WHEN 'id' THEN CONCAT(p.purchaseOrderId, '') " +
            "WHEN 'address' THEN CONCAT(a.line1, ' ', a.line2, ' ', a.city, ' ', a.state, ' ', a.zipCode) " +
            "WHEN 'expectedShipmentDate' THEN CONCAT(p.expectedShipmentDate, '') " +
            "WHEN 'vendorNumber' THEN CONCAT(p.vendorNumber, '') " +
            "WHEN 'orderReceipt' THEN CONCAT(p.orderReceipt, '') " +
            "WHEN 'assignedLead' THEN CONCAT(l.firstName, ' ', l.lastName, ' ', l.email) " +
            "ELSE '' END) LIKE " +
            "(CASE :condition " +
            "WHEN 'contains' THEN CONCAT('%', :filterExpr, '%') " +
            "WHEN 'equals' THEN :filterExpr " +
            "WHEN 'startsWith' THEN CONCAT(:filterExpr, '%') " +
            "WHEN 'endsWith' THEN CONCAT('%', :filterExpr) " +
            "WHEN 'isEmpty' THEN '' " +
            "WHEN 'isNotEmpty' THEN '%' " +
            "ELSE '' END))")
    Page<Object[]> findPaginatedPurchaseOrder(@Param("columnName") String columnName,
                                             @Param("condition") String condition,
                                             @Param("filterExpr") String filterExpr,
                                             @Param("includeDeleted") boolean includeDeleted,
                                             Pageable pageable);
}