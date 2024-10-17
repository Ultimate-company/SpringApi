package com.example.SpringApi.Repository.CarrierDatabase;

import com.example.SpringApi.DatabaseModels.CarrierDatabase.WebTemplateUserCartMapping;
import com.example.SpringApi.DatabaseModels.CarrierDatabase.WebTemplateUserLikedItemsMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WebTemplateUserLikedItemsMappingRepository extends JpaRepository<WebTemplateUserLikedItemsMapping, Long> {
    List<WebTemplateUserLikedItemsMapping> findByUserIdAndProductId(long userId, long productId);

    @Query("select COALESCE(count(w), 0) from WebTemplateUserLikedItemsMapping w where w.productId = :productId")
    int countLikedItemsContainingProduct(@Param("productId") long productId);

    @Query("select COALESCE((count(w) > 0), false) from WebTemplateUserLikedItemsMapping w where w.userId = :userId and w.productId = :productId")
    boolean isProductInUserLikedItems(@Param("userId") long userId, @Param("productId") long productId);
}