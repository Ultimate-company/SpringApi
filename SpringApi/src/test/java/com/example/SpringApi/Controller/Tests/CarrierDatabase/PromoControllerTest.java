package com.example.SpringApi.Controller.Tests.CarrierDatabase;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import org.example.Models.CommunicationModels.CarrierModels.Promo;
import org.example.Models.RequestModels.GridRequestModels.PaginationBaseRequestModel;
import org.example.Models.ResponseModels.ApiResponseModels.PaginationBaseResponseModel;
import org.example.Models.ResponseModels.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.example.SpringApi.Controllers.CarrierDatabase.PromoController;
import com.example.SpringApi.Services.CarrierDatabase.PromoDataAccessor;

@ExtendWith(MockitoExtension.class)
public class PromoControllerTest {
    @InjectMocks
    PromoController promoController;
    @Mock
    PromoDataAccessor promoDataAccessor;

    @Test
    public void testGetPromosInBatches() {
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
        Response<PaginationBaseResponseModel<Promo>> response = new Response<>(true, "Success", new PaginationBaseResponseModel<>());
        when(promoDataAccessor.getPromosInBatches(any(PaginationBaseRequestModel.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<PaginationBaseResponseModel<Promo>>> responseEntity = promoController.getPromosInBatches(paginationBaseRequestModel);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isNotNull();
    }

    @Test
    public void testAddPromo() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock the data
        Promo promo = new Promo();
        promo.setPromoCode("Test1");
        promo.setDescription("Test Description");
        promo.setDiscountValue(100.0);
        promo.setPercent(false);

        // mock the data accessor
        Response<Long> response = new Response<>(true, "Success", 1L);
        when(promoDataAccessor.createPromo(any(Promo.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<Long>> responseEntity = promoController.addPromo(promo);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isEqualTo(1L);
    }

    @Test
    public void testGetPromoDetailsById() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock the data
        long id = 1L;

        // mock the data accessor
        Response<Promo> response = new Response<>(true, "Success", new Promo());
        when(promoDataAccessor.getPromoDetailsById(any(Long.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<Promo>> responseEntity = promoController.getPromoDetailsById(id);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isNotNull();
    }

    @Test
    public void testTogglePromo() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock the data
        long id = 1L;

        // mock the data accessor
        Response<Boolean> response = new Response<>(true, "Success", true);
        when(promoDataAccessor.togglePromo(any(Long.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<Boolean>> responseEntity = promoController.togglePromo(id);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isEqualTo(true);
    }

    @Test
    public void testGetPromoDetailsByName() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock the data
        String promoCode = "Test1";

        // mock the data accessor
        Response<Promo> response = new Response<>(true, "Success", new Promo());
        when(promoDataAccessor.getPromoDetailsByName(any(String.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<Promo>> responseEntity = promoController.getPromoDetailsByName(promoCode);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isNotNull();
    }
}