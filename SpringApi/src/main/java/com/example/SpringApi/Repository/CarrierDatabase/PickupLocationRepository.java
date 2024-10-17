package com.example.SpringApi.Repository.CarrierDatabase;

import com.example.SpringApi.DatabaseModels.CarrierDatabase.PickupLocation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PickupLocationRepository extends JpaRepository<PickupLocation, Long> {
    PickupLocation findByAddressNickName(String addressNickName);

    @Query("select p from PickupLocation p where p.deleted = false")
    List<PickupLocation> findAllNonDeleted();

    @Query("select p,a from PickupLocation p join Address a on a.addressId = p.pickupLocationAddressId " +
            "where (:includeDeleted = true OR p.deleted = false) " +
            "and (COALESCE(:filterExpr, '') = '' OR " +
            "(CASE :columnName " +
            "WHEN 'locationName' THEN CONCAT(p.addressNickName, '') " +
            "WHEN 'address' THEN CONCAT(a.line1, ' ', a.line2, ' ', a.city, ' ', a.state, ' ', a.zipCode) " +
            "WHEN 'phoneOnAddress' THEN CONCAT(a.phoneOnAddress, '') " +
            "WHEN 'emailAtAddress' THEN CONCAT(a.emailAtAddress, '') " +
            "ELSE '' END) LIKE " +
            "(CASE :condition " +
            "WHEN 'contains' THEN CONCAT('%', :filterExpr, '%') " +
            "WHEN 'equals' THEN :filterExpr " +
            "WHEN 'startsWith' THEN CONCAT(:filterExpr, '%') " +
            "WHEN 'endsWith' THEN CONCAT('%', :filterExpr) " +
            "WHEN 'isEmpty' THEN '' " +
            "WHEN 'isNotEmpty' THEN '%' " +
            "ELSE '' END))")
    Page<Object[]> findPaginatedPickupLocations(@Param("columnName") String columnName,
                                               @Param("condition") String condition,
                                               @Param("filterExpr") String filterExpr,
                                               @Param("includeDeleted") boolean includeDeleted,
                                               Pageable pageable);
}