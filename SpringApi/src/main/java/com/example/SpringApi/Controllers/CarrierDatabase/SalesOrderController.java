package com.example.SpringApi.Controllers.CarrierDatabase;

import com.example.SpringApi.Services.CarrierDatabase.SalesOrderDataAccessor;
import com.itextpdf.text.DocumentException;
import freemarker.template.TemplateException;
import org.example.ApiRoutes;
import org.example.Models.Authorizations;
import org.example.Models.CommunicationModels.CarrierModels.Address;
import org.example.Models.RequestModels.ApiRequestModels.SalesOrderRequestModel;
import org.example.Models.RequestModels.GridRequestModels.GetSalesOrdersRequestModel;
import org.example.Models.ResponseModels.ApiResponseModels.PaginationBaseResponseModel;
import org.example.Models.ResponseModels.ApiResponseModels.SalesOrderResponseModel;
import org.example.Models.ResponseModels.Response;
import org.example.Translators.CarrierDatabaseTranslators.Interfaces.ISalesOrderSubTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/" + ApiRoutes.ApiControllerNames.SALES_ORDER + "/")
public class SalesOrderController {

    private final ISalesOrderSubTranslator accessor;
    @Autowired
    public SalesOrderController(SalesOrderDataAccessor accessor) {
        this.accessor = accessor;
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_SALES_ORDERS_PERMISSION +"')")
    @PostMapping(ApiRoutes.SalesOrderSubRoute.GET_SALES_ORDERS_IN_BATCHES)
    public ResponseEntity<Response<PaginationBaseResponseModel<SalesOrderResponseModel>>> getSalesOrdersInBatches(@RequestBody GetSalesOrdersRequestModel getSalesOrdersRequestModel) {
        return ResponseEntity.ok(accessor.getSalesOrdersInBatches(getSalesOrdersRequestModel));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.INSERT_SALES_ORDERS_PERMISSION +"')")
    @PutMapping(ApiRoutes.SalesOrderSubRoute.CREATE_SALES_ORDER)
    public ResponseEntity<Response<Long>> createSalesOrder(@RequestBody SalesOrderRequestModel salesOrderRequestModel) throws Exception {
        return ResponseEntity.ok(accessor.createSalesOrder(salesOrderRequestModel));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.UPDATE_SALES_ORDERS_PERMISSION +"')")
    @PostMapping(ApiRoutes.SalesOrderSubRoute.UPDATE_SALES_ORDER)
    public ResponseEntity<Response<Long>> updateSalesOrder(@RequestBody SalesOrderRequestModel salesOrderRequestModel) {
        return ResponseEntity.ok(accessor.updateSalesOrder(salesOrderRequestModel));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_SALES_ORDERS_PERMISSION +"')")
    @GetMapping(ApiRoutes.SalesOrderSubRoute.GET_SALES_ORDER_BY_ID)
    public ResponseEntity<Response<SalesOrderResponseModel>> getSalesOrderDetailsById(@RequestParam long id) {
        return ResponseEntity.ok(accessor.getSalesOrderDetailsById(id));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.TOGGLE_SALES_ORDERS_PERMISSION +"')")
    @DeleteMapping(ApiRoutes.SalesOrderSubRoute.TOGGLE_SALES_ORDER)
    public ResponseEntity<Response<Boolean>> toggleSalesOrder(@RequestParam long id) {
        return ResponseEntity.ok(accessor.toggleSalesOrder(id));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_SALES_ORDERS_PERMISSION +"')")
    @GetMapping(ApiRoutes.SalesOrderSubRoute.GET_SALES_ORDER_PDF)
    public ResponseEntity<Response<byte[]>> getSalesOrderPDF(@RequestParam long id) throws TemplateException, IOException, DocumentException {
        return ResponseEntity.ok(accessor.getSalesOrderPDF(id));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.UPDATE_SALES_ORDERS_PERMISSION +"')")
    @DeleteMapping(ApiRoutes.SalesOrderSubRoute.CANCEL_SALES_ORDER)
    public ResponseEntity<Response<Boolean>> cancelSalesOrder(@RequestParam long id) {
        return ResponseEntity.ok(accessor.cancelSalesOrder(id));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.UPDATE_SALES_ORDERS_PERMISSION +"')")
    @PostMapping(ApiRoutes.SalesOrderSubRoute.UPDATE_SALES_ORDER_PICKUP_ADDRESS)
    public ResponseEntity<Response<Boolean>> updateSalesOrderPickupAddress(@RequestParam long id, @RequestParam long shipRocketOrderId, @RequestParam long pickupLocationId) {
        return ResponseEntity.ok(accessor.updateSalesOrderPickupAddress(id, shipRocketOrderId, pickupLocationId));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.UPDATE_SALES_ORDERS_PERMISSION +"')")
    @PostMapping(ApiRoutes.SalesOrderSubRoute.UPDATE_CUSTOMER_DELIVERY_ADDRESS)
    public ResponseEntity<Response<Boolean>> updateCustomerDeliveryAddress(@RequestParam long id, @RequestBody Address address) {
        return ResponseEntity.ok(accessor.updateCustomerDeliveryAddress(id, address));
    }
}
