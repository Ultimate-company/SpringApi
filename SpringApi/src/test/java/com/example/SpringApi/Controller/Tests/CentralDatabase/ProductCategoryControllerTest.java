package com.example.SpringApi.Controller.Tests.CentralDatabase;

import com.example.SpringApi.Controllers.CentralDatabase.ProductCategoryController;
import com.example.SpringApi.Services.CentralDatabase.ProductCategoryDataAccessor;
import org.example.Models.CommunicationModels.CentralModels.ProductCategory;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductCategoryControllerTest {
    @InjectMocks
    ProductCategoryController productCategoryController;
    @Mock
    ProductCategoryDataAccessor productCategoryDataAccessor;

    @Test
    public void testGetRootCategories() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock the data accessor
        Response<List<ProductCategory>> response = new Response<>(true, "Success", new ArrayList<>());
        when(productCategoryDataAccessor.getRootCategories()).thenReturn(response);

        // test the controller
        ResponseEntity<Response<List<ProductCategory>>> responseEntity = productCategoryController.getRootCategories();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isNotNull();
    }

    @Test
    public void testGetChildCategoriesGivenParentId() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock the request data
        long id = 1L;

        // mock the data accessor
        Response<List<ProductCategory>> response = new Response<>(true, "Success", new ArrayList<>());
        when(productCategoryDataAccessor.getChildCategoriesGivenParentId(any(Long.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<List<ProductCategory>>> responseEntity = productCategoryController.getChildCategoriesGivenParentId(id);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isNotNull();
    }
}