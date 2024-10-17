package com.example.SpringApi.Controllers.CentralDatabase;

import com.example.SpringApi.Services.CentralDatabase.CarrierDataAccessor;
import org.example.ApiRoutes;
import org.example.Models.Authorizations;
import org.example.Models.CommunicationModels.CentralModels.Carrier;
import org.example.Models.CommunicationModels.CentralModels.WebTemplateCarrierMapping;
import org.example.Models.RequestModels.GridRequestModels.GetCarriersRequestModel;
import org.example.Models.ResponseModels.ApiResponseModels.CarrierByWebTemplateWildCardResponse;
import org.example.Models.ResponseModels.ApiResponseModels.PaginationBaseResponseModel;
import org.example.Models.ResponseModels.Response;
import org.example.Translators.CentralDatabaseTranslators.Interfaces.ICarrierSubTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/" + ApiRoutes.ApiControllerNames.CARRIER + "/")
public class CarrierController {
    private final ICarrierSubTranslator accessor;

    @Autowired
    public CarrierController(CarrierDataAccessor accessor) {
        this.accessor = accessor;
    }

    @PreAuthorize("@customAuthorization.validateToken()")
    @GetMapping(ApiRoutes.CarriersSubRoute.GET_CARRIER_DETAILS_BY_ID)
    public ResponseEntity<Response<Carrier>> getCarrierDetailsById(long carrierId) {
        return ResponseEntity.ok(accessor.getCarrierDetailsById(carrierId));
    }

    @PreAuthorize("@customAuthorization.validateToken()")
    @GetMapping(ApiRoutes.CarriersSubRoute.IS_USER_MAPPED_TO_CARRIER)
    public ResponseEntity<Response<Boolean>> isUserMappedToCarrier() {
        return ResponseEntity.ok(accessor.isUserMappedToCarrier());
    }

    @PreAuthorize("@customAuthorization.validateToken()")
    @PostMapping(ApiRoutes.CarriersSubRoute.GET_CARRIER_IN_BATCHES)
    public ResponseEntity<Response<PaginationBaseResponseModel<Carrier>>> getCarrierInBatches(@RequestBody GetCarriersRequestModel getCarriersRequestModel) {
        return ResponseEntity.ok(accessor.getCarriersInBatches(getCarriersRequestModel));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.UPDATE_API_KEYS_PERMISSION +"')")
    @PostMapping(ApiRoutes.CarriersSubRoute.UPDATE_API_KEYS)
    public ResponseEntity<Response<Boolean>> updateApiKeys(@RequestBody Carrier carrier) {
        return ResponseEntity.ok(accessor.updateApiKeys(carrier));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_API_KEYS_PERMISSION +"')")
    @GetMapping(ApiRoutes.CarriersSubRoute.GET_API_KEYS)
    public ResponseEntity<Response<Carrier>> getApiKeys() {
        return ResponseEntity.ok(accessor.getApiKeys());
    }

    // public endpoint
    @GetMapping(ApiRoutes.CarriersSubRoute.GET_CARRIER_ID_BY_WEB_TEMPLATE_WILDCARD)
    public ResponseEntity<Response<CarrierByWebTemplateWildCardResponse>> getCarrierIdByWebTemplateWildCard(@RequestParam String wildCard){
        return ResponseEntity.ok(accessor.getCarrierByWebTemplateWildCard(wildCard));
    }

    @PostMapping(ApiRoutes.CarriersSubRoute.GET_TOKEN_FOR_WEBTEMPLATE)
    public ResponseEntity<Response<String>> getTokenForWebTemplate(@RequestBody WebTemplateCarrierMapping webTemplateCarrierMapping){
        return ResponseEntity.ok(accessor.getTokenForWebTemplate(webTemplateCarrierMapping));
    }
}
