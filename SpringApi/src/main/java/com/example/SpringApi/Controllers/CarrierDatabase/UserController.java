package com.example.SpringApi.Controllers.CarrierDatabase;

import com.example.SpringApi.Services.CentralDatabase.UserDataAccessor;
import org.example.ApiRoutes;
import org.example.Models.Authorizations;
import org.example.Models.CommunicationModels.CarrierModels.Permissions;
import org.example.Models.CommunicationModels.CentralModels.User;
import org.example.Models.RequestModels.ApiRequestModels.ImportUsersRequestModel;
import org.example.Models.RequestModels.ApiRequestModels.UsersRequestModel;
import org.example.Models.RequestModels.GridRequestModels.GetUsersRequestModel;
import org.example.Models.ResponseModels.ApiResponseModels.PaginationBaseResponseModel;
import org.example.Models.ResponseModels.Response;
import org.example.Translators.CentralDatabaseTranslators.Interfaces.IUserSubTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/" + ApiRoutes.ApiControllerNames.USER + "/")
public class UserController {
    private final IUserSubTranslator accessor;

    @Autowired
    public UserController(UserDataAccessor accessor) {
        this.accessor = accessor;
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_USER_PERMISSION +"')")
    @GetMapping(ApiRoutes.UsersSubRoute.GET_USER_PERMISSIONS_BY_ID)
    public ResponseEntity<Response<Permissions>> getUserPermissionsById(@RequestParam long id) {
        return ResponseEntity.ok(accessor.getUserPermissionsById(id));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_USER_PERMISSION +"')")
    @GetMapping(ApiRoutes.UsersSubRoute.IS_EMAIL_AVAILABLE_IN_SYSTEM)
    public ResponseEntity<Response<Boolean>> isEmailAvailableInSystem(@RequestParam String email) throws Exception {
        return ResponseEntity.ok(accessor.isEmailAvailableInSystem(email));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_USER_PERMISSION +"')")
    @GetMapping(ApiRoutes.UsersSubRoute.GET_USER_BY_EMAIL)
    public ResponseEntity<Response<User>> getUserByEmail(@RequestParam String email) {
        return ResponseEntity.ok(accessor.getUserByEmail(email));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_USER_PERMISSION +"')")
    @GetMapping(ApiRoutes.UsersSubRoute.GET_USER_BY_ID)
    public ResponseEntity<Response<User>> getUserById(@RequestParam long id) {
        return ResponseEntity.ok(accessor.getUserById(id));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.INSERT_USER_PERMISSION +"')")
    @PutMapping(ApiRoutes.UsersSubRoute.CREATE_USER)
    public ResponseEntity<Response<Long>> createUser(@RequestBody UsersRequestModel usersRequestModel) {
        return ResponseEntity.ok(accessor.createUser(usersRequestModel));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.DELETE_USER_PERMISSION +"')")
    @DeleteMapping(ApiRoutes.UsersSubRoute.TOGGLE_USER)
    public ResponseEntity<Response<Long>> toggleUser(@RequestParam long id) {
        return ResponseEntity.ok(accessor.toggleUser(id));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.UPDATE_USER_PERMISSION +"')")
    @PostMapping(ApiRoutes.UsersSubRoute.UPDATE_USER)
    public ResponseEntity<Response<Long>> updateUser(@RequestBody UsersRequestModel usersRequestModel) {
        return ResponseEntity.ok(accessor.updateUser(usersRequestModel));
    }

    // just have this for name sake not going to expose this
//    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_USER_PERMISSION +"')")
//    @GetMapping(ApiRoutes.UsersSubRoute.FETCH_ALL_USERS_IN_SYSTEM)
//    public ResponseEntity<Response<List<User>>> fetchAllUsersInSystem(@RequestBody UsersRequestModel usersRequestModel) {
//        return ResponseEntity.ok(accessor.fetchAllUsersInSystem(usersRequestModel.isIncludeDeleted()));
//    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_USER_PERMISSION +"')")
    @GetMapping(ApiRoutes.UsersSubRoute.FETCH_USERS_IN_CARRIER)
    public ResponseEntity<Response<List<User>>> fetchUsersInCarrier(@RequestParam boolean includeDeleted) {
        return ResponseEntity.ok(accessor.fetchUsersInCarrier(includeDeleted));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_USER_PERMISSION +"')")
    @PostMapping(ApiRoutes.UsersSubRoute.GET_USERS_IN_CARRIER_IN_BATCHES)
    public ResponseEntity<Response<PaginationBaseResponseModel<User>>> fetchUsersInCarrierInBatches(@RequestBody GetUsersRequestModel getUsersRequestModel) {
        return ResponseEntity.ok(accessor.fetchUsersInCarrierInBatches(getUsersRequestModel));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.INSERT_USER_PERMISSION +"')")
    @PostMapping(ApiRoutes.UsersSubRoute.IMPORT_USERS)
    public ResponseEntity<Response<String>> importUsers(@RequestBody ImportUsersRequestModel importUsersRequestModel) throws Exception {
        return ResponseEntity.ok(accessor.importUsers(importUsersRequestModel));
    }
}