package com.example.SpringApi.Controllers.CarrierDatabase;

import com.example.SpringApi.Services.CarrierDatabase.WebTemplatesDataAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.ApiRoutes;
import org.example.Models.Authorizations;
import org.example.Models.RequestModels.ApiRequestModels.WebTemplateRequestModel;
import org.example.Models.RequestModels.GridRequestModels.PaginationBaseRequestModel;
import org.example.Models.ResponseModels.ApiResponseModels.PaginationBaseResponseModel;
import org.example.Models.ResponseModels.ApiResponseModels.WebTemplateResponseModel;
import org.example.Models.ResponseModels.Response;
import org.example.Translators.CarrierDatabaseTranslators.Interfaces.IWebTemplateSubTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/" + ApiRoutes.ApiControllerNames.WEB_TEMPLATE + "/")
public class WebTemplatesController {
    private final IWebTemplateSubTranslator accessor;
    @Autowired
    public WebTemplatesController(WebTemplatesDataAccessor accessor) {
        this.accessor = accessor;
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_WEB_TEMPLATE_PERMISSION +"')")
    @PostMapping(ApiRoutes.WebTemplateSubRoute.GET_WEB_TEMPLATES_IN_BATCHES)
    public ResponseEntity<Response<PaginationBaseResponseModel<WebTemplateResponseModel>>> getWebTemplatesInBatches(@RequestBody PaginationBaseRequestModel paginationBaseRequestModel) {
        return ResponseEntity.ok(accessor.getWebTemplatesInBatches(paginationBaseRequestModel));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.INSERT_WEB_TEMPLATE_PERMISSION +"')")
    @PutMapping(ApiRoutes.WebTemplateSubRoute.INSERT_WEB_TEMPLATE)
    public ResponseEntity<Response<Long>> insertWebTemplate(@RequestBody WebTemplateRequestModel webTemplateRequestModel) throws JsonProcessingException {
        return ResponseEntity.ok(accessor.insertWebTemplate(webTemplateRequestModel));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.TOGGLE_WEB_TEMPLATE_PERMISSION +"')")
    @DeleteMapping(ApiRoutes.WebTemplateSubRoute.TOGGLE_WEB_TEMPLATE)
    public ResponseEntity<Response<Boolean>> toggleWebTemplate(@RequestParam long id) {
        return ResponseEntity.ok(accessor.toggleWebTemplate(id));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.DEPLOY_WEB_TEMPLATE_PERMISSION +"')")
    @PostMapping(ApiRoutes.WebTemplateSubRoute.DEPLOY_WEB_TEMPLATE)
    public ResponseEntity<Response<Boolean>> deployWebTemplate(@RequestParam long id) {
        return ResponseEntity.ok(accessor.deployWebTemplate(id));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.UPDATE_WEB_TEMPLATE_PERMISSION +"')")
    @PostMapping(ApiRoutes.WebTemplateSubRoute.UPDATE_WEB_TEMPLATE)
    public ResponseEntity<Response<Long>> updateWebTemplate(@RequestBody WebTemplateRequestModel webTemplateRequestModel) throws JsonProcessingException {
        return ResponseEntity.ok(accessor.updateWebTemplate(webTemplateRequestModel));
    }

    // public endpoints
    @PreAuthorize("@customAuthorization.validateToken()")
    @PostMapping(ApiRoutes.WebTemplateSubRoute.UPDATE_USER_CART)
    public ResponseEntity<Response<Long>> updateUserCart(@RequestBody WebTemplateRequestModel webTemplateRequestModel) {
        return ResponseEntity.ok(accessor.updateUserCart(webTemplateRequestModel));
    }

    @PreAuthorize("@customAuthorization.validateToken()")
    @PostMapping(ApiRoutes.WebTemplateSubRoute.UPDATE_USER_LIKED_ITEMS)
    public ResponseEntity<Response<Long>> updateUserLikedItems(@RequestBody WebTemplateRequestModel webTemplateRequestModel) {
        return ResponseEntity.ok(accessor.updateUserLikedItems(webTemplateRequestModel));
    }

    @PreAuthorize("@customAuthorization.validatePublicEndpoint()")
    @GetMapping(ApiRoutes.WebTemplateSubRoute.GET_WEB_TEMPLATE_BY_ID)
    public ResponseEntity<Response<WebTemplateResponseModel>> getWebTemplateById(@RequestParam long id) {
        return ResponseEntity.ok(accessor.getWebTemplateById(id));
    }
}