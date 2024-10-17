package com.example.SpringApi.Controller.Tests.CentralDatabase;

import com.example.SpringApi.Controllers.CentralDatabase.CarrierController;
import com.example.SpringApi.Services.CentralDatabase.CarrierDataAccessor;
import com.example.SpringApi.DatabaseModels.CentralDatabase.Carrier;
import org.example.Models.RequestModels.ApiRequestModels.BaseRequest;
import org.example.Models.RequestModels.GridRequestModels.GetCarriersRequestModel;
import org.example.Models.ResponseModels.ApiResponseModels.PaginationBaseResponseModel;
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
public class CarrierControllerTest {

    @InjectMocks
    CarrierController carrierController;
    @Mock
    CarrierDataAccessor carrierDataAccessor;

    @Test
    public void testGetCarrierDetailsById() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

//        // mock the data accessor
//        Response<Carrier> response = new Response<>(true, "Success", new Carrier());
//        when(carrierDataAccessor.getCarrierDetailsById()).thenReturn(response);
//
//        // test the controller
//        ResponseEntity<Response<Carrier>> responseEntity = carrierController.getCarrierDetailsById();
//        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
//        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
//        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isNotNull();
    }

    @Test
    public void testIsUserMappedToCarrier() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // Data
        BaseRequest baseRequest = new BaseRequest();
        baseRequest.setCarrierId(1L);
        baseRequest.setUserId(552L);

//        // mock the data accessor
//        Response<Boolean> response = new Response<>(true, "Success", true);
//        when(carrierDataAccessor.isUserMappedToCarrier(any(Long.class), any(Long.class))).thenReturn(response);
//
//        // test the controller
//        ResponseEntity<Response<Boolean>> responseEntity = carrierController.isUserMappedToCarrier(baseRequest.getCarrierId(),
//                baseRequest.getUserId());
//        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
//        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
//        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isEqualTo(true);
    }

    @Test
    public void testGetCarrierInBatches() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

//        // Data
//        GetCarriersRequestModel getCarriersRequestModel = new GetCarriersRequestModel();
//        getCarriersRequestModel.setStart(0);
//        getCarriersRequestModel.setEnd(10);
//        getCarriersRequestModel.setFilterExpr("Carrier1");
//        getCarriersRequestModel.setUserId(552);
//
//        // mock the data accessor
//        Response<PaginationBaseResponseModel<Carrier>> response = new Response<>(true, "Success", new PaginationBaseResponseModel<>());
//        when(carrierDataAccessor.getCarrierInBatches(any(GetCarriersRequestModel.class))).thenReturn(response);
//
//        // test the controller
//        ResponseEntity<Response<PaginationBaseResponseModel<Carrier>>> responseEntity = carrierController.getCarrierInBatches(getCarriersRequestModel);
//        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
//        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
//        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isNotNull();
    }

    @Test
    public void testGetApiKeys() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

//        // mock the data accessor
//        Response<Carrier> response = new Response<>(true, "Success", new Carrier());
//        when(carrierDataAccessor.getApiKeys()).thenReturn(response);
//
//        // test the controller
//        ResponseEntity<Response<Carrier>> responseEntity = carrierController.getApiKeys();
//        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
//        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
//        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isNotNull();
    }
}
