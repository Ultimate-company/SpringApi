package com.example.SpringApi.Repository.CarrierDatabase;

import com.example.SpringApi.DatabaseModels.CarrierDatabase.Promo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PromoRepository extends JpaRepository<Promo, Long> {
    Promo findPromoByPromoCode(String promoCode);

    @Query("select p from Promo p " +
            "where (:includeDeleted = true OR p.deleted = false) " +
            "and (COALESCE(:filterExpr, '') = '' OR " +
            "(CASE :columnName " +
            "WHEN 'promoCode' THEN CONCAT(p.promoCode, '') " +
            "WHEN 'description' THEN CONCAT(p.description, '') " +
            "WHEN 'discountValue' THEN CONCAT(p.discountValue, '') " +
            "ELSE '' END) LIKE " +
            "(CASE :condition " +
            "WHEN 'contains' THEN CONCAT('%', :filterExpr, '%') " +
            "WHEN 'equals' THEN :filterExpr " +
            "WHEN 'startsWith' THEN CONCAT(:filterExpr, '%') " +
            "WHEN 'endsWith' THEN CONCAT('%', :filterExpr) " +
            "WHEN 'isEmpty' THEN '' " +
            "WHEN 'isNotEmpty' THEN '%' " +
            "ELSE '' END)) ")
    Page<Promo> findPaginatedPromos(@Param("columnName") String columnName,
                                            @Param("condition") String condition,
                                            @Param("filterExpr") String filterExpr,
                                            @Param("includeDeleted") boolean includeDeleted,
                                            Pageable pageable);
}