package com.example.SpringApi.Services.CarrierDatabase;

import com.example.SpringApi.DatabaseModels.CarrierDatabase.Address;
import com.example.SpringApi.DatabaseModels.CarrierDatabase.PickupLocation;
import com.example.SpringApi.DatabaseModels.CarrierDatabase.WebTemplate;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Repository.CarrierDatabase.*;
import com.example.SpringApi.Repository.CentralDatabase.CarrierRepository;
import com.example.SpringApi.Repository.CentralDatabase.ProductCategoryRepository;
import com.example.SpringApi.Services.BaseDataAccessor;
import com.example.SpringApi.Services.CentralDatabase.UserLogDataAccessor;
import com.example.SpringApi.SuccessMessages;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.example.ApiRoutes;
import org.example.CommonHelpers.HelperUtils;
import com.example.SpringApi.DatabaseModels.CarrierDatabase.Product;
import com.example.SpringApi.DatabaseModels.CentralDatabase.ProductCategory;
import org.example.CommonHelpers.ImageHelper;
import org.example.CommonHelpers.Validations;
import org.example.Models.RequestModels.ApiRequestModels.ProductRequestModel;
import org.example.Models.RequestModels.GridRequestModels.PaginationBaseRequestModel;
import org.example.Models.ResponseModels.ApiResponseModels.PaginationBaseResponseModel;
import org.example.Models.ResponseModels.ApiResponseModels.PickupLocationResponseModel;
import org.example.Models.ResponseModels.ApiResponseModels.ProductsResponseModel;
import org.example.Models.ResponseModels.Response;
import org.example.Translators.CarrierDatabaseTranslators.Interfaces.IProductSubTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductDataAccessor extends BaseDataAccessor implements IProductSubTranslator {
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;
    private final PickupLocationRepository pickupLocationRepository;
    private final ProductReviewRepository productReviewRepository;
    private final WebTemplatesRepository webTemplatesRepository;
    private final WebTemplateUserCartMappingRepository webTemplateUserCartMappingRepository;
    private final WebTemplateUserLikedItemsMappingRepository webTemplateUserLikedItemsMappingRepository;
    private final UserLogDataAccessor userLogDataAccessor;

    @Autowired
    public ProductDataAccessor(HttpServletRequest request,
                               CarrierRepository carrierRepository,
                               PickupLocationRepository pickupLocationRepository,
                               AddressRepository addressRepository,
                               ProductCategoryRepository productCategoryRepository,
                               ProductRepository productRepository,
                               ProductReviewRepository productReviewRepository,
                               WebTemplatesRepository webTemplatesRepository,
                               WebTemplateUserCartMappingRepository webTemplateUserCartMappingRepository,
                               WebTemplateUserLikedItemsMappingRepository webTemplateUserLikedItemsMappingRepository,
                               UserLogDataAccessor userLogDataAccessor) {
        super(request, carrierRepository);
        this.productCategoryRepository = productCategoryRepository;
        this.pickupLocationRepository = pickupLocationRepository;
        this.addressRepository = addressRepository;
        this.productRepository = productRepository;
        this.productReviewRepository = productReviewRepository;
        this.webTemplatesRepository = webTemplatesRepository;
        this.webTemplateUserCartMappingRepository = webTemplateUserCartMappingRepository;
        this.webTemplateUserLikedItemsMappingRepository = webTemplateUserLikedItemsMappingRepository;
        this.userLogDataAccessor = userLogDataAccessor;
    }

    private Pair<String, Boolean> validateProduct(org.example.Models.CommunicationModels.CarrierModels.Product product) {
        // Check mandatory fields
        if (product.getTitle() == null || !StringUtils.hasText(product.getTitle())) {
            return Pair.of(ErrorMessages.ProductErrorMessages.ER001, false);
        }
        if (product.getDescriptionHtml() == null || !StringUtils.hasText(product.getDescriptionHtml())) {
            return Pair.of(ErrorMessages.ProductErrorMessages.ER002, false);
        }
        if (product.getBrand() == null || !StringUtils.hasText(product.getBrand())) {
            return Pair.of(ErrorMessages.ProductErrorMessages.ER003, false);
        }
        if (product.getCountryOfManufacture() == null || !StringUtils.hasText(product.getCountryOfManufacture())) {
            return Pair.of(ErrorMessages.ProductErrorMessages.ER004, false);
        }
        if (product.getCategoryId() == 0L) {
            return Pair.of(ErrorMessages.ProductErrorMessages.ER007, false);
        }

        // validate messages
        if (product.getMainImage() == null
                || !StringUtils.hasText(product.getMainImage())

                || product.getTopImage() == null
                || !StringUtils.hasText(product.getTopImage())

                || product.getBottomImage() == null
                || !StringUtils.hasText(product.getBottomImage())

                || product.getFrontImage() == null
                || !StringUtils.hasText(product.getFrontImage())

                || product.getBackImage() == null
                || !StringUtils.hasText(product.getBackImage())

                || product.getRightImage() == null
                || !StringUtils.hasText(product.getRightImage())

                || product.getLeftImage() == null
                || !StringUtils.hasText(product.getLeftImage())

                || product.getDetailsImage() == null
                || !StringUtils.hasText(product.getDetailsImage())) {
            return Pair.of(ErrorMessages.ProductErrorMessages.ER005, false);
        }

        if (product.getItemAvailableFrom() == null || product.getItemAvailableFrom().isBefore(LocalDateTime.now())) {
            return Pair.of(ErrorMessages.ProductErrorMessages.ER006, false);
        }

        return Pair.of("Success", true);
    }

    private void fillInAdditionalInformationForProduct(ProductsResponseModel productsResponseModel, long productId, Long userId) {
        productsResponseModel.setTotalProductReviews(productReviewRepository.countTotalReviewsForProduct(productId));
        productsResponseModel.setProductRating(productReviewRepository.calculateAverageRatingForProduct(productId));
        productsResponseModel.setNumberOfCarts(webTemplateUserCartMappingRepository.countCartsContainingProduct(productId));
        productsResponseModel.setNumberOfLikedItems(webTemplateUserLikedItemsMappingRepository.countLikedItemsContainingProduct(productId));

        if(userId != null) {
            productsResponseModel.setQuantityInCurrentUsersCart(webTemplateUserCartMappingRepository.findTotalQuantityInUserCart(userId, productId));
            productsResponseModel.setProductInLikedItemsForCurrentUser(webTemplateUserLikedItemsMappingRepository.isProductInUserLikedItems(userId, productId));
        }
    }

    private Response<PaginationBaseResponseModel<ProductsResponseModel>> getProductInBatchesInternal(PaginationBaseRequestModel paginationBaseRequestModel, Set<Long> filteredProductIds) {
        // Validate the column names
        if (StringUtils.hasText(paginationBaseRequestModel.getColumnName())) {
            Set<String> validColumns = new HashSet<>(Arrays.asList("title", "type", "upc", "price", "discount", "availableStock", "dimensions"));

            if (!validColumns.contains(paginationBaseRequestModel.getColumnName())) {
                return new Response<>(false, ErrorMessages.InvalidColumn + String.join(",", validColumns), null);
            }
        }

        // Fetch the products, with or without filtering by product IDs
        Page<Product> products = productRepository.findPaginatedProducts(
                paginationBaseRequestModel.getColumnName(),
                paginationBaseRequestModel.getCondition(),
                paginationBaseRequestModel.getFilterExpr(),
                paginationBaseRequestModel.isIncludeDeleted(),
                filteredProductIds,
                PageRequest.of(paginationBaseRequestModel.getStart() / (paginationBaseRequestModel.getEnd() - paginationBaseRequestModel.getStart()),
                        paginationBaseRequestModel.getEnd() - paginationBaseRequestModel.getStart(),
                        Sort.by("productId").descending()));

        // Map the products to the response model
        List<ProductsResponseModel> productsResponseModels = new ArrayList<>();
        for (Product product : products.getContent()) {
            ProductsResponseModel productsResponseModel = new ProductsResponseModel();
            productsResponseModel.setProduct(HelperUtils.copyFields(product, org.example.Models.CommunicationModels.CarrierModels.Product.class));

            productCategoryRepository.findById(product.getCategoryId())
                    .ifPresent(productCategory ->
                            productsResponseModel.setProductCategory(HelperUtils.copyFields(productCategory, org.example.Models.CommunicationModels.CentralModels.ProductCategory.class))
                    );

            fillInAdditionalInformationForProduct(productsResponseModel, product.getProductId(), getUserId());
            productsResponseModels.add(productsResponseModel);
        }

        PaginationBaseResponseModel<ProductsResponseModel> paginationBaseResponseModel = new PaginationBaseResponseModel<>();
        paginationBaseResponseModel.setData(productsResponseModels);
        paginationBaseResponseModel.setTotalDataCount(products.getTotalElements());

        return new Response<>(true, SuccessMessages.ProductsSuccessMessages.GetProduct, paginationBaseResponseModel);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<Long> addProduct(org.example.Models.CommunicationModels.CarrierModels.Product product) {
        Pair<String, Boolean> validation = validateProduct(product);
        if(!validation.getValue()){
            return new Response<>(false, validation.getKey(), null);
        }

        Product savedProduct = productRepository.save(HelperUtils.copyFields(product, Product.class));
        userLogDataAccessor.logData(getUserId(),
                SuccessMessages.ProductsSuccessMessages.InsertProduct + " " + savedProduct.getProductId(),
                ApiRoutes.ProductsSubRoute.ADD_PRODUCT);
        return new Response<>(true, SuccessMessages.ProductsSuccessMessages.InsertProduct, savedProduct.getProductId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<Long> editProduct(org.example.Models.CommunicationModels.CarrierModels.Product product) {
        Optional<Product> existingProduct = productRepository.findById(product.getProductId());
        if(existingProduct.isEmpty()){
            return new Response<>(false, ErrorMessages.ProductErrorMessages.InvalidId, null);
        }

        Pair<String, Boolean> validation = validateProduct(product);
        if(!validation.getValue()){
            return new Response<>(false, validation.getKey(), null);
        }

        Product savedProduct = productRepository.save(HelperUtils.copyFields(product, Product.class));
        userLogDataAccessor.logData(getUserId(),
                SuccessMessages.ProductsSuccessMessages.UpdateProduct + " " + savedProduct.getProductId(),
                ApiRoutes.ProductsSubRoute.EDIT_PRODUCT);
        return new Response<>(true, SuccessMessages.ProductsSuccessMessages.UpdateProduct, savedProduct.getProductId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<Boolean> toggleDeleteProduct(long productId) {
        Optional<Product> existingProduct = productRepository.findById(productId);
        if(existingProduct.isEmpty()){
            return new Response<>(false, ErrorMessages.ProductErrorMessages.InvalidId, false);
        }

        existingProduct.get().setDeleted(!existingProduct.get().isDeleted());
        productRepository.save(existingProduct.get());
        userLogDataAccessor.logData(getUserId(),
                SuccessMessages.ProductsSuccessMessages.ToggleProduct + " " + existingProduct.get().getProductId(),
                ApiRoutes.ProductsSubRoute.TOGGLE_DELETE_PRODUCT);

        return new Response<>(true, SuccessMessages.ProductsSuccessMessages.ToggleProduct, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<Boolean> toggleReturnProduct(long productId) {
        Optional<Product> existingProduct = productRepository.findById(productId);
        if(existingProduct.isEmpty()){
            return new Response<>(false, ErrorMessages.ProductErrorMessages.InvalidId, false);
        }

        existingProduct.get().setReturnsAllowed(!existingProduct.get().isReturnsAllowed());
        productRepository.save(existingProduct.get());
        userLogDataAccessor.logData(getUserId(),
                SuccessMessages.ProductsSuccessMessages.ToggleReturnProduct + " " + existingProduct.get().getProductId(),
                ApiRoutes.ProductsSubRoute.TOGGLE_RETURN_PRODUCT);

        return new Response<>(true, SuccessMessages.ProductsSuccessMessages.ToggleReturnProduct, true);
    }

    @Override
    public Response<List<ProductsResponseModel>> getProductDetailsByIds(List<Long> productIds) {
        // Remove all the duplicate productIds
        productIds = productIds.stream()
                .distinct()
                .toList();

        List<Product> products = productRepository.findAllById(productIds);
        List<ProductsResponseModel> productsResponseModels = new ArrayList<>();
        for(Product product : products) {
            Optional<ProductCategory> productCategory = productCategoryRepository.findById(product.getCategoryId());
            if(productCategory.isPresent()) {
                Optional<PickupLocation> pickupLocation = pickupLocationRepository.findById(product.getPickupLocationId());
                if(pickupLocation.isPresent()) {
                    Optional<Address> address = addressRepository.findById(pickupLocation.get().getPickupLocationAddressId());
                    if(address.isPresent()) {
                        ProductsResponseModel productsResponseModel = new ProductsResponseModel();
                        PickupLocationResponseModel pickupLocationResponseModel = new PickupLocationResponseModel();

                        productsResponseModel.setProductCategory(HelperUtils.copyFields(productCategory.get(), org.example.Models.CommunicationModels.CentralModels.ProductCategory.class));
                        productsResponseModel.setProduct(HelperUtils.copyFields(product, org.example.Models.CommunicationModels.CarrierModels.Product.class));
                        pickupLocationResponseModel.setPickupLocation(HelperUtils.copyFields(pickupLocation.get(), org.example.Models.CommunicationModels.CarrierModels.PickupLocation.class));
                        pickupLocationResponseModel.setAddress(HelperUtils.copyFields(address.get(), org.example.Models.CommunicationModels.CarrierModels.Address.class));

                        productsResponseModel.setPickupLocationResponseModel(pickupLocationResponseModel);
                        fillInAdditionalInformationForProduct(productsResponseModel, product.getProductId(), getUserId());
                        productsResponseModels.add(productsResponseModel);
                    }
                }
                else{
                    return new Response<>(false, ErrorMessages.PickupLocationErrorMessages.InvalidId, null);
                }
            }
            else {
                return new Response<>(false, ErrorMessages.ProductCategoryErrorMessages.InvalidId, null);
            }
        }

        return new Response<>(true, SuccessMessages.ProductsSuccessMessages.GetProduct, productsResponseModels);
    }

    @Override
    public Response<ProductsResponseModel> getProductDetailsById(long productId) {
        Optional<Product> existingProduct = productRepository.findById(productId);
        if(existingProduct.isPresent()){
            Optional<ProductCategory> productCategory = productCategoryRepository.findById(existingProduct.get().getCategoryId());
            if(productCategory.isPresent()){
                Optional<PickupLocation> pickupLocation = pickupLocationRepository.findById(existingProduct.get().getPickupLocationId());
                if(pickupLocation.isPresent()) {
                    Optional<Address> address = addressRepository.findById(pickupLocation.get().getPickupLocationAddressId());
                    if(address.isPresent()) {
                        ProductsResponseModel productsResponseModel = new ProductsResponseModel();
                        PickupLocationResponseModel pickupLocationResponseModel = new PickupLocationResponseModel();

                        productsResponseModel.setProductCategory(HelperUtils.copyFields(productCategory.get(), org.example.Models.CommunicationModels.CentralModels.ProductCategory.class));
                        productsResponseModel.setProduct(HelperUtils.copyFields(existingProduct.get(), org.example.Models.CommunicationModels.CarrierModels.Product.class));
                        pickupLocationResponseModel.setPickupLocation(HelperUtils.copyFields(pickupLocation.get(), org.example.Models.CommunicationModels.CarrierModels.PickupLocation.class));
                        pickupLocationResponseModel.setAddress(HelperUtils.copyFields(address.get(), org.example.Models.CommunicationModels.CarrierModels.Address.class));

                        productsResponseModel.setPickupLocationResponseModel(pickupLocationResponseModel);

                        fillInAdditionalInformationForProduct(productsResponseModel, existingProduct.get().getProductId(), getUserId());
                        return new Response<>(true, SuccessMessages.ProductsSuccessMessages.GetProduct, productsResponseModel);
                    }
                    else {
                        return new Response<>(false, ErrorMessages.AddressErrorMessages.InvalidId, null);
                    }
                }
                else {
                    return new Response<>(false, ErrorMessages.PickupLocationErrorMessages.InvalidId, null);
                }
            }
            else {
                return new Response<>(false, ErrorMessages.ProductCategoryErrorMessages.InvalidId, null);
            }
        }
        else {
            return new Response<>(false, ErrorMessages.ProductErrorMessages.InvalidId, null);
        }
    }

    @Override
    public Response<PaginationBaseResponseModel<ProductsResponseModel>> getProductInBatches(PaginationBaseRequestModel paginationBaseRequestModel) {
        return getProductInBatchesInternal(paginationBaseRequestModel, null);
    }

    // public endpoints
    @Override
    public Response<PaginationBaseResponseModel<ProductsResponseModel>> getProductInBatches_Public(PaginationBaseRequestModel paginationBaseRequestModel) {
        // filter products only allowed to be displayed on this website
        Optional<WebTemplate> webTemplate = webTemplatesRepository.findById(getWebTemplateId());
        return webTemplate.map(template -> getProductInBatchesInternal(paginationBaseRequestModel, Arrays.stream(template.getSelectedProducts().split(","))
                .map(String::trim)
                .map(Long::parseLong)
                .collect(Collectors.toSet()))).orElseGet(() -> new Response<>(false, ErrorMessages.WebTemplatesErrorMessages.InvalidId, null));
    }

    @Override
    public Response<List<ProductsResponseModel>> getProductDetailsByIds_Public(List<Long> productIds) {
        return getProductDetailsByIds(productIds);
    }

    @Override
    public Response<ProductsResponseModel> getProductDetailsById_Public(long productId) {
        return getProductDetailsById(productId);
    }
}