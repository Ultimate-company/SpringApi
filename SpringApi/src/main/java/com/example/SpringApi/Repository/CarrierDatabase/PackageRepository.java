package com.example.SpringApi.Repository.CarrierDatabase;

import com.example.SpringApi.DatabaseModels.CarrierDatabase.Package;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackageRepository extends JpaRepository<Package, Long> {
    @Query("select p from Package p " +
            "where p.length = :length and " +
            "p.breadth = :breadth and " +
            "p.height = :height")
    List<Package> findByDimensions(@Param("length") int length,
                             @Param("breadth") int breadth,
                             @Param("height") int height);

    List<Package> findByDeleted(boolean isDeleted);

    @Query("select p from Package p " +
            "where (:includeDeleted = true OR p.deleted = false) " +
            "and (COALESCE(:filterExpr, '') = '' OR " +
            "(CASE :columnName " +
            "WHEN 'dimensions' THEN CONCAT(p.length, ' ', p.breadth, ' ', p.height) " +
            "WHEN 'quantity' THEN CONCAT(p.quantity, '') " +
            "ELSE '' END) LIKE " +
            "(CASE :condition " +
            "WHEN 'contains' THEN CONCAT('%', :filterExpr, '%') " +
            "WHEN 'equals' THEN :filterExpr " +
            "WHEN 'startsWith' THEN CONCAT(:filterExpr, '%') " +
            "WHEN 'endsWith' THEN CONCAT('%', :filterExpr) " +
            "WHEN 'isEmpty' THEN '' " +
            "WHEN 'isNotEmpty' THEN '%' " +
            "ELSE '' END))")
    Page<Object[]> findPaginatedPackages(@Param("columnName") String columnName,
                                                @Param("condition") String condition,
                                                @Param("filterExpr") String filterExpr,
                                                @Param("includeDeleted") boolean includeDeleted,
                                                Pageable pageable);
}
