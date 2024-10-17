package com.example.SpringApi.Controller.Tests.CarrierDatabase;

import com.example.SpringApi.Controllers.CarrierDatabase.SalesOrderController;
import com.example.SpringApi.Services.CarrierDatabase.SalesOrderDataAccessor;
import com.itextpdf.text.DocumentException;
import freemarker.template.TemplateException;
import org.example.Models.RequestModels.ApiRequestModels.SalesOrderRequestModel;
import org.example.Models.RequestModels.GridRequestModels.GetSalesOrdersRequestModel;
import org.example.Models.ResponseModels.ApiResponseModels.PaginationBaseResponseModel;
import org.example.Models.ResponseModels.ApiResponseModels.SalesOrderResponseModel;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SalesOrderControllerTest {
    @InjectMocks
    SalesOrderController salesOrderController;
    @Mock
    SalesOrderDataAccessor salesOrderDataAccessor;

    @Test
    public void testSalesOrderInBatches() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock the request data
        GetSalesOrdersRequestModel getSalesOrdersRequestModel = new GetSalesOrdersRequestModel();
        getSalesOrdersRequestModel.setStart(0);
        getSalesOrdersRequestModel.setEnd(3);
        getSalesOrdersRequestModel.setFilterExpr("");
        getSalesOrdersRequestModel.setColumnName("");
        getSalesOrdersRequestModel.setCondition("");
        getSalesOrdersRequestModel.setIncludeDeleted(true);

        // mock the data accessor
        Response<PaginationBaseResponseModel<SalesOrderResponseModel>> response = new Response<>(true, "Success", new PaginationBaseResponseModel<>());
        when(salesOrderDataAccessor.getSalesOrdersInBatches(any(GetSalesOrdersRequestModel.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<PaginationBaseResponseModel<SalesOrderResponseModel>>> responseEntity = salesOrderController.getSalesOrdersInBatches(getSalesOrdersRequestModel);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isNotNull();
    }

    @Test
    public void testInsertSalesOrder() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock the request data
        org.example.Models.CommunicationModels.CarrierModels.SalesOrder salesOrder = new org.example.Models.CommunicationModels.CarrierModels.SalesOrder();
        salesOrder.setTermsAndConditionsMarkdown("Terms and conditions Markdown content sales order 2");
        salesOrder.setTermsAndConditionsHtml("<p>Terms and conditions HTML content sales order 2</p>");
        salesOrder.setPurchaseOrderId(3);

        org.example.Models.CommunicationModels.CarrierModels.Address billingAddress = new org.example.Models.CommunicationModels.CarrierModels.Address();
        billingAddress.setLine1("123 Billing St");
        billingAddress.setLine2("Apt 101");
        billingAddress.setLandmark("Near Billing Landmark");
        billingAddress.setState("Tamil Nadu");
        billingAddress.setCity("Los Angeles");
        billingAddress.setZipCode("90001");
        billingAddress.setNameOnAddress("John Doe Billing");
        billingAddress.setPhoneOnAddress("1234567890");

        org.example.Models.CommunicationModels.CarrierModels.Address shippingAddress = new org.example.Models.CommunicationModels.CarrierModels.Address();
        shippingAddress.setLine1("456 Shipping St");
        shippingAddress.setLine2("Apt 202");
        shippingAddress.setLandmark("Near Shipping Landmark");
        shippingAddress.setState("Tamil Nadu");
        shippingAddress.setCity("New York City");
        shippingAddress.setZipCode("10001");
        shippingAddress.setNameOnAddress("John Doe Shipping");
        shippingAddress.setPhoneOnAddress("9876543210");

        org.example.Models.CommunicationModels.CarrierModels.PaymentInfo paymentInfo = new org.example.Models.CommunicationModels.CarrierModels.PaymentInfo();
        paymentInfo.setTotal(100.0);
        paymentInfo.setTax(10.0);
        paymentInfo.setServiceFee(5.0);
        paymentInfo.setDiscount(5.0);
        paymentInfo.setStatus(1);
        paymentInfo.setMode(2);
        paymentInfo.setSubTotal(80.0);
        paymentInfo.setDeliveryFee(10.0);
        paymentInfo.setPendingAmount(0.0);
        paymentInfo.setRazorpayTransactionId("trans123");
        paymentInfo.setRazorpayReceipt("receipt123");
        paymentInfo.setRazorpayOrderId("order123");
        paymentInfo.setRazorpayPaymentNotes("Payment notes");
        paymentInfo.setRazorpaySignature("signature123");

        List<org.example.Models.CommunicationModels.CarrierModels.SalesOrdersProductQuantityMap> salesOrdersProductQuantityMaps = new ArrayList<>();

        org.example.Models.CommunicationModels.CarrierModels.SalesOrdersProductQuantityMap map1 = new org.example.Models.CommunicationModels.CarrierModels.SalesOrdersProductQuantityMap();
        map1.setQuantity(5);
        map1.setPricePerQuantityPerProduct(8.0);
        map1.setProductId(1);
        salesOrdersProductQuantityMaps.add(map1);

        org.example.Models.CommunicationModels.CarrierModels.SalesOrdersProductQuantityMap map2 = new org.example.Models.CommunicationModels.CarrierModels.SalesOrdersProductQuantityMap();
        map2.setQuantity(5);
        map2.setPricePerQuantityPerProduct(8.0);
        map2.setProductId(2);
        salesOrdersProductQuantityMaps.add(map2);

        SalesOrderRequestModel salesOrderRequestModel = new SalesOrderRequestModel();
        salesOrderRequestModel.setSalesOrder(salesOrder);
        salesOrderRequestModel.setBillingAddress(billingAddress);
        salesOrderRequestModel.setShippingAddress(shippingAddress);
        salesOrderRequestModel.setPaymentInfo(paymentInfo);
        salesOrderRequestModel.setSalesOrdersProductQuantityMaps(salesOrdersProductQuantityMaps);

        // mock the data accessor
        Response<Long> response = new Response<>(true, "Success", 1L);
        when(salesOrderDataAccessor.createSalesOrder(any(SalesOrderRequestModel.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<Long>> responseEntity = salesOrderController.createSalesOrder(salesOrderRequestModel);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isEqualTo(1L);
    }

    @Test
    public void testUpdateSalesOrder() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock the request data
        org.example.Models.CommunicationModels.CarrierModels.SalesOrder salesOrder = new org.example.Models.CommunicationModels.CarrierModels.SalesOrder();
        salesOrder.setSalesOrderId(1L);
        salesOrder.setTermsAndConditionsMarkdown("Terms and conditions Markdown content sales order 2");
        salesOrder.setTermsAndConditionsHtml("<p>Terms and conditions HTML content sales order 2</p>");
        salesOrder.setPurchaseOrderId(3);

        org.example.Models.CommunicationModels.CarrierModels.Address billingAddress = new org.example.Models.CommunicationModels.CarrierModels.Address();
        billingAddress.setLine1("123 Billing St");
        billingAddress.setLine2("Apt 101");
        billingAddress.setLandmark("Near Billing Landmark");
        billingAddress.setState("Tamil Nadu");
        billingAddress.setCity("Los Angeles");
        billingAddress.setZipCode("90001");
        billingAddress.setNameOnAddress("John Doe Billing");
        billingAddress.setPhoneOnAddress("1234567890");

        org.example.Models.CommunicationModels.CarrierModels.Address shippingAddress = new org.example.Models.CommunicationModels.CarrierModels.Address();
        shippingAddress.setLine1("456 Shipping St");
        shippingAddress.setLine2("Apt 202");
        shippingAddress.setLandmark("Near Shipping Landmark");
        shippingAddress.setState("Tamil Nadu");
        shippingAddress.setCity("New York City");
        shippingAddress.setZipCode("10001");
        shippingAddress.setNameOnAddress("John Doe Shipping");
        shippingAddress.setPhoneOnAddress("9876543210");

        org.example.Models.CommunicationModels.CarrierModels.PaymentInfo paymentInfo = new org.example.Models.CommunicationModels.CarrierModels.PaymentInfo();
        paymentInfo.setTotal(100.0);
        paymentInfo.setTax(10.0);
        paymentInfo.setServiceFee(5.0);
        paymentInfo.setDiscount(5.0);
        paymentInfo.setStatus(1);
        paymentInfo.setMode(2);
        paymentInfo.setSubTotal(80.0);
        paymentInfo.setDeliveryFee(10.0);
        paymentInfo.setPendingAmount(0.0);
        paymentInfo.setRazorpayTransactionId("trans123");
        paymentInfo.setRazorpayReceipt("receipt123");
        paymentInfo.setRazorpayOrderId("order123");
        paymentInfo.setRazorpayPaymentNotes("Payment notes");
        paymentInfo.setRazorpaySignature("signature123");

        List<org.example.Models.CommunicationModels.CarrierModels.SalesOrdersProductQuantityMap> salesOrdersProductQuantityMaps = new ArrayList<>();

        org.example.Models.CommunicationModels.CarrierModels.SalesOrdersProductQuantityMap map1 = new org.example.Models.CommunicationModels.CarrierModels.SalesOrdersProductQuantityMap();
        map1.setQuantity(5);
        map1.setPricePerQuantityPerProduct(8.0);
        map1.setProductId(1);
        salesOrdersProductQuantityMaps.add(map1);

        org.example.Models.CommunicationModels.CarrierModels.SalesOrdersProductQuantityMap map2 = new org.example.Models.CommunicationModels.CarrierModels.SalesOrdersProductQuantityMap();
        map2.setQuantity(5);
        map2.setPricePerQuantityPerProduct(8.0);
        map2.setProductId(2);
        salesOrdersProductQuantityMaps.add(map2);

        SalesOrderRequestModel salesOrderRequestModel = new SalesOrderRequestModel();
        salesOrderRequestModel.setSalesOrder(salesOrder);
        salesOrderRequestModel.setBillingAddress(billingAddress);
        salesOrderRequestModel.setShippingAddress(shippingAddress);
        salesOrderRequestModel.setPaymentInfo(paymentInfo);
        salesOrderRequestModel.setSalesOrdersProductQuantityMaps(salesOrdersProductQuantityMaps);

        // mock the data accessor
        Response<Long> response = new Response<>(true, "Success", 1L);
        when(salesOrderDataAccessor.updateSalesOrder(any(SalesOrderRequestModel.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<Long>> responseEntity = salesOrderController.updateSalesOrder(salesOrderRequestModel);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isEqualTo(1L);
    }

    @Test
    public void testGetSalesOrderDetailsById() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock the request data
        long id = 1L;

        // mock the data accessor
        Response<SalesOrderResponseModel> response = new Response<>(true, "Success", new SalesOrderResponseModel());
        when(salesOrderDataAccessor.getSalesOrderDetailsById(any(Long.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<SalesOrderResponseModel>> responseEntity = salesOrderController.getSalesOrderDetailsById(id);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isNotNull();
    }

    @Test
    public void testToggleSalesOrder() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock the request data
        long id = 1L;

        // mock the data accessor
        Response<Boolean> response = new Response<>(true, "Success", true);
        when(salesOrderDataAccessor.toggleSalesOrder(any(Long.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<Boolean>> responseEntity = salesOrderController.toggleSalesOrder(id);
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
        Response<byte[]> response = new Response<>(true, "Success", new byte[100]);
        when(salesOrderDataAccessor.getSalesOrderPDF(any(Long.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<byte[]>> responseEntity = salesOrderController.getSalesOrderPDF(id);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isNotNull();
    }
}