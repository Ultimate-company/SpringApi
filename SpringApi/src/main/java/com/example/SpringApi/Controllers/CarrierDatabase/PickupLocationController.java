package com.example.SpringApi.Controllers.CarrierDatabase;

import com.example.SpringApi.Services.CarrierDatabase.PickupLocationDataAccessor;
import org.example.ApiRoutes;
import org.example.Models.Authorizations;
import org.example.Models.CommunicationModels.CarrierModels.PickupLocation;
import org.example.Models.RequestModels.ApiRequestModels.PickupLocationRequestModel;
import org.example.Models.RequestModels.GridRequestModels.PaginationBaseRequestModel;
import org.example.Models.ResponseModels.ApiResponseModels.PaginationBaseResponseModel;
import org.example.Models.ResponseModels.ApiResponseModels.PickupLocationResponseModel;
import org.example.Models.ResponseModels.Response;
import org.example.Translators.CarrierDatabaseTranslators.Interfaces.IPickupLocationSubTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/" + ApiRoutes.ApiControllerNames.PICKUP_LOCATION + "/")
public class PickupLocationController {

    private final IPickupLocationSubTranslator accessor;
    @Autowired
    public PickupLocationController(PickupLocationDataAccessor accessor) {
        this.accessor = accessor;
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_PICKUP_LOCATIONS_PERMISSION +"')")
    @PostMapping(ApiRoutes.PickupLocationsSubRoute.GET_PICKUP_LOCATIONS_IN_BATCHES)
    public ResponseEntity<Response<PaginationBaseResponseModel<PickupLocationResponseModel>>> getPickupLocationInBatches(@RequestBody PaginationBaseRequestModel paginationBaseRequestModel) {
        return ResponseEntity.ok(accessor.getPickupLocationsInBatches(paginationBaseRequestModel));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.INSERT_PICKUP_LOCATIONS_PERMISSION +"')")
    @PutMapping(ApiRoutes.PickupLocationsSubRoute.CREATE_PICKUP_LOCATION)
    public ResponseEntity<Response<Long>> createPickupLocation(@RequestBody PickupLocationRequestModel pickupLocationRequestModel) throws Exception {
        return ResponseEntity.ok(accessor.createPickupLocation(pickupLocationRequestModel));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_PICKUP_LOCATIONS_PERMISSION +"')")
    @GetMapping(ApiRoutes.PickupLocationsSubRoute.GET_ALL_PICKUP_LOCATIONS)
    public ResponseEntity<Response<List<PickupLocation>>> getAllPickupLocations(@RequestParam boolean includeDeleted) {
        return ResponseEntity.ok(accessor.getAllPickupLocations(includeDeleted));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.UPDATE_PICKUP_LOCATIONS_PERMISSION +"')")
    @PostMapping(ApiRoutes.PickupLocationsSubRoute.UPDATE_PICKUP_LOCATION)
    public ResponseEntity<Response<Long>> updatePickupLocation(@RequestBody PickupLocationRequestModel pickupLocationRequestModel) throws Exception {
        return ResponseEntity.ok(accessor.updatePickupLocation(pickupLocationRequestModel));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_PICKUP_LOCATIONS_PERMISSION +"')")
    @GetMapping(ApiRoutes.PickupLocationsSubRoute.GET_PICKUP_LOCATION_BY_ID)
    public ResponseEntity<Response<PickupLocationResponseModel>> getPickupLocationById(@RequestParam long id) {
        return ResponseEntity.ok(accessor.getPickupLocationById(id));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.DELETE_PICKUP_LOCATIONS_PERMISSION +"')")
    @DeleteMapping(ApiRoutes.PickupLocationsSubRoute.TOGGLE_PICKUP_LOCATION)
    public ResponseEntity<Response<Boolean>> togglePickupLocation(@RequestParam long id) {
        return ResponseEntity.ok(accessor.togglePickupLocation(id));
    }
}