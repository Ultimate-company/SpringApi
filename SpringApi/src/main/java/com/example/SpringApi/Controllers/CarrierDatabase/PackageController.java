package com.example.SpringApi.Controllers.CarrierDatabase;

import com.example.SpringApi.Services.CarrierDatabase.PackageDataAccessor;
import org.example.ApiRoutes;
import org.example.Models.Authorizations;
import org.example.Models.CommunicationModels.CarrierModels.Package;
import org.example.Models.RequestModels.GridRequestModels.PaginationBaseRequestModel;
import org.example.Models.ResponseModels.ApiResponseModels.PaginationBaseResponseModel;
import org.example.Models.ResponseModels.Response;
import org.example.Translators.CarrierDatabaseTranslators.Interfaces.IPackageSubTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/" + ApiRoutes.ApiControllerNames.PACKAGE + "/")
public class PackageController {
    private final IPackageSubTranslator accessor;
    @Autowired
    public PackageController(PackageDataAccessor accessor) {
        this.accessor = accessor;
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_PACKAGES_PERMISSION +"')")
    @PostMapping(ApiRoutes.PackageSubRoute.GET_PACKAGES_IN_BATCHES)
    public ResponseEntity<Response<PaginationBaseResponseModel<Package>>> getPackagesInBatches(@RequestBody PaginationBaseRequestModel paginationBaseRequestModel) {
        return ResponseEntity.ok(accessor.getPackagesInBatches(paginationBaseRequestModel));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_PACKAGES_PERMISSION +"')")
    @GetMapping(ApiRoutes.PackageSubRoute.GET_PACKAGE_BY_ID)
    public ResponseEntity<Response<Package>> getPackageById(@RequestParam long id) {
        return ResponseEntity.ok(accessor.getPackageById(id));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_PACKAGES_PERMISSION +"')")
    @GetMapping(ApiRoutes.PackageSubRoute.GET_ALL_PACKAGES_IN_SYSTEM)
    public ResponseEntity<Response<List<Package>>> getPackageById() {
        return ResponseEntity.ok(accessor.getAllPackagesInSystem());
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.TOGGLE_PACKAGES_PERMISSION +"')")
    @DeleteMapping(ApiRoutes.PackageSubRoute.TOGGLE_PACKAGE)
    public ResponseEntity<Response<Boolean>> togglePackage(@RequestParam long id) {
        return ResponseEntity.ok(accessor.togglePackage(id));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.UPDATE_PACKAGES_PERMISSION +"')")
    @PostMapping(ApiRoutes.PackageSubRoute.UPDATE_PACKAGE)
    public ResponseEntity<Response<Long>> updatePackage(@RequestBody Package _package) {
        return ResponseEntity.ok(accessor.updatePackage(_package));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.INSERT_PACKAGES_PERMISSION +"')")
    @PutMapping(ApiRoutes.PackageSubRoute.CREATE_PACKAGE)
    public ResponseEntity<Response<Long>> createPackage(@RequestBody Package _package) {
        return ResponseEntity.ok(accessor.createPackage(_package));
    }
}