package com.example.SpringApi.Controllers.CentralDatabase;

import com.example.SpringApi.Services.CentralDatabase.ProductCategoryDataAccessor;
import org.example.ApiRoutes;
import org.example.Models.Authorizations;
import org.example.Models.CommunicationModels.CentralModels.ProductCategory;
import org.example.Models.ResponseModels.Response;
import org.example.Translators.CentralDatabaseTranslators.Interfaces.IProductCategorySubTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/" + ApiRoutes.ApiControllerNames.PRODUCT_CATEGORY + "/")
public class ProductCategoryController {
    private final IProductCategorySubTranslator accessor;

    @Autowired
    public ProductCategoryController(ProductCategoryDataAccessor accessor){
        this.accessor = accessor;
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.INSERT_PRODUCTS_PERMISSION +"')")
    @GetMapping(ApiRoutes.ProductCategorySubRoute.GET_ROOT_CATEGORIES)
    public ResponseEntity<Response<List<ProductCategory>>> getRootCategories() {
        return ResponseEntity.ok(accessor.getRootCategories());
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.INSERT_PRODUCTS_PERMISSION +"')")
    @GetMapping(ApiRoutes.ProductCategorySubRoute.GET_CHILD_CATEGORIES_GIVEN_PARENT_ID)
    public ResponseEntity<Response<List<ProductCategory>>> getChildCategoriesGivenParentId(@RequestParam long id) {
        return ResponseEntity.ok(accessor.getChildCategoriesGivenParentId(id));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.INSERT_PRODUCTS_PERMISSION +"')")
    @GetMapping(ApiRoutes.ProductCategorySubRoute.GET_CATEGORY_BY_NAME)
    public ResponseEntity<Response<ProductCategory>> getCategoryByName(@RequestParam String name) {
        return ResponseEntity.ok(accessor.getCategoryByName(URLDecoder.decode(name, StandardCharsets.UTF_8)));
    }
}
