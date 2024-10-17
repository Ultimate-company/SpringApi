package com.example.SpringApi.Controllers.CentralDatabase;

import com.example.SpringApi.Services.CentralDatabase.UserLogDataAccessor;
import org.example.ApiRoutes;
import org.example.Models.Authorizations;
import org.example.Models.CommunicationModels.CentralModels.UserLog;
import org.example.Models.RequestModels.GridRequestModels.GetUserLogsRequestModel;
import org.example.Models.ResponseModels.ApiResponseModels.PaginationBaseResponseModel;
import org.example.Models.ResponseModels.Response;
import org.example.Translators.CentralDatabaseTranslators.Interfaces.IUserLogSubTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/" + ApiRoutes.ApiControllerNames.USERLOG)
public class UserLogController {
    private final IUserLogSubTranslator accessor;

    @Autowired
    public UserLogController(UserLogDataAccessor accessor) {
        this.accessor = accessor;
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_USER_PERMISSION +"')")
    @PostMapping(ApiRoutes.UserLogSubRoute.GET_USER_LOGS_IN_BATCHES_BY_USERID)
    public ResponseEntity<Response<PaginationBaseResponseModel<UserLog>>> fetchUserLogsInBatches(@RequestBody GetUserLogsRequestModel getUserLogsRequestModel) {
        return ResponseEntity.ok(accessor.fetchUserLogsInBatches(getUserLogsRequestModel));
    }
}