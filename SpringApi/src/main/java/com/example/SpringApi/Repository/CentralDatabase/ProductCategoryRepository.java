package com.example.SpringApi.Repository.CentralDatabase;

import com.example.SpringApi.DatabaseModels.CentralDatabase.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {
    @Query("Select p from ProductCategory p where p.parentId = :parentId")
    List<ProductCategory> findChildrenCategories(@Param("parentId") Long parentId);

    @Query("select p from ProductCategory p where p.parentId is null")
    List<ProductCategory> findRootCategories();

    ProductCategory findProductCategoryByName(String name);
}
