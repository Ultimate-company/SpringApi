package com.example.SpringApi.Controller.Tests.CarrierDatabase;

import com.example.SpringApi.Controllers.CarrierDatabase.PurchaseOrderController;
import com.example.SpringApi.Services.CarrierDatabase.PurchaseOrderDataAccessor;
import com.itextpdf.text.DocumentException;
import freemarker.template.TemplateException;
import org.example.Models.RequestModels.ApiRequestModels.PurchaseOrderRequestModel;
import org.example.Models.RequestModels.GridRequestModels.PaginationBaseRequestModel;
import org.example.Models.ResponseModels.ApiResponseModels.PaginationBaseResponseModel;
import org.example.Models.ResponseModels.ApiResponseModels.PurchaseOrderResponseModel;
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

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PurchaseOrderControllerTest {
    @InjectMocks
    PurchaseOrderController purchaseOrderController;
    @Mock
    PurchaseOrderDataAccessor purchaseOrderDataAccessor;

    @Test
    public void testGetPurchaseOrdersInBatches() {
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
        Response<PaginationBaseResponseModel<PurchaseOrderResponseModel>> response = new Response<>(true, "Success", new PaginationBaseResponseModel<>());
        when(purchaseOrderDataAccessor.getPurchaseOrdersInBatches(any(PaginationBaseRequestModel.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<PaginationBaseResponseModel<PurchaseOrderResponseModel>>> responseEntity = purchaseOrderController.getPurchaseOrdersInBatches(paginationBaseRequestModel);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isNotNull();
    }

    @Test
    public void testInsertPurchaseOrder() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        //mock the request data
        org.example.Models.CommunicationModels.CarrierModels.PurchaseOrder purchaseOrder = new org.example.Models.CommunicationModels.CarrierModels.PurchaseOrder();
        purchaseOrder.setExpectedShipmentDate(LocalDateTime.parse("2025-04-01T08:00:00"));
        purchaseOrder.setVendorNumber("VENDOR123");
        purchaseOrder.setTermsConditionsHtml("<p>Terms and conditions HTML content</p>");
        purchaseOrder.setTermsConditionsMarkdown("Terms and conditions Markdown content");
        purchaseOrder.setOrderReceipt("Order receipt content 3");
        purchaseOrder.setAssignedLeadId(7L);

        org.example.Models.CommunicationModels.CarrierModels.Address address = new org.example.Models.CommunicationModels.CarrierModels.Address();
        address.setLine1("123 Main St");
        address.setLine2("Apt 101");
        address.setState("California");
        address.setCity("Tamil Nadu");
        address.setZipCode("90001");
        address.setNameOnAddress("John Doe Purchase Order");
        address.setPhoneOnAddress("1234567890");

        Map<Long, Integer> productIdQuantityMapping = new HashMap<>();
        productIdQuantityMapping.put(1L, 10);
        productIdQuantityMapping.put(2L, 5);
        productIdQuantityMapping.put(3L, 3);

        PurchaseOrderRequestModel purchaseOrderRequestModel = new PurchaseOrderRequestModel();
        purchaseOrderRequestModel.setPurchaseOrder(purchaseOrder);
        purchaseOrderRequestModel.setAddress(address);
        purchaseOrderRequestModel.setProductIdQuantityMapping(productIdQuantityMapping);

        // mock the data accessor
        Response<Long> response = new Response<>(true, "Success", 1L);
        when(purchaseOrderDataAccessor.createPurchaseOrder(any(PurchaseOrderRequestModel.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<Long>> responseEntity = purchaseOrderController.createPurchaseOrder(purchaseOrderRequestModel);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isEqualTo(1L);
    }

    @Test
    public void testUpdatePurchaseOrder() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        //mock the request data
        org.example.Models.CommunicationModels.CarrierModels.PurchaseOrder purchaseOrder = new org.example.Models.CommunicationModels.CarrierModels.PurchaseOrder();
        purchaseOrder.setPurchaseOrderAddressId(1L);
        purchaseOrder.setExpectedShipmentDate(LocalDateTime.parse("2025-04-01T08:00:00"));
        purchaseOrder.setVendorNumber("VENDOR123");
        purchaseOrder.setTermsConditionsHtml("<p>Terms and conditions HTML content</p>");
        purchaseOrder.setTermsConditionsMarkdown("Terms and conditions Markdown content");
        purchaseOrder.setOrderReceipt("Order receipt content 3");
        purchaseOrder.setAssignedLeadId(7L);

        org.example.Models.CommunicationModels.CarrierModels.Address address = new org.example.Models.CommunicationModels.CarrierModels.Address();
        address.setLine1("123 Main St");
        address.setLine2("Apt 101");
        address.setState("California");
        address.setCity("Tamil Nadu");
        address.setZipCode("90001");
        address.setNameOnAddress("John Doe Purchase Order");
        address.setPhoneOnAddress("1234567890");

        Map<Long, Integer> productIdQuantityMapping = new HashMap<>();
        productIdQuantityMapping.put(1L, 10);
        productIdQuantityMapping.put(2L, 5);
        productIdQuantityMapping.put(3L, 3);

        PurchaseOrderRequestModel purchaseOrderRequestModel = new PurchaseOrderRequestModel();
        purchaseOrderRequestModel.setPurchaseOrder(purchaseOrder);
        purchaseOrderRequestModel.setAddress(address);
        purchaseOrderRequestModel.setProductIdQuantityMapping(productIdQuantityMapping);

        // mock the data accessor
        Response<Long> response = new Response<>(true, "Success", 1L);
        when(purchaseOrderDataAccessor.updatePurchaseOrder(any(PurchaseOrderRequestModel.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<Long>> responseEntity = purchaseOrderController.updatePurchaseOrder(purchaseOrderRequestModel);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isEqualTo(1L);
    }

    @Test
    public void testGetPurchaseOrderDetailsById() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock the request data
        long id = 1L;

        // mock the data accessor
        Response<PurchaseOrderResponseModel> response = new Response<>(true, "Success", new PurchaseOrderResponseModel());
        when(purchaseOrderDataAccessor.getPurchaseOrderDetailsById(any(Long.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<PurchaseOrderResponseModel>> responseEntity = purchaseOrderController.getPurchaseOrderDetailsById(id);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isNotNull();
    }

    @Test
    public void testTogglePurchaseOrder() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock the request data
        long id = 1L;

        // mock the data accessor
        Response<Boolean> response = new Response<>(true, "Success", true);
        when(purchaseOrderDataAccessor.togglePurchaseOrder(any(Long.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<Boolean>> responseEntity = purchaseOrderController.togglePurchaseOrder(id);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isEqualTo(true);
    }

    @Test
    public void testApprovedByPurchaseOrder() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock the request data
        long id = 1L;

        // mock the data accessor
        Response<Boolean> response = new Response<>(true, "Success", true);
        when(purchaseOrderDataAccessor.approvedByPurchaseOrder(any(Long.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<Boolean>> responseEntity = purchaseOrderController.approvedByPurchaseOrder(id);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isEqualTo(true);
    }

    @Test
    public void testGetSalesOrderPDF() throws TemplateException, DocumentException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock the request data
        long id = 1L;

        // mock the data accessor
        Response<String> response = new Response<>(true, "Success", "");
        when(purchaseOrderDataAccessor.getPurchaseOrderPDF(any(Long.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<String>> responseEntity = purchaseOrderController.getPurchaseOrderPDF(id);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isNotNull();
    }
}
