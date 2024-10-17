package com.example.SpringApi.Controllers.CarrierDatabase;

import com.example.SpringApi.Services.CarrierDatabase.AddressDataAccessor;
import org.example.ApiRoutes;
import org.example.Models.Authorizations;
import org.example.Models.CommunicationModels.CarrierModels.Address;
import org.example.Models.RequestModels.ApiRequestModels.BaseRequest;
import org.example.Models.ResponseModels.Response;
import org.example.Translators.CarrierDatabaseTranslators.Interfaces.IAddressSubTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/" + ApiRoutes.ApiControllerNames.ADDRESS + "/")
public class AddressController {
    private final IAddressSubTranslator accessor;
    @Autowired
    public AddressController(AddressDataAccessor accessor) {
        this.accessor = accessor;
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.DELETE_ADDRESS_PERMISSION +"')")
    @DeleteMapping(ApiRoutes.AddressSubRoute.TOGGLE_ADDRESS)
    public ResponseEntity<Response<Boolean>> toggleAddress(@RequestBody BaseRequest baseRequest) {
        return ResponseEntity.ok(accessor.toggleAddress(baseRequest.getId()));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_ADDRESS_PERMISSION +"')")
    @GetMapping(ApiRoutes.AddressSubRoute.GET_ADDRESS_BY_ID)
    public ResponseEntity<Response<Address>> getAddressById(@RequestBody BaseRequest baseRequest) throws Exception {
        return ResponseEntity.ok(accessor.getAddressById(baseRequest.getId()));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.INSERT_ADDRESS_PERMISSION +"')")
    @PutMapping(ApiRoutes.AddressSubRoute.INSERT_ADDRESS)
    public ResponseEntity<Response<Long>> insertAddress(@RequestBody Address address) {
        return ResponseEntity.ok(accessor.insertAddress(address));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.UPDATE_ADDRESS_PERMISSION +"')")
    @PostMapping(ApiRoutes.AddressSubRoute.UPDATE_ADDRESS)
    public ResponseEntity<Response<Long>> updateAddress(@RequestBody Address address) {
        return ResponseEntity.ok(accessor.updateAddress(address));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_ADDRESS_PERMISSION +"')")
    @GetMapping(ApiRoutes.AddressSubRoute.GET_ADDRESS_BY_USER_ID)
    public ResponseEntity<Response<Address>> getAddressByUserId(@RequestParam long id) {
        return ResponseEntity.ok(accessor.getAddressByUserId(id));
    }
}
