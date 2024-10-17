package com.example.SpringApi.Repository.CarrierDatabase;

import com.example.SpringApi.DatabaseModels.CarrierDatabase.Lead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {
    Lead findByEmail(String email);

    @Query("select l,a from Lead l join Address a on l.addressId = a.addressId " +
            "where (:includeDeleted = true OR l.deleted = false) " +
            "and (COALESCE(:filterExpr, '') = '' OR " +
            "(CASE :columnName " +
            "WHEN 'firstName' THEN CONCAT(l.firstName, '') " +
            "WHEN 'lastName' THEN CONCAT(l.lastName, '') " +
            "WHEN 'email' THEN CONCAT(l.email, '') " +
            "WHEN 'address' THEN CONCAT(a.line1, ' ', a.line2, ' ', a.city, ' ', a.state, ' ', a.zipCode) " +
            "WHEN 'website' THEN CONCAT(l.website, '') " +
            "WHEN 'phone' THEN CONCAT(l.phone, '') " +
            "WHEN 'companySize' THEN CONCAT(l.companySize, '') " +
            "WHEN 'title' THEN CONCAT(l.title, '') " +
            "WHEN 'leadStatus' THEN CONCAT(l.leadStatus, '') " +
            "ELSE '' END) LIKE " +
            "(CASE :condition " +
            "WHEN 'contains' THEN CONCAT('%', :filterExpr, '%') " +
            "WHEN 'equals' THEN :filterExpr " +
            "WHEN 'startsWith' THEN CONCAT(:filterExpr, '%') " +
            "WHEN 'endsWith' THEN CONCAT('%', :filterExpr) " +
            "WHEN 'isEmpty' THEN '' " +
            "WHEN 'isNotEmpty' THEN '%' " +
            "ELSE '' END))")
    Page<Object[]> findPaginatedLeads(
            @Param("columnName") String columnName,
            @Param("condition") String condition,
            @Param("filterExpr") String filterExpr,
            @Param("includeDeleted") boolean includeDeleted,
            Pageable pageable);
}
