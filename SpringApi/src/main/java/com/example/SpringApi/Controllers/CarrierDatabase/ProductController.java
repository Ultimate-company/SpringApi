package com.example.SpringApi.Controllers.CarrierDatabase;

import com.example.SpringApi.Services.CarrierDatabase.ProductDataAccessor;
import org.example.ApiRoutes;
import org.example.Models.Authorizations;
import org.example.Models.CommunicationModels.CarrierModels.Product;
import org.example.Models.RequestModels.ApiRequestModels.ProductRequestModel;
import org.example.Models.RequestModels.GridRequestModels.PaginationBaseRequestModel;
import org.example.Models.ResponseModels.ApiResponseModels.PaginationBaseResponseModel;
import org.example.Models.ResponseModels.ApiResponseModels.ProductsResponseModel;
import org.example.Models.ResponseModels.Response;
import org.example.Translators.CarrierDatabaseTranslators.Interfaces.IProductSubTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/" + ApiRoutes.ApiControllerNames.PRODUCT + "/")
public class ProductController {

    private final IProductSubTranslator accessor;
    @Autowired
    public ProductController(ProductDataAccessor accessor) {
        this.accessor = accessor;
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.INSERT_PRODUCTS_PERMISSION +"')")
    @PutMapping(ApiRoutes.ProductsSubRoute.ADD_PRODUCT)
    public ResponseEntity<Response<Long>> addProduct(@RequestBody Product product) {
        return ResponseEntity.ok(accessor.addProduct(product));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.UPDATE_PRODUCTS_PERMISSION +"')")
    @PostMapping(ApiRoutes.ProductsSubRoute.EDIT_PRODUCT)
    public ResponseEntity<Response<Long>> editProduct(@RequestBody Product product) {
        return ResponseEntity.ok(accessor.editProduct(product));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.DELETE_PRODUCTS_PERMISSION +"')")
    @DeleteMapping(ApiRoutes.ProductsSubRoute.TOGGLE_DELETE_PRODUCT)
    public ResponseEntity<Response<Boolean>> toggleDeleteProduct(@RequestParam long id) {
        return ResponseEntity.ok(accessor.toggleDeleteProduct(id));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.TOGGLE_PRODUCT_RETURNS_PERMISSION +"')")
    @DeleteMapping(ApiRoutes.ProductsSubRoute.TOGGLE_RETURN_PRODUCT)
    public ResponseEntity<Response<Boolean>> toggleReturnProduct(@RequestParam long id) {
        return ResponseEntity.ok(accessor.toggleReturnProduct(id));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_PRODUCTS_PERMISSION +"')")
    @GetMapping(ApiRoutes.ProductsSubRoute.GET_PRODUCT_DETAILS_BY_ID)
    public ResponseEntity<Response<ProductsResponseModel>> getProductDetailsById(@RequestParam long id) {
        return ResponseEntity.ok(accessor.getProductDetailsById(id));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_PRODUCTS_PERMISSION +"')")
    @PostMapping(ApiRoutes.ProductsSubRoute.GET_PRODUCTS_IN_BATCHES)
    public ResponseEntity<Response<PaginationBaseResponseModel<ProductsResponseModel>>> getProductInBatches(@RequestBody PaginationBaseRequestModel paginationBaseRequestModel) {
        return ResponseEntity.ok(accessor.getProductInBatches(paginationBaseRequestModel));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_PRODUCTS_PERMISSION +"')")
    @GetMapping(ApiRoutes.ProductsSubRoute.GET_PRODUCT_DETAILS_BY_IDS)
    public ResponseEntity<Response<List<ProductsResponseModel>>> getProductDetailsByIds(@RequestParam List<Long> id) {
        return ResponseEntity.ok(accessor.getProductDetailsByIds(id));
    }

    // public endpoints
    @PreAuthorize("@customAuthorization.validatePublicEndpoint()")
    @PostMapping(ApiRoutes.ProductsSubRoute.GET_PRODUCTS_IN_BATCHES_PUBLIC)
    public ResponseEntity<Response<PaginationBaseResponseModel<ProductsResponseModel>>> getProductsInBatches_Public(@RequestBody PaginationBaseRequestModel paginationBaseRequestModel) {
        return ResponseEntity.ok(accessor.getProductInBatches_Public(paginationBaseRequestModel));
    }

    @PreAuthorize("@customAuthorization.validatePublicEndpoint()")
    @GetMapping(ApiRoutes.ProductsSubRoute.GET_PRODUCT_DETAILS_BY_IDS_PUBLIC)
    public ResponseEntity<Response<List<ProductsResponseModel>>> getProductDetailsByIds_Public(@RequestParam List<Long> id) {
        return ResponseEntity.ok(accessor.getProductDetailsByIds_Public(id));
    }

    @PreAuthorize("@customAuthorization.validatePublicEndpoint()")
    @GetMapping(ApiRoutes.ProductsSubRoute.GET_PRODUCT_DETAILS_BY_ID_PUBLIC)
    public ResponseEntity<Response<ProductsResponseModel>> getProductDetailsById_Public(@RequestParam long id) {
        return ResponseEntity.ok(accessor.getProductDetailsById_Public(id));
    }
}