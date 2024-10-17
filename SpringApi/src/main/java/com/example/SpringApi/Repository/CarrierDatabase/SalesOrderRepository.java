package com.example.SpringApi.Repository.CarrierDatabase;

import com.example.SpringApi.DatabaseModels.CarrierDatabase.SalesOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {
    @Query("select s,p,pi,billingAddress,shippingAddress,l,purchaseOrderAddress,salesOrderPackagingAndShipRocketMapping from SalesOrder s " +
            "join PurchaseOrder p on s.purchaseOrderId = p.purchaseOrderId " +
            "join PaymentInfo pi on s.paymentId = pi.paymentId " +
            "join Address billingAddress on s.billingAddressId = billingAddress.addressId " +
            "join Address shippingAddress on s.shippingAddressId = shippingAddress.addressId " +
            "join Lead l on p.assignedLeadId = l.leadId " +
            "join Address purchaseOrderAddress on p.purchaseOrderAddressId = purchaseOrderAddress.addressId " +
            "join SalesOrderPackagingAndShipRocketMapping salesOrderPackagingAndShipRocketMapping on salesOrderPackagingAndShipRocketMapping.salesOrderId = s.salesOrderId " +
            "where s.salesOrderStatus in :salesOrderStatus and" +
            "(:includeDeleted = true OR s.deleted = false) " +
            "and (COALESCE(:filterExpr, '') = '' OR " +
            "(CASE :columnName " +
            "WHEN 'id' THEN CONCAT(s.salesOrderId, '') " +
            "WHEN 'assignedLead' THEN CONCAT(l.firstName, ' ', l.lastName, ' ', l.email) " +
            "WHEN 'billingAddress' THEN CONCAT(billingAddress.line1, ' ', billingAddress.line2, ' ', billingAddress.city, ' ', billingAddress.state, ' ', billingAddress.zipCode) " +
            "WHEN 'shippingAddress' THEN CONCAT(shippingAddress.line1, ' ', shippingAddress.line2, ' ', shippingAddress.city, ' ', shippingAddress.state, ' ', shippingAddress.zipCode) " +
            "ELSE '' END) LIKE " +
            "(CASE :condition " +
            "WHEN 'contains' THEN CONCAT('%', :filterExpr, '%') " +
            "WHEN 'equals' THEN :filterExpr " +
            "WHEN 'startsWith' THEN CONCAT(:filterExpr, '%') " +
            "WHEN 'endsWith' THEN CONCAT('%', :filterExpr) " +
            "WHEN 'isEmpty' THEN '' " +
            "WHEN 'isNotEmpty' THEN '%' " +
            "ELSE '' END))")
    Page<Object[]> findPaginatedSalesOrder(@Param("columnName") String columnName,
                                           @Param("condition") String condition,
                                           @Param("filterExpr") String filterExpr,
                                           @Param("includeDeleted") boolean includeDeleted,
                                           @Param("salesOrderStatus") int[] salesOrderStatus,
                                           Pageable pageable);

    List<SalesOrder> findAllBySalesOrderStatus(int salesOrderStatus);
}