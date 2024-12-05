package com.example.SpringApi.Controllers.CarrierDatabase;

import com.example.SpringApi.Authentication.Authorization;
import com.example.SpringApi.Services.BulkDataAccessor;
import com.example.SpringApi.SuccessMessages;
import org.example.ApiRoutes;
import org.example.Models.ResponseModels.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/" + ApiRoutes.ApiControllerNames.BULK + "/")
public class BulkController {

    private final BulkDataAccessor accessor;
    private final Authorization authorization;

    @Autowired
    public BulkController(BulkDataAccessor accessor, Authorization authorization) {
        this.accessor = accessor;
        this.authorization = authorization;
    }

    @PreAuthorize("@customAuthorization.validateToken()")
    @PutMapping(ApiRoutes.BulkSubRoute.BULK_INSERT)
    public Response<Boolean> bulkUpload(@RequestParam String type,
                                        @RequestParam String AuditUserId,
                                        @RequestParam String CarrierId,
                                        @RequestBody String object) throws Exception {
        accessor.bulkAddAsync(type, object, AuditUserId, CarrierId, authorization.getJwtFromRequest());
        return new Response<>(true, SuccessMessages.Success, true);
    }
}