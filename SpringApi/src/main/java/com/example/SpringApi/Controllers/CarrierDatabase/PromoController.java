package com.example.SpringApi.Controllers.CarrierDatabase;

import com.example.SpringApi.Services.CarrierDatabase.PromoDataAccessor;
import org.example.ApiRoutes;
import org.example.Models.Authorizations;
import org.example.Models.CommunicationModels.CarrierModels.Promo;
import org.example.Models.RequestModels.GridRequestModels.PaginationBaseRequestModel;
import org.example.Models.ResponseModels.ApiResponseModels.PaginationBaseResponseModel;
import org.example.Models.ResponseModels.Response;
import org.example.Translators.CarrierDatabaseTranslators.Interfaces.IPromoSubTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/" + ApiRoutes.ApiControllerNames.PROMO + "/")
public class PromoController {

    private final IPromoSubTranslator accessor;
    @Autowired
    public PromoController(PromoDataAccessor accessor) {
        this.accessor = accessor;
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_PROMOS_PERMISSION +"')")
    @PostMapping(ApiRoutes.PromosSubRoute.GET_PROMOS_IN_BATCHES)
    public ResponseEntity<Response<PaginationBaseResponseModel<Promo>>> getPromosInBatches(@RequestBody PaginationBaseRequestModel paginationBaseRequestModel) {
        return ResponseEntity.ok(accessor.getPromosInBatches(paginationBaseRequestModel));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.INSERT_PROMOS_PERMISSION +"')")
    @PutMapping(ApiRoutes.PromosSubRoute.CREATE_PROMO)
    public ResponseEntity<Response<Long>> addPromo(@RequestBody Promo promo) {
        return ResponseEntity.ok(accessor.createPromo(promo));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_PROMOS_PERMISSION +"')")
    @GetMapping(ApiRoutes.PromosSubRoute.GET_PROMO_DETAILS_BY_ID)
    public ResponseEntity<Response<Promo>> getPromoDetailsById(@RequestParam long id) {
        return ResponseEntity.ok(accessor.getPromoDetailsById(id));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.DELETE_PROMOS_PERMISSION +"')")
    @DeleteMapping(ApiRoutes.PromosSubRoute.TOGGLE_PROMO)
    public ResponseEntity<Response<Boolean>> togglePromo(@RequestParam long id) {
        return ResponseEntity.ok(accessor.togglePromo(id));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_PROMOS_PERMISSION +"')")
    @GetMapping(ApiRoutes.PromosSubRoute.GET_PROMO_DETAILS_BY_NAME)
    public ResponseEntity<Response<Promo>> getPromoDetailsByName(@RequestParam String promoCode) {
        return ResponseEntity.ok(accessor.getPromoDetailsByName(promoCode));
    }
}