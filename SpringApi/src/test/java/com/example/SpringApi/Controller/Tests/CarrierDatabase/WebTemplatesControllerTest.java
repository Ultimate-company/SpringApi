package com.example.SpringApi.Controller.Tests.CarrierDatabase;

import com.example.SpringApi.Controllers.CarrierDatabase.WebTemplatesController;
import com.example.SpringApi.Services.CarrierDatabase.WebTemplatesDataAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.Models.RequestModels.ApiRequestModels.WebTemplateRequestModel;
import org.example.Models.RequestModels.GridRequestModels.PaginationBaseRequestModel;
import org.example.Models.ResponseModels.ApiResponseModels.PaginationBaseResponseModel;
import org.example.Models.ResponseModels.ApiResponseModels.WebTemplateResponseModel;
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

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WebTemplatesControllerTest{
    @InjectMocks
    WebTemplatesController webTemplatesController;
    @Mock
    WebTemplatesDataAccessor webTemplatesDataAccessor;

    @Test
    public void testGetWebTemplateById() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock the data
        long id = 1L;

        // mock the data accessor
        Response<WebTemplateResponseModel> response = new Response<>(true, "Success", new WebTemplateResponseModel());
        when(webTemplatesDataAccessor.getWebTemplateById(any(Long.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<WebTemplateResponseModel>> responseEntity = webTemplatesController.getWebTemplateById(id);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isNotNull();
    }

    @Test
    public void testGetWebTemplateInBatches() {
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
        Response<PaginationBaseResponseModel<WebTemplateResponseModel>> response = new Response<>(true, "Success", new PaginationBaseResponseModel<>());
        when(webTemplatesDataAccessor.getWebTemplatesInBatches(any(PaginationBaseRequestModel.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<PaginationBaseResponseModel<WebTemplateResponseModel>>> responseEntity = webTemplatesController.getWebTemplatesInBatches(paginationBaseRequestModel);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isNotNull();
    }

    @Test
    public void testInsertWebTemplate() throws JsonProcessingException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock the request data
        WebTemplateRequestModel webTemplateRequestModel = new WebTemplateRequestModel();

        // Populate sortOptions, filterOptions, selectedProductIds, acceptedPaymentOptions, and shippingStates
        webTemplateRequestModel.setSortOptions(List.of("Price(low to high)", "Price(high to low)", "Rating", "Newest", "Oldest"));
        webTemplateRequestModel.setFilterOptions(List.of("Price Range", "Category", "Brand", "Size", "Color", "Rating", "Availability"));
        webTemplateRequestModel.setSelectedProductIds(List.of(1L, 2L, 3L, 4L));
        webTemplateRequestModel.setAcceptedPaymentOptions(List.of("Credit Card", "Debit Card", "Amazon Pay", "Net Banking", "UPI", "EMI", "Gift Cards", "Cash on Delivery (COD)"));

        // Populate stateCityMapping
        HashMap<String, List<String>> stateCityMapping = new HashMap<>();
        stateCityMapping.put("Maharashtra", List.of("Mumbai", "Pune"));
        stateCityMapping.put("Tamil Nadu", List.of("Chennai", "Coimbatore"));
        stateCityMapping.put("Karnataka", List.of("Bangalore", "Mysore"));
        webTemplateRequestModel.setStateCityMapping(stateCityMapping);

        // Populate webTemplate
        org.example.Models.CommunicationModels.CarrierModels.WebTemplate webTemplate = new org.example.Models.CommunicationModels.CarrierModels.WebTemplate();
        webTemplate.setUrl("http://PostmanClient2.ultimatecompany.com");
        webTemplateRequestModel.setWebTemplate(webTemplate);

        // Populate cardHeaderFontStyle, cardSubTextFontStyle, and headerFontStyle
        org.example.Models.CommunicationModels.CarrierModels.WebTemplatesFontStyle webTemplatesFontStyle = new org.example.Models.CommunicationModels.CarrierModels.WebTemplatesFontStyle();
        webTemplatesFontStyle.setFontStyle("Times New Roman");
        webTemplatesFontStyle.setFontColor("#FFF");
        webTemplatesFontStyle.setFontSize(12.0);
        webTemplateRequestModel.setCardHeaderFontStyle(webTemplatesFontStyle);
        webTemplateRequestModel.setCardSubTextFontStyle(webTemplatesFontStyle);
        webTemplateRequestModel.setHeaderFontStyle(webTemplatesFontStyle);

        // mock the data accessor
        Response<Long> response = new Response<>(true, "Success", 1L);
        when(webTemplatesDataAccessor.insertWebTemplate(any(WebTemplateRequestModel.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<Long>> responseEntity = webTemplatesController.insertWebTemplate(webTemplateRequestModel);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(responseEntity.getBody().getItem()).isEqualTo(1L);
    }

    @Test
    public void testToggleWebTemplate() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock the request data
        long id = 1L;

        // mock the data accessor
        Response<Boolean> response = new Response<>(true, "Success", true);
        when(webTemplatesDataAccessor.toggleWebTemplate(any(Long.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<Boolean>> responseEntity = webTemplatesController.toggleWebTemplate(id);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(responseEntity.getBody().getItem()).isEqualTo(true);
    }

    @Test
    public void testUpdateWebTemplate() throws JsonProcessingException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock the request data
        WebTemplateRequestModel webTemplateRequestModel = new WebTemplateRequestModel();

        // Populate sortOptions, filterOptions, selectedProductIds, acceptedPaymentOptions, and shippingStates
        webTemplateRequestModel.setSortOptions(List.of("Price(low to high)", "Price(high to low)", "Rating", "Newest", "Oldest"));
        webTemplateRequestModel.setFilterOptions(List.of("Price Range", "Category", "Brand", "Size", "Color", "Rating", "Availability"));
        webTemplateRequestModel.setSelectedProductIds(List.of(1L, 2L, 3L, 4L));
        webTemplateRequestModel.setAcceptedPaymentOptions(List.of("Credit Card", "Debit Card", "Amazon Pay", "Net Banking", "UPI", "EMI", "Gift Cards", "Cash on Delivery (COD)"));

        // Populate stateCityMapping
        HashMap<String, List<String>> stateCityMapping = new HashMap<>();
        stateCityMapping.put("Maharashtra", List.of("Mumbai", "Pune"));
        stateCityMapping.put("Tamil Nadu", List.of("Chennai", "Coimbatore"));
        stateCityMapping.put("Karnataka", List.of("Bangalore", "Mysore"));
        webTemplateRequestModel.setStateCityMapping(stateCityMapping);

        // Populate webTemplate
        org.example.Models.CommunicationModels.CarrierModels.WebTemplate webTemplate = new org.example.Models.CommunicationModels.CarrierModels.WebTemplate();
        webTemplate.setUrl("http://PostmanClient2.ultimatecompany.com");
        webTemplate.setWebTemplateId(1L);
        webTemplateRequestModel.setWebTemplate(webTemplate);

        // Populate cardHeaderFontStyle, cardSubTextFontStyle, and headerFontStyle
        org.example.Models.CommunicationModels.CarrierModels.WebTemplatesFontStyle webTemplatesFontStyle = new org.example.Models.CommunicationModels.CarrierModels.WebTemplatesFontStyle();
        webTemplatesFontStyle.setFontStyle("Times New Roman");
        webTemplatesFontStyle.setFontColor("#FFF");
        webTemplatesFontStyle.setFontSize(12.0);
        webTemplateRequestModel.setCardHeaderFontStyle(webTemplatesFontStyle);
        webTemplateRequestModel.setCardSubTextFontStyle(webTemplatesFontStyle);
        webTemplateRequestModel.setHeaderFontStyle(webTemplatesFontStyle);

        // mock the data accessor
        Response<Long> response = new Response<>(true, "Success", 1L);
        when(webTemplatesDataAccessor.updateWebTemplate(any(WebTemplateRequestModel.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<Long>> responseEntity = webTemplatesController.updateWebTemplate(webTemplateRequestModel);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(responseEntity.getBody().getItem()).isEqualTo(1L);
    }
}
