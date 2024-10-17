package com.example.SpringApi.Controllers.CarrierDatabase;

import com.example.SpringApi.Services.CarrierDatabase.ProductReviewDataAccessor;
import org.example.ApiRoutes;
import org.example.Models.CommunicationModels.CarrierModels.ProductReview;
import org.example.Models.RequestModels.GridRequestModels.PaginationBaseRequestModel;
import org.example.Models.ResponseModels.ApiResponseModels.ProductReviewResponseModel;
import org.example.Models.ResponseModels.Response;
import org.example.Translators.CarrierDatabaseTranslators.Interfaces.IProductReviewSubTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/" + ApiRoutes.ApiControllerNames.PRODUCT_REVIEW + "/")
public class ProductReviewController {

    private final IProductReviewSubTranslator accessor;

    @Autowired
    public ProductReviewController(ProductReviewDataAccessor accessor) {
        this.accessor = accessor;
    }

    @PreAuthorize("@customAuthorization.validateToken()")
    @PutMapping(ApiRoutes.ProductReviewSubRoute.INSERT_PRODUCT_REVIEW)
    public ResponseEntity<Response<Long>> insertProductReview(@RequestBody ProductReview productReview) {
        return ResponseEntity.ok(accessor.insertProductReview(productReview));
    }

    @PreAuthorize("@customAuthorization.validateToken()")
    @PostMapping(ApiRoutes.ProductReviewSubRoute.GET_PRODUCT_REVIEWS_GIVEN_PRODUCT_ID)
    public ResponseEntity<Response<ProductReviewResponseModel>> getProductReviewsGivenProductId(@RequestBody PaginationBaseRequestModel paginationBaseRequestModel, @RequestParam long id) {
        return ResponseEntity.ok(accessor.getProductReviewsGivenProductId(paginationBaseRequestModel, id));
    }

    @PreAuthorize("@customAuthorization.validateToken()")
    @DeleteMapping(ApiRoutes.ProductReviewSubRoute.TOGGLE_PRODUCT_REVIEW)
    public ResponseEntity<Response<Boolean>> toggleProductReview(@RequestParam long id) {
        return ResponseEntity.ok(accessor.deleteReview(id));
    }

    @PreAuthorize("@customAuthorization.validateToken()")
    @PostMapping(ApiRoutes.ProductReviewSubRoute.TOGGLE_PRODUCT_REVIEW_SCORE)
    public ResponseEntity<Response<Boolean>> toggleProductReviewScore(@RequestParam long id, @RequestParam boolean increaseScore) {
        return ResponseEntity.ok(accessor.toggleProductReviewScore(id, increaseScore));
    }

    @PreAuthorize("@customAuthorization.validateToken()")
    @GetMapping(ApiRoutes.ProductReviewSubRoute.GET_PRODUCT_REVIEW_BY_ID)
    public ResponseEntity<Response<ProductReviewResponseModel>> getProductReviewById(@RequestParam long id) {
        return ResponseEntity.ok(accessor.getProductReviewById(id));
    }
}