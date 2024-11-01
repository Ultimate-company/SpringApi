package com.example.SpringApi.Controllers.CarrierDatabase;

import com.example.SpringApi.Services.CarrierDatabase.GridDataAccessor;
import org.example.ApiRoutes;
import org.example.Models.CommunicationModels.CarrierModels.UserGridPreference;
import org.example.Models.RequestModels.ApiRequestModels.GridPreferenceRequestModel;
import org.example.Models.ResponseModels.Response;
import org.example.Translators.CarrierDatabaseTranslators.Interfaces.IGridSubTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/" + ApiRoutes.ApiControllerNames.GRID + "/")
public class GridController {
    private final IGridSubTranslator accessor;

    @Autowired
    public GridController(GridDataAccessor accessor) {
        this.accessor = accessor;
    }

    @PreAuthorize("@customAuthorization.validateToken()")
    @PostMapping(ApiRoutes.GridSubRoute.UPDATE_GRID_VISIBILITY_PREFERENCE)
    public ResponseEntity<Response<Boolean>> updateGridVisibilityPreference(@RequestBody GridPreferenceRequestModel gridPreferenceRequestModel) {
        return ResponseEntity.ok(accessor.updateGridVisibilityPreference(gridPreferenceRequestModel));
    }

    @PreAuthorize("@customAuthorization.validateToken()")
    @PostMapping(ApiRoutes.GridSubRoute.UPDATE_GRID_DENSITY_PREFERENCE)
    public ResponseEntity<Response<Boolean>> updateGridDensityVisibilityPreference(@RequestBody GridPreferenceRequestModel gridPreferenceRequestModel) {
        return ResponseEntity.ok(accessor.updateGridDensityVisibilityPreference(gridPreferenceRequestModel));
    }

    @PreAuthorize("@customAuthorization.validateToken()")
    @PostMapping(ApiRoutes.GridSubRoute.UPDATE_ROWS_PER_PAGE_PREFERENCE)
    public ResponseEntity<Response<Boolean>> updateRowsPerPagePreference(@RequestBody GridPreferenceRequestModel gridPreferenceRequestModel) {
        return ResponseEntity.ok(accessor.updateRowsPerPagePreference(gridPreferenceRequestModel));
    }

    @PreAuthorize("@customAuthorization.validateToken()")
    @GetMapping(ApiRoutes.GridSubRoute.GET_GRID_VISIBILITY_PREFERENCE)
    public ResponseEntity<Response<UserGridPreference>> getGridVisibilityPreference(@RequestParam int gridId) {
        return ResponseEntity.ok(accessor.getGridVisibilityPreference(gridId));
    }
}