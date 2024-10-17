package com.example.SpringApi.Controllers.CarrierDatabase;

import com.example.SpringApi.Services.CarrierDatabase.LeadDataAccessor;
import org.example.ApiRoutes;
import org.example.Models.Authorizations;
import org.example.Models.RequestModels.ApiRequestModels.LeadRequestModel;
import org.example.Models.RequestModels.GridRequestModels.PaginationBaseRequestModel;
import org.example.Models.ResponseModels.ApiResponseModels.LeadResponseModel;
import org.example.Models.ResponseModels.ApiResponseModels.PaginationBaseResponseModel;
import org.example.Models.ResponseModels.Response;
import org.example.Translators.CarrierDatabaseTranslators.Interfaces.ILeadSubTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/" + ApiRoutes.ApiControllerNames.LEAD + "/")
public class
LeadController {

    private final ILeadSubTranslator accessor;
    @Autowired
    public LeadController(LeadDataAccessor accessor) {
        this.accessor = accessor;
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_LEADS_PERMISSION +"')")
    @PostMapping(ApiRoutes.LeadsSubRoute.GET_LEADS_IN_BATCHES)
    public ResponseEntity<Response<PaginationBaseResponseModel<LeadResponseModel>>> getLeadsInBatches(@RequestBody PaginationBaseRequestModel paginationBaseRequestModel) {
        return ResponseEntity.ok(accessor.getLeadsInBatches(paginationBaseRequestModel));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.INSERT_LEADS_PERMISSION +"')")
    @PutMapping(ApiRoutes.LeadsSubRoute.CREATE_LEAD)
    public ResponseEntity<Response<Long>> insertLead(@RequestBody LeadRequestModel leadRequestModel) {
        return ResponseEntity.ok(accessor.createLead(leadRequestModel));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.UPDATE_LEADS_PERMISSION +"')")
    @PostMapping(ApiRoutes.LeadsSubRoute.UPDATE_LEAD)
    public ResponseEntity<Response<Long>> updateLead(@RequestBody LeadRequestModel leadRequestModel) {
        return ResponseEntity.ok(accessor.updateLead(leadRequestModel));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_LEADS_PERMISSION +"')")
    @GetMapping(ApiRoutes.LeadsSubRoute.GET_LEAD_DETAILS_BY_ID)
    public ResponseEntity<Response<LeadResponseModel>> getLeadDetailsById(@RequestParam long id) {
        return ResponseEntity.ok(accessor.getLeadDetailsById(id));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.TOGGLE_LEADS_PERMISSION +"')")
    @DeleteMapping(ApiRoutes.LeadsSubRoute.TOGGLE_LEAD)
    public ResponseEntity<Response<Boolean>> toggleLead(@RequestParam long id) {
        return ResponseEntity.ok(accessor.toggleLead(id));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_LEADS_PERMISSION +"')")
    @GetMapping(ApiRoutes.LeadsSubRoute.GET_LEAD_DETAILS_BY_EMAIL)
    public ResponseEntity<Response<LeadResponseModel>> getLeadDetailsByEmail(@RequestParam String email) {
        return ResponseEntity.ok(accessor.getLeadDetailsByEmail(email));
    }
}