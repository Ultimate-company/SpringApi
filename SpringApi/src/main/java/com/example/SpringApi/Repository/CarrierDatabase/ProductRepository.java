package com.example.SpringApi.Repository.CarrierDatabase;

import com.example.SpringApi.DatabaseModels.CarrierDatabase.PickupLocation;
import com.example.SpringApi.DatabaseModels.CarrierDatabase.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>{
    @Query("select p from Product p " +
            "where (:includeDeleted = true OR p.deleted = false) " +
            "and (:filteredProductIds IS NULL OR p.productId IN :filteredProductIds) " +
            "and (COALESCE(:filterExpr, '') = '' OR " +
            "(CASE :columnName " +
            "WHEN 'dimensions' THEN CONCAT(p.length, ' ', p.breadth, ' ', p.height) " +
            "WHEN 'title' THEN CONCAT(p.title, '') " +
            "WHEN 'type' THEN CONCAT(p.availableStock, '') " +
            "WHEN 'upc' THEN CONCAT(p.upc, '') " +
            "WHEN 'price' THEN CONCAT(p.price, '') " +
            "WHEN 'discount' THEN CONCAT(p.discount, '') " +
            "WHEN 'availableStock' THEN CONCAT(p.availableStock, '') " +
            "ELSE '' END) LIKE " +
            "(CASE :condition " +
            "WHEN 'contains' THEN CONCAT('%', :filterExpr, '%') " +
            "WHEN 'equals' THEN :filterExpr " +
            "WHEN 'startsWith' THEN CONCAT(:filterExpr, '%') " +
            "WHEN 'endsWith' THEN CONCAT('%', :filterExpr) " +
            "WHEN 'isEmpty' THEN '' " +
            "WHEN 'isNotEmpty' THEN '%' " +
            "ELSE '' END))")
    Page<Product> findPaginatedProducts(
            @Param("columnName") String columnName,
            @Param("condition") String condition,
            @Param("filterExpr") String filterExpr,
            @Param("includeDeleted") boolean includeDeleted,
            @Param("filteredProductIds") Set<Long> filteredProductIds,
            Pageable pageable);

    @Query("select p, pl, a from Product p " +
            "join PickupLocation pl on p.pickupLocationId = pl.pickupLocationId " +
            "join Address a on pl.pickupLocationAddressId = a.addressId")
    List<Object[]> getProductPickupLocationAddressModels (@Param("productIds") List<Long> productIds);
}