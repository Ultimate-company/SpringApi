package com.example.SpringApi.Repository.CarrierDatabase;

import com.example.SpringApi.DatabaseModels.CarrierDatabase.WebTemplateUserCartMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WebTemplateUserCartMappingRepository extends JpaRepository<WebTemplateUserCartMapping, Long> {
    List<WebTemplateUserCartMapping> findByUserIdAndProductId(long userId, long productId);

    @Query("select COALESCE(count(w), 0) from WebTemplateUserCartMapping w where w.productId = :productId")
    int countCartsContainingProduct(@Param("productId") long productId);

    @Query("select COALESCE(sum(w.quantity), 0) from WebTemplateUserCartMapping w " +
            "where w.userId = :userId and w.productId = :productId")
    int findTotalQuantityInUserCart(@Param("userId") long userId, @Param("productId") long productId);
}