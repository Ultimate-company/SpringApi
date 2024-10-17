package com.example.SpringApi.Controller.Tests.CarrierDatabase;

import com.example.SpringApi.Controllers.CarrierDatabase.LeadController;
import com.example.SpringApi.Services.CarrierDatabase.LeadDataAccessor;
import com.example.SpringApi.DatabaseModels.CarrierDatabase.Address;
import com.example.SpringApi.DatabaseModels.CarrierDatabase.Lead;
import org.example.Models.RequestModels.ApiRequestModels.LeadRequestModel;
import org.example.Models.RequestModels.GridRequestModels.PaginationBaseRequestModel;
import org.example.Models.ResponseModels.ApiResponseModels.LeadResponseModel;
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
public class LeadControllerTest {
    @InjectMocks
    LeadController leadController;
    @Mock
    LeadDataAccessor leadDataAccessor;

    @Test
    public void testGetLeadsInBatches() {
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
        Response<PaginationBaseResponseModel<LeadResponseModel>> response = new Response<>(true, "Success", new PaginationBaseResponseModel<>());
        when(leadDataAccessor.getLeadsInBatches(any(PaginationBaseRequestModel.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<PaginationBaseResponseModel<LeadResponseModel>>> responseEntity = leadController.getLeadsInBatches(paginationBaseRequestModel);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isNotNull();
    }

    @Test
    public void testInsertLead() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock the request data
        LeadRequestModel leadRequestModel = new LeadRequestModel();
        org.example.Models.CommunicationModels.CarrierModels.Address address = new org.example.Models.CommunicationModels.CarrierModels.Address();
        address.setLine1("123 Main St");
        address.setLine2("Apt 101");
        address.setLandmark("Near Park");
        address.setState("Maharashtra");
        address.setCity("Mumbai");
        address.setZipCode("400002");
        address.setNameOnAddress("John Postman 8 Doe");
        address.setPhoneOnAddress("1234567890");

        org.example.Models.CommunicationModels.CarrierModels.Lead lead = new org.example.Models.CommunicationModels.CarrierModels.Lead();
        lead.setAnnualRevenue("1000000");
        lead.setCompany("Test Company");
        lead.setCompanySize(500);
        lead.setEmail("test@example.com");
        lead.setFirstName("John Postman 8");
        lead.setFax("1234567890");
        lead.setLastName("Doe");
        lead.setLeadStatus("Contacted");
        lead.setPhone("1234567890");
        lead.setTitle("Manager");
        lead.setWebsite("https://example.com");
        lead.setDeleted(false);
        lead.setAssignedAgentId(552L);

        leadRequestModel.setAddress(address);
        leadRequestModel.setLead(lead);

        // mock the data accessor
        Response<Long> response = new Response<>(true, "Success", 1L);
        when(leadDataAccessor.createLead(any(LeadRequestModel.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<Long>> responseEntity = leadController.insertLead(leadRequestModel);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isEqualTo(1L);
    }

    @Test
    public void testUpdateLead() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock the request data
        LeadRequestModel leadRequestModel = new LeadRequestModel();
        org.example.Models.CommunicationModels.CarrierModels.Address address = new org.example.Models.CommunicationModels.CarrierModels.Address();
        address.setLine1("123 Main St");
        address.setLine2("Apt 101");
        address.setLandmark("Near Park");
        address.setState("Maharashtra");
        address.setCity("Mumbai");
        address.setZipCode("400002");
        address.setNameOnAddress("John Postman 8 Doe");
        address.setPhoneOnAddress("1234567890");

        org.example.Models.CommunicationModels.CarrierModels.Lead lead = new org.example.Models.CommunicationModels.CarrierModels.Lead();
        lead.setLeadId(1);
        lead.setAnnualRevenue("1000000");
        lead.setCompany("Test Company");
        lead.setCompanySize(500);
        lead.setEmail("test@example.com");
        lead.setFirstName("John Postman 8");
        lead.setFax("1234567890");
        lead.setLastName("Doe");
        lead.setLeadStatus("Contacted");
        lead.setPhone("1234567890");
        lead.setTitle("Manager");
        lead.setWebsite("https://example.com");
        lead.setDeleted(false);
        lead.setAssignedAgentId(552L);

        leadRequestModel.setAddress(address);
        leadRequestModel.setLead(lead);

        // mock the data accessor
        Response<Long> response = new Response<>(true, "Success", 1L);
        when(leadDataAccessor.updateLead(any(LeadRequestModel.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<Long>> responseEntity = leadController.updateLead(leadRequestModel);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isEqualTo(1L);
    }

    @Test
    public void testGetLeadDetailsById() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock the data
        long id = 1L;

        // mock the data accessor
        Response<LeadResponseModel> response = new Response<>(true, "Success", new LeadResponseModel());
        when(leadDataAccessor.getLeadDetailsById(any(Long.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<LeadResponseModel>> responseEntity = leadController.getLeadDetailsById(id);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isNotNull();
    }

    @Test
    public void testToggleLead() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock the data
        long id = 1L;

        // mock the data accessor
        Response<Boolean> response = new Response<>(true, "Success", true);
        when(leadDataAccessor.toggleLead(any(Long.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<Boolean>> responseEntity = leadController.toggleLead(id);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isEqualTo(true);
    }
}