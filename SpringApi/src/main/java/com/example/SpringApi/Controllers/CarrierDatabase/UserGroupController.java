package com.example.SpringApi.Controllers.CarrierDatabase;

import com.example.SpringApi.Services.CarrierDatabase.UserGroupDataAccessor;
import org.example.ApiRoutes;
import org.example.Models.Authorizations;
import org.example.Models.CommunicationModels.CarrierModels.UserGroup;
import org.example.Models.RequestModels.ApiRequestModels.UserGroupRequestModel;
import org.example.Models.RequestModels.GridRequestModels.PaginationBaseRequestModel;
import org.example.Models.ResponseModels.ApiResponseModels.PaginationBaseResponseModel;
import org.example.Models.ResponseModels.ApiResponseModels.UserGroupResponseModel;
import org.example.Models.ResponseModels.Response;
import org.example.Translators.CarrierDatabaseTranslators.Interfaces.IUserGroupSubTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/" + ApiRoutes.ApiControllerNames.USER_GROUP + "/")
public class UserGroupController {
    private final IUserGroupSubTranslator accessor;
    @Autowired
    public UserGroupController(UserGroupDataAccessor accessor) {
        this.accessor = accessor;
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_GROUPS_PERMISSION +"')")
    @PostMapping(ApiRoutes.UserGroupsSubRoute.GET_USER_GROUPS_IN_BATCHES)
    public ResponseEntity<Response<PaginationBaseResponseModel<UserGroupResponseModel>>> getUserGroupsInBatches(@RequestBody PaginationBaseRequestModel paginationBaseRequestModel) {
        return ResponseEntity.ok(accessor.getUserGroupsInBatches(paginationBaseRequestModel));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_GROUPS_PERMISSION +"')")
    @GetMapping(ApiRoutes.UserGroupsSubRoute.GET_USER_GROUP_DETAILS_BY_ID)
    public ResponseEntity<Response<UserGroupResponseModel>> getUserGroupDetailsById(@RequestParam long id) {
        return ResponseEntity.ok(accessor.getUserGroupDetailsById(id));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.INSERT_GROUPS_PERMISSION +"')")
    @PutMapping(ApiRoutes.UserGroupsSubRoute.CREATE_USER_GROUP)
    public ResponseEntity<Response<Long>> createUserGroup(@RequestBody UserGroupRequestModel userGroupRequestModel) {
        return ResponseEntity.ok(accessor.createUserGroup(userGroupRequestModel));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.UPDATE_GROUPS_PERMISSION +"')")
    @PostMapping(ApiRoutes.UserGroupsSubRoute.UPDATE_USER_GROUP)
    public ResponseEntity<Response<Long>> updateUserGroup(@RequestBody UserGroupRequestModel userGroupRequestModel) {
        return ResponseEntity.ok(accessor.updateUserGroup(userGroupRequestModel));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.DELETE_GROUPS_PERMISSION +"')")
    @DeleteMapping(ApiRoutes.UserGroupsSubRoute.TOGGLE_USER_GROUP)
    public ResponseEntity<Response<Boolean>> toggleUserGroup(@RequestParam long id) {
        return ResponseEntity.ok(accessor.toggleUserGroup(id));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_GROUPS_PERMISSION +"')")
    @GetMapping(ApiRoutes.UserGroupsSubRoute.GET_USER_GROUP_IDS_BY_USERID)
    public ResponseEntity<Response<List<Long>>> getUserGroupIdsByUserId(@RequestParam long id) {
        return ResponseEntity.ok(accessor.getUserGroupIdsByUserId(id));
    }
}