package com.example.SpringApi.Services.CarrierDatabase;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Repository.CarrierDatabase.ProductRepository;
import com.example.SpringApi.Repository.CarrierDatabase.ProductReviewRepository;
import com.example.SpringApi.Repository.CentralDatabase.CarrierRepository;
import com.example.SpringApi.Repository.CentralDatabase.UserRepository;
import com.example.SpringApi.Services.BaseDataAccessor;
import com.example.SpringApi.Services.CentralDatabase.UserLogDataAccessor;
import com.example.SpringApi.SuccessMessages;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.example.ApiRoutes;
import com.example.SpringApi.DatabaseModels.CarrierDatabase.Product;
import org.example.CommonHelpers.HelperUtils;
import org.example.Models.CommunicationModels.CarrierModels.ProductReview;
import org.example.Models.CommunicationModels.CentralModels.User;
import org.example.Models.RequestModels.GridRequestModels.PaginationBaseRequestModel;
import org.example.Models.ResponseModels.ApiResponseModels.ProductReviewResponseModel;
import org.example.Models.ResponseModels.Response;
import org.example.Translators.CarrierDatabaseTranslators.Interfaces.IProductReviewSubTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class ProductReviewDataAccessor extends BaseDataAccessor implements IProductReviewSubTranslator {
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductReviewRepository productReviewRepository;
    private final UserLogDataAccessor userLogDataAccessor;

    @Autowired
    public ProductReviewDataAccessor(HttpServletRequest request, CarrierRepository carrierRepository,
                                     ProductRepository productRepository,
                                     UserRepository userRepository,
                                     ProductReviewRepository productReviewRepository,
                                     UserLogDataAccessor userLogDataAccessor) {
        super(request, carrierRepository);
        this.productRepository = productRepository;
        this.productReviewRepository = productReviewRepository;
        this.userLogDataAccessor = userLogDataAccessor;
        this.userRepository = userRepository;
    }

    private Pair<String, Boolean> validateProductReview(ProductReview productReview) {
        // validate required fields
        if(productReview.getRatings() < 0 || productReview.getRatings() > 5) {
            return Pair.of(ErrorMessages.ProductReviewErrorMessages.ER001, false);
        }

        if(productReview.getReview() == null || !StringUtils.hasText(productReview.getReview())) {
            return Pair.of(ErrorMessages.ProductReviewErrorMessages.ER002, false);
        }

        if(productReview.getProductId() == 0L || productRepository.findById(productReview.getProductId()).isEmpty()) {
            return Pair.of(ErrorMessages.ProductReviewErrorMessages.ER004, false);
        }

        return Pair.of("Success", true);
    }

    @Override
    public Response<Long> insertProductReview(ProductReview productReview) {
        Pair<String, Boolean> validateProductReview = validateProductReview(productReview);
        if(!validateProductReview.getValue()) {
            return new Response<>(false, validateProductReview.getKey(), null);
        }

        // its an edit
        if(productReview.getReviewId() != null) {
            Optional<com.example.SpringApi.DatabaseModels.CarrierDatabase.ProductReview> existingProductReview = productReviewRepository.findById(productReview.getReviewId());
            if(existingProductReview.isEmpty()){
                return new Response<>(false, ErrorMessages.ProductReviewErrorMessages.InvalidId, null);
            }

            existingProductReview.get().setRatings(productReview.getRatings());
            existingProductReview.get().setReview(productReview.getReview());
            productReviewRepository.save(existingProductReview.get());

            userLogDataAccessor.logData(getUserId(),
                    SuccessMessages.ProductReviewSuccessMessages.UpdateProductReview  + " " + productReview.getReviewId(),
                    ApiRoutes.ProductReviewSubRoute.INSERT_PRODUCT_REVIEW);
            return new Response<>(true, SuccessMessages.ProductReviewSuccessMessages.UpdateProductReview, productReview.getReviewId());
        }
        // its a new review
        else{
            productReview.setUserId(getUserId());
            productReview.setScore(0);
            com.example.SpringApi.DatabaseModels.CarrierDatabase.ProductReview savedProductReview = productReviewRepository.save(HelperUtils.copyFields(productReview,
                    com.example.SpringApi.DatabaseModels.CarrierDatabase.ProductReview.class));

            userLogDataAccessor.logData(getUserId(),
                    SuccessMessages.ProductReviewSuccessMessages.InsertProductReview + " " + savedProductReview.getReviewId(),
                    ApiRoutes.ProductReviewSubRoute.INSERT_PRODUCT_REVIEW);
            return new Response<>(true, SuccessMessages.ProductReviewSuccessMessages.InsertProductReview, savedProductReview.getReviewId());
        }
    }

    @Override
    public Response<Boolean> deleteReview(long reviewId) {
        Optional<com.example.SpringApi.DatabaseModels.CarrierDatabase.ProductReview> productReview = productReviewRepository.findById(reviewId);
        if(productReview.isEmpty()){
            return new Response<>(false, ErrorMessages.ProductReviewErrorMessages.InvalidId, false);
        }

        if(productReview.get().getUserId() != getUserId()) {
            return new Response<>(false, ErrorMessages.Unauthorized, false);
        }

        // Delete all children of the review
        deleteAllChildren(reviewId);
        productReviewRepository.deleteById(reviewId);

        return new Response<>(true, SuccessMessages.ProductReviewSuccessMessages.DeleteProductReview, true);
    }

    private void deleteAllChildren(Long parentId) {
        // Find all children of the current parent
        List<com.example.SpringApi.DatabaseModels.CarrierDatabase.ProductReview> children = productReviewRepository.findByParentId(parentId);

        // For each child, recursively delete its children
        for (com.example.SpringApi.DatabaseModels.CarrierDatabase.ProductReview child : children) {
            deleteAllChildren(child.getReviewId());
        }

        // Finally delete the children themselves
        productReviewRepository.deleteAll(children);
    }

    @Override
    public Response<Boolean> toggleProductReviewScore(long reviewId, boolean increaseScore) {
        Optional<com.example.SpringApi.DatabaseModels.CarrierDatabase.ProductReview> productReview = productReviewRepository.findById(reviewId);
        if(productReview.isEmpty()){
            return new Response<>(false, ErrorMessages.ProductReviewErrorMessages.InvalidId, null);
        }

        if(increaseScore){
            productReview.get().setScore(productReview.get().getScore() + 1);
        }
        else {
            productReview.get().setScore(productReview.get().getScore() - 1);
        }

        productReviewRepository.save(productReview.get());
        userLogDataAccessor.logData(getUserId(),
                SuccessMessages.ProductReviewSuccessMessages.ScoreUpdate + " " + reviewId,
                ApiRoutes.ProductReviewSubRoute.TOGGLE_PRODUCT_REVIEW_SCORE);

        return new Response<>(true, SuccessMessages.ProductReviewSuccessMessages.ScoreUpdate, true);
    }

    @Override
    public Response<ProductReviewResponseModel> getProductReviewsGivenProductId(PaginationBaseRequestModel paginationBaseRequestModel, long productId) {
        Optional<Product> product = productRepository.findById(productId);
        if(product.isEmpty()){
            return new Response<>(false, ErrorMessages.ProductErrorMessages.InvalidId, null);
        }

        Pageable pageable = PageRequest.of(paginationBaseRequestModel.getStart() / (paginationBaseRequestModel.getEnd() - paginationBaseRequestModel.getStart()),
                paginationBaseRequestModel.getEnd() - paginationBaseRequestModel.getStart(),
                Sort.by("reviewId").descending());

        Page<com.example.SpringApi.DatabaseModels.CarrierDatabase.ProductReview> productReviews = productReviewRepository.findPaginatedProductReviewByProductId(productId ,pageable);

        // bfs traversal and tree formation
        LinkedList<com.example.SpringApi.DatabaseModels.CarrierDatabase.ProductReview> queue = new LinkedList<>(productReviews.getContent());
        Map<Long, List<Long>> response = new HashMap<>();
        Map<Long, com.example.SpringApi.DatabaseModels.CarrierDatabase.ProductReview> reviewMap = new HashMap<>();
        HashSet<Long> userIds = new HashSet<>();
        while(!queue.isEmpty()){
            com.example.SpringApi.DatabaseModels.CarrierDatabase.ProductReview node = queue.pop();
            reviewMap.put(node.getReviewId(), node);
            userIds.add(node.getUserId());
            List<com.example.SpringApi.DatabaseModels.CarrierDatabase.ProductReview> childs = productReviewRepository.findByParentId(node.getReviewId());
            if(response.containsKey(node.getReviewId())){
                response.get(node.getReviewId()).addAll(childs.stream().map(com.example.SpringApi.DatabaseModels.CarrierDatabase.ProductReview::getReviewId).toList());
            }
            else{
                response.put(node.getReviewId(), childs == null ? new ArrayList<>() : childs.stream().map(com.example.SpringApi.DatabaseModels.CarrierDatabase.ProductReview::getReviewId).collect(Collectors.toList()));
            }
            queue.addAll(childs);
        }

        // preparing the response model
        ProductReviewResponseModel productReviewResponseModel = new ProductReviewResponseModel();
        productReviewResponseModel.setProductReviewTree(response);

        productReviewResponseModel.setProductReviewMap(reviewMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> HelperUtils.copyFields(entry.getValue(), org.example.Models.CommunicationModels.CarrierModels.ProductReview.class))));

        productReviewResponseModel.setUserIdFullNameMapping(
                userIds.stream()
                        .collect(Collectors.toMap(
                                key -> key,
                                value -> {
                                    Optional<com.example.SpringApi.DatabaseModels.CentralDatabase.User> userOpt = userRepository.findById(value);
                                    return userOpt.map(u -> u.getFirstName() + " " + u.getLastName())
                                            .orElse("");
                                }
                        ))
        );

        productReviewResponseModel.setTotalRootComments(productReviews.getTotalElements());

        return new Response<>(true, SuccessMessages.ProductReviewSuccessMessages.GetProductReview, productReviewResponseModel);
    }

    @Override
    public Response<Boolean> toggleProductReview(long id) {
        Optional<com.example.SpringApi.DatabaseModels.CarrierDatabase.ProductReview> productReview = productReviewRepository.findById(id);
        if(productReview.isEmpty()){
            return new Response<>(false, ErrorMessages.ProductReviewErrorMessages.InvalidId, false);
        }

        productReview.get().setDeleted(!productReview.get().isDeleted());
        productReviewRepository.save(productReview.get());
        userLogDataAccessor.logData(getUserId(),
                SuccessMessages.ProductReviewSuccessMessages.ToggleProductReview + " " + productReview.get().getReviewId(),
                ApiRoutes.ProductReviewSubRoute.TOGGLE_PRODUCT_REVIEW);
        return new Response<>(true, SuccessMessages.ProductReviewSuccessMessages.ToggleProductReview, true);
    }

    @Override
    public Response<ProductReviewResponseModel> getProductReviewById(long id) {
        Optional<com.example.SpringApi.DatabaseModels.CarrierDatabase.ProductReview> productReview = productReviewRepository.findById(id);
        if(productReview.isEmpty()){
            return new Response<>(false, ErrorMessages.ProductReviewErrorMessages.InvalidId, null);
        }

        ProductReviewResponseModel productReviewResponseModel = new ProductReviewResponseModel();
        productReviewResponseModel.setProductReview(HelperUtils.copyFields(productReview.get(), ProductReview.class));
        productReviewResponseModel.setUser(
                userRepository.findById(productReview.get().getUserId())
                        .map(user -> HelperUtils.copyFields(user, User.class))
                        .orElse(null)
        );

        return new Response<>(true, SuccessMessages.ProductReviewSuccessMessages.GetProductReview, productReviewResponseModel);
    }
}