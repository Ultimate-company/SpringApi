package com.example.SpringApi.Controllers.CarrierDatabase;

import com.example.SpringApi.Services.CarrierDatabase.PurchaseOrderDataAccessor;
import com.itextpdf.text.DocumentException;
import freemarker.template.TemplateException;
import org.example.ApiRoutes;
import org.example.Models.Authorizations;
import org.example.Models.RequestModels.ApiRequestModels.PurchaseOrderRequestModel;
import org.example.Models.RequestModels.GridRequestModels.PaginationBaseRequestModel;
import org.example.Models.ResponseModels.ApiResponseModels.PaginationBaseResponseModel;
import org.example.Models.ResponseModels.ApiResponseModels.PurchaseOrderResponseModel;
import org.example.Models.ResponseModels.Response;
import org.example.Translators.CarrierDatabaseTranslators.Interfaces.IPurchaseOrderSubTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/" + ApiRoutes.ApiControllerNames.PURCHASE_ORDER + "/")
public class PurchaseOrderController {

    private final IPurchaseOrderSubTranslator accessor;
    @Autowired
    public PurchaseOrderController(PurchaseOrderDataAccessor accessor) {
        this.accessor = accessor;
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_PURCHASE_ORDERS_PERMISSION +"')")
    @PostMapping(ApiRoutes.PurchaseOrderSubRoute.GET_PURCHASE_ORDERS_IN_BATCHES)
    public ResponseEntity<Response<PaginationBaseResponseModel<PurchaseOrderResponseModel>>> getPurchaseOrdersInBatches(@RequestBody PaginationBaseRequestModel paginationBaseRequestModel) {
        return ResponseEntity.ok(accessor.getPurchaseOrdersInBatches(paginationBaseRequestModel));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.INSERT_PURCHASE_ORDERS_PERMISSION +"')")
    @PutMapping(ApiRoutes.PurchaseOrderSubRoute.CREATE_PURCHASE_ORDER)
    public ResponseEntity<Response<Long>> createPurchaseOrder(@RequestBody PurchaseOrderRequestModel purchaseOrderRequestModel) {
        return ResponseEntity.ok(accessor.createPurchaseOrder(purchaseOrderRequestModel));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.UPDATE_PURCHASE_ORDERS_PERMISSION +"')")
    @PostMapping(ApiRoutes.PurchaseOrderSubRoute.UPDATE_PURCHASE_ORDER)
    public ResponseEntity<Response<Long>> updatePurchaseOrder(@RequestBody PurchaseOrderRequestModel purchaseOrderRequestModel) {
        return ResponseEntity.ok(accessor.updatePurchaseOrder(purchaseOrderRequestModel));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_PURCHASE_ORDERS_PERMISSION +"')")
    @GetMapping(ApiRoutes.PurchaseOrderSubRoute.GET_PURCHASE_ORDER_BY_ID)
    public ResponseEntity<Response<PurchaseOrderResponseModel>> getPurchaseOrderDetailsById(@RequestParam long id) {
        return ResponseEntity.ok(accessor.getPurchaseOrderDetailsById(id));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.TOGGLE_PURCHASE_ORDERS_PERMISSION +"')")
    @DeleteMapping(ApiRoutes.PurchaseOrderSubRoute.TOGGLE_PURCHASE_ORDER)
    public ResponseEntity<Response<Boolean>> togglePurchaseOrder(@RequestParam long id) {
        return ResponseEntity.ok(accessor.togglePurchaseOrder(id));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.UPDATE_PURCHASE_ORDERS_PERMISSION +"')")
    @DeleteMapping(ApiRoutes.PurchaseOrderSubRoute.APPROVED_BY_PURCHASE_ORDER)
    public ResponseEntity<Response<Boolean>> approvedByPurchaseOrder(@RequestParam long id) {
        return ResponseEntity.ok(accessor.approvedByPurchaseOrder(id));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_PURCHASE_ORDERS_PERMISSION +"')")
    @GetMapping(ApiRoutes.PurchaseOrderSubRoute.GET_PURCHASE_ORDER_PDF)
    public ResponseEntity<Response<String>> getPurchaseOrderPDF(@RequestParam long id) throws TemplateException, IOException, DocumentException {
        return ResponseEntity.ok(accessor.getPurchaseOrderPDF(id));
    }
}