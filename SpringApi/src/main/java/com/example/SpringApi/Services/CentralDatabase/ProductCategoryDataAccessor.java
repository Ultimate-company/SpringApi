package com.example.SpringApi.Services.CentralDatabase;

import com.example.SpringApi.Repository.CentralDatabase.CarrierRepository;
import com.example.SpringApi.Repository.CentralDatabase.ProductCategoryRepository;
import com.example.SpringApi.Services.BaseDataAccessor;
import com.example.SpringApi.SuccessMessages;
import jakarta.servlet.http.HttpServletRequest;
import org.example.CommonHelpers.HelperUtils;
import org.example.Models.CommunicationModels.CentralModels.ProductCategory;
import org.example.Models.ResponseModels.Response;
import org.example.Translators.CentralDatabaseTranslators.Interfaces.IProductCategorySubTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductCategoryDataAccessor extends BaseDataAccessor implements IProductCategorySubTranslator {
    private final ProductCategoryRepository productCategoryRepository;

    @Autowired
    public ProductCategoryDataAccessor(HttpServletRequest request,
                             CarrierRepository carrierRepository,
                             ProductCategoryRepository productCategoryRepository) {
        super(request, carrierRepository);
        this.productCategoryRepository = productCategoryRepository;
    }

    @Override
    public Response<List<ProductCategory>> getRootCategories() {
        return new Response<>(true, SuccessMessages.ProductCategorySuccessMessages.GetProductCategories,
                HelperUtils.copyFields(productCategoryRepository.findRootCategories(), ProductCategory.class));
    }

    @Override
    public Response<List<ProductCategory>> getChildCategoriesGivenParentId(long categoryId) {
        return new Response<>(true, SuccessMessages.ProductCategorySuccessMessages.GetProductCategories,
                HelperUtils.copyFields(productCategoryRepository.findChildrenCategories(categoryId), ProductCategory.class));
    }

    @Override
    public Response<ProductCategory> getCategoryByName(String name) {
        return new Response<>(true, SuccessMessages.ProductCategorySuccessMessages.GetProductCategories,
                HelperUtils.copyFields(productCategoryRepository.findProductCategoryByName(name), ProductCategory.class));
    }
}
