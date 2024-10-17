package com.example.SpringApi.Controller.Tests.CarrierDatabase;

import com.example.SpringApi.Controllers.CarrierDatabase.ProductReviewController;
import com.example.SpringApi.Services.CarrierDatabase.ProductReviewDataAccessor;
import org.example.Models.CommunicationModels.CarrierModels.ProductReview;
import org.example.Models.RequestModels.GridRequestModels.PaginationBaseRequestModel;
import org.example.Models.ResponseModels.ApiResponseModels.ProductReviewResponseModel;
import org.example.Models.ResponseModels.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductReviewControllerTest {
    @InjectMocks
    ProductReviewController productReviewController;
    @Mock
    ProductReviewDataAccessor productReviewDataAccessor;

    @Test
    public void testInsertProductReview() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock the request data
        ProductReview productReview = new ProductReview();
        productReview.setRatings(4.5);
        productReview.setReview("Test Review");
        productReview.setProductId(1);

        // mock the data accessor
        Response<Long> response = new Response<>(true, "Success", 1L);
        when(productReviewDataAccessor.insertProductReview(any(ProductReview.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<Long>> responseEntity = productReviewController.insertProductReview(productReview);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isEqualTo(1L);
    }

    @Test
    public void testGetProductReviewsGivenProductId() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock the request data
        PaginationBaseRequestModel paginationBaseRequestModel = new PaginationBaseRequestModel();
        paginationBaseRequestModel.setStart(0);
        paginationBaseRequestModel.setEnd(1);
        long productId = 1L;

        // mock the data accessor
        Response<ProductReviewResponseModel> response = new Response<>(true, "Success", new ProductReviewResponseModel());
        when(productReviewDataAccessor.getProductReviewsGivenProductId(any(PaginationBaseRequestModel.class), any(Long.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<ProductReviewResponseModel>> responseEntity = productReviewController.getProductReviewsGivenProductId(paginationBaseRequestModel, productId);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isNotNull();
    }

//    @Test
//    public void testToggleProductReview() {
//        MockHttpServletRequest request = new MockHttpServletRequest();
//        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
//
//        // mock the request data
//        long id = 1L;
//
//        // mock the data accessor
//        Response<Boolean> response = new Response<>(true, "Success", true);
//        when(productReviewDataAccessor.toggleProductReview(any(Long.class))).thenReturn(response);
//
//        // test the controller
//        ResponseEntity<Response<Boolean>> responseEntity = productReviewController.toggleProductReview(id);
//        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
//        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
//        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isEqualTo(true);
//    }
}
