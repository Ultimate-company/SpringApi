package com.example.SpringApi.Controller.Tests.CarrierDatabase;

import com.example.SpringApi.Controllers.CarrierDatabase.ProductController;
import com.example.SpringApi.Services.CarrierDatabase.ProductDataAccessor;
import org.example.Models.CommunicationModels.CarrierModels.Product;
import org.example.Models.RequestModels.GridRequestModels.PaginationBaseRequestModel;
import org.example.Models.ResponseModels.ApiResponseModels.PaginationBaseResponseModel;
import org.example.Models.ResponseModels.ApiResponseModels.ProductsResponseModel;
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

import java.time.LocalDateTime;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductControllerTest {
    @InjectMocks
    ProductController productController;
    @Mock
    ProductDataAccessor productDataAccessor;

    @Test
    public void testAddProduct() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        //mock the request data
        Product product = new Product();
        product.setTitle("Test Product Postman 10");
        product.setDescriptionHtml("<p>This is a test product description.</p>");
        product.setLength(10.00);
        product.setAvailableStock(100);
        product.setBrand("Test Brand");
        product.setColor("Red");
        product.setDeleted(false);
        product.setCondition(1);
        product.setCountryOfManufacture("USA");
        product.setModel("Test Model");
        product.setItemModified(false);
        product.setUpc("1234567890");
        product.setModificationHtml("Test Modification");
        product.setModificationHtml("<p>Test Modification Description</p>");
        product.setMainImage("https://example.com/test_main_image.jpg");
        product.setTopImage("https://example.com/test_top_image.jpg");
        product.setBottomImage("https://example.com/test_bottom_image.jpg");
        product.setFrontImage("https://example.com/test_front_image.jpg");
        product.setBackImage("https://example.com/test_back_image.jpg");
        product.setRightImage("https://example.com/test_right_image.jpg");
        product.setLeftImage("https://example.com/test_left_image.jpg");
        product.setDetailsImage("https://example.com/test_details_image.jpg");
        product.setDefectImage("https://example.com/test_defect_image.jpg");
        product.setAdditionalImage1("https://example.com/test_additional_image_1.jpg");
        product.setAdditionalImage2("https://example.com/test_additional_image_2.jpg");
        product.setAdditionalImage3("https://example.com/test_additional_image_3.jpg");
        product.setPrice(99.99);
        product.setDiscount(10.00);
        product.setReturnsAllowed(true);
        product.setItemAvailableFrom(LocalDateTime.parse("2024-03-29T12:00:00"));
        product.setBreadth(5.00);
        product.setHeight(15.00);
        product.setWeightKgs(2.5);
        product.setCategoryId(1);

        // mock the data accessor
        Response<Long> response = new Response<>(true, "Success", 1L);
        when(productDataAccessor.addProduct(any(Product.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<Long>> responseEntity = productController.addProduct(product);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(responseEntity.getBody().getItem()).isEqualTo(1L);
    }

    @Test
    public void testEditProduct() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        //mock the request data
        Product product = new Product();
        product.setProductId(1L);
        product.setTitle("Test Product Postman 10");
        product.setDescriptionHtml("This is a test product description.");
        product.setDescriptionHtml("<p>This is a test product description.</p>");
        product.setLength(10.00);
        product.setAvailableStock(100);
        product.setBrand("Test Brand");
        product.setColor("Red");
        product.setDeleted(false);
        product.setCondition(1);
        product.setCountryOfManufacture("USA");
        product.setModel("Test Model");
        product.setItemModified(false);
        product.setUpc("1234567890");
        product.setModificationHtml("Test Modification");
        product.setModificationHtml("<p>Test Modification Description</p>");
        product.setMainImage("https://example.com/test_main_image.jpg");
        product.setTopImage("https://example.com/test_top_image.jpg");
        product.setBottomImage("https://example.com/test_bottom_image.jpg");
        product.setFrontImage("https://example.com/test_front_image.jpg");
        product.setBackImage("https://example.com/test_back_image.jpg");
        product.setRightImage("https://example.com/test_right_image.jpg");
        product.setLeftImage("https://example.com/test_left_image.jpg");
        product.setDetailsImage("https://example.com/test_details_image.jpg");
        product.setDefectImage("https://example.com/test_defect_image.jpg");
        product.setAdditionalImage1("https://example.com/test_additional_image_1.jpg");
        product.setAdditionalImage2("https://example.com/test_additional_image_2.jpg");
        product.setAdditionalImage3("https://example.com/test_additional_image_3.jpg");
        product.setPrice(99.99);
        product.setDiscount(10.00);
        product.setReturnsAllowed(true);
        product.setItemAvailableFrom(LocalDateTime.parse("2024-03-29T12:00:00"));
        product.setBreadth(5.00);
        product.setHeight(15.00);
        product.setWeightKgs(2.5);
        product.setCategoryId(1);

        // mock the data accessor
        Response<Long> response = new Response<>(true, "Success", 1L);
        when(productDataAccessor.editProduct(any(Product.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<Long>> responseEntity = productController.editProduct(product);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(responseEntity.getBody().getItem()).isEqualTo(1L);
    }

    @Test
    public void testToggleDeleteProduct() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock the request data
        long id = 1L;

        // mock the data accessor
        Response<Boolean> response = new Response<>(true, "Success", true);
        when(productDataAccessor.toggleDeleteProduct(any(Long.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<Boolean>> responseEntity = productController.toggleDeleteProduct(id);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(responseEntity.getBody().getItem()).isEqualTo(true);
    }

    @Test
    public void testToggleReturnProduct() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock the request data
        long id = 1L;

        // mock the data accessor
        Response<Boolean> response = new Response<>(true, "Success", true);
        when(productDataAccessor.toggleReturnProduct(any(Long.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<Boolean>> responseEntity = productController.toggleReturnProduct(id);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(responseEntity.getBody().getItem()).isEqualTo(true);
    }

    @Test
    public void testGetProductDetailsById() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock the request data
        long id = 1L;

        // mock the data accessor
        Response<ProductsResponseModel> response = new Response<>(true, "Success", new ProductsResponseModel());
        when(productDataAccessor.getProductDetailsById(any(Long.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<ProductsResponseModel>> responseEntity = productController.getProductDetailsById(id);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(responseEntity.getBody().getItem()).isNotNull();
    }

    @Test
    public void testGetProductInBatches() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock the request data
        PaginationBaseRequestModel paginationBaseRequestModel = new PaginationBaseRequestModel();
        paginationBaseRequestModel.setStart(0);
        paginationBaseRequestModel.setEnd(3);
        paginationBaseRequestModel.setFilterExpr("");
        paginationBaseRequestModel.setColumnName("");
        paginationBaseRequestModel.setCondition("");
        paginationBaseRequestModel.setIncludeDeleted(true);

        // mock the data accessor
        Response<PaginationBaseResponseModel<ProductsResponseModel>> response = new Response<>(true, "Success", new PaginationBaseResponseModel<>());
        when(productDataAccessor.getProductInBatches(any(PaginationBaseRequestModel.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<PaginationBaseResponseModel<ProductsResponseModel>>> responseEntity = productController.getProductInBatches(paginationBaseRequestModel);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(responseEntity.getBody().getItem()).isNotNull();
    }
}
