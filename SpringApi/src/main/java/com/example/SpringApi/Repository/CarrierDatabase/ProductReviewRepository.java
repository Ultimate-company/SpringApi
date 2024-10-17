package com.example.SpringApi.Repository.CarrierDatabase;

import com.example.SpringApi.DatabaseModels.CarrierDatabase.ProductReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {
    @Query("select p from ProductReview p " +
            "where p.isDeleted = false and p.productId = :productId and p.parentId is null " +
            "order by p.reviewId desc")
    Page<ProductReview> findPaginatedProductReviewByProductId(@Param("productId") long productId, Pageable pageable);

    List<ProductReview> findByParentId(long parentId);

    @Query("select COALESCE(count(p), 0) from ProductReview p " +
            "where p.isDeleted = false and p.productId = :productId")
    int countTotalReviewsForProduct(@Param("productId") long productId);

    @Query("select COALESCE(avg(p.score), 0) from ProductReview p " +
            "where p.isDeleted = false and p.productId = :productId")
    double calculateAverageRatingForProduct(@Param("productId") long productId);
}
