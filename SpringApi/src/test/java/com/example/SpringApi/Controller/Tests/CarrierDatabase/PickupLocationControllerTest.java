package com.example.SpringApi.Controller.Tests.CarrierDatabase;

import com.example.SpringApi.Controllers.CarrierDatabase.PickupLocationController;
import com.example.SpringApi.Services.CarrierDatabase.PickupLocationDataAccessor;
import org.example.Models.CommunicationModels.CarrierModels.PickupLocation;
import org.example.Models.RequestModels.ApiRequestModels.PickupLocationRequestModel;
import org.example.Models.RequestModels.GridRequestModels.PaginationBaseRequestModel;
import org.example.Models.ResponseModels.ApiResponseModels.PaginationBaseResponseModel;
import org.example.Models.ResponseModels.ApiResponseModels.PickupLocationResponseModel;
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
public class PickupLocationControllerTest {
    @InjectMocks
    PickupLocationController pickupLocationController;
    @Mock
    PickupLocationDataAccessor pickupLocationDataAccessor;

    @Test
    public void testGetPickupLocationInBatches() {
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
        Response<PaginationBaseResponseModel<PickupLocationResponseModel>> response = new Response<>(true, "Success", new PaginationBaseResponseModel<>());
        when(pickupLocationDataAccessor.getPickupLocationsInBatches(any(PaginationBaseRequestModel.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<PaginationBaseResponseModel<PickupLocationResponseModel>>> responseEntity = pickupLocationController.getPickupLocationInBatches(paginationBaseRequestModel);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isNotNull();
    }

    @Test
    public void testAddPickupLocation() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock the request data
        PickupLocationRequestModel pickupLocationRequestModel = new PickupLocationRequestModel();
        org.example.Models.CommunicationModels.CarrierModels.PickupLocation pickupLocation = new org.example.Models.CommunicationModels.CarrierModels.PickupLocation();
        org.example.Models.CommunicationModels.CarrierModels.Address address = new org.example.Models.CommunicationModels.CarrierModels.Address();
        pickupLocation.setAddressNickName("Test pickup location 1");
        address.setLine1("123 Main Street");
        address.setLine2("Apt 101");
        address.setLandmark("landmark");
        address.setState("state");
        address.setCity("city");
        address.setZipCode("123456");
        address.setNameOnAddress("john doe");
        address.setEmailAtAddress("john@example.com");
        address.setPhoneOnAddress("1234567890");
        address.setAddressLabel("home");

        pickupLocationRequestModel.setAddress(address);
        pickupLocationRequestModel.setPickupLocation(pickupLocation);

        // mock the data accessor
        Response<Long> response = new Response<>(true, "Success", 1L);
        when(pickupLocationDataAccessor.createPickupLocation(any(PickupLocationRequestModel.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<Long>> responseEntity = pickupLocationController.createPickupLocation(pickupLocationRequestModel);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(responseEntity.getBody().getItem()).isEqualTo(1L);
    }

    @Test
    public void testGetAllPickupLocations() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock the data
        boolean includeDeleted = true;

        // mock the data accessor
        Response<List<PickupLocation>> response = new Response<>(true, "Success", new ArrayList<>());
        when(pickupLocationDataAccessor.getAllPickupLocations(any(Boolean.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<List<PickupLocation>>> responseEntity = pickupLocationController.getAllPickupLocations(includeDeleted);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(responseEntity.getBody().getItem()).isNotNull();
    }

    @Test
    public void testUpdatePickupLocation() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock the request data
        PickupLocationRequestModel pickupLocationRequestModel = new PickupLocationRequestModel();
        org.example.Models.CommunicationModels.CarrierModels.PickupLocation pickupLocation = new org.example.Models.CommunicationModels.CarrierModels.PickupLocation();
        org.example.Models.CommunicationModels.CarrierModels.Address address = new org.example.Models.CommunicationModels.CarrierModels.Address();
        pickupLocation.setPickupLocationId(1L);
        pickupLocation.setAddressNickName("Test pickup location 1");
        address.setLine1("123 Main Street");
        address.setLine2("Apt 101");
        address.setLandmark("landmark");
        address.setState("state");
        address.setCity("city");
        address.setZipCode("123456");
        address.setNameOnAddress("john doe");
        address.setEmailAtAddress("john@example.com");
        address.setPhoneOnAddress("1234567890");
        address.setAddressLabel("home");

        pickupLocationRequestModel.setAddress(address);
        pickupLocationRequestModel.setPickupLocation(pickupLocation);

        // mock the data accessor
        Response<Long> response = new Response<>(true, "Success", 1L);
        when(pickupLocationDataAccessor.updatePickupLocation(any(PickupLocationRequestModel.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<Long>> responseEntity = pickupLocationController.updatePickupLocation(pickupLocationRequestModel);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(responseEntity.getBody().getItem()).isEqualTo(1L);
    }

    @Test
    public void testGetPickupLocationById() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock the request data
        long id = 1L;

        // mock the data accessor
        Response<PickupLocationResponseModel> response = new Response<>(true, "Success", new PickupLocationResponseModel());
        when(pickupLocationDataAccessor.getPickupLocationById(any(Long.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<PickupLocationResponseModel>> responseEntity = pickupLocationController.getPickupLocationById(id);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(responseEntity.getBody().getItem()).isNotNull();
    }

    @Test
    public void testTogglePickupLocation() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock the request data
        long id = 1L;

        // mock the data accessor
        Response<Boolean> response = new Response<>(true, "Success", true);
        when(pickupLocationDataAccessor.togglePickupLocation(any(Long.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<Boolean>> responseEntity = pickupLocationController.togglePickupLocation(id);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(responseEntity.getBody().getItem()).isEqualTo(true);
    }
}