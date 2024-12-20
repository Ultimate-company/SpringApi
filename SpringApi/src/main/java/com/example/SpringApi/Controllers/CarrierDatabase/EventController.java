package com.example.SpringApi.Controllers.CarrierDatabase;

import com.example.SpringApi.Services.CarrierDatabase.EventDataAccessor;
import org.example.ApiRoutes;
import org.example.Models.Authorizations;
import org.example.Models.CommunicationModels.CarrierModels.Event;
import org.example.Models.RequestModels.ApiRequestModels.BaseRequest;
import org.example.Models.RequestModels.ApiRequestModels.EventRequestModel;
import org.example.Models.ResponseModels.ApiResponseModels.EventResponseModel;
import org.example.Models.ResponseModels.Response;
import org.example.Translators.CarrierDatabaseTranslators.Interfaces.IEventSubTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/" + ApiRoutes.ApiControllerNames.EVENT + "/")
public class EventController {
    private final IEventSubTranslator accessor;
    @Autowired
    public EventController(EventDataAccessor accessor) {
        this.accessor = accessor;
    }

    @PreAuthorize("@customAuthorization.hasAuthority('" + Authorizations.VIEW_EVENTS_PERMISSION + "')")
    @GetMapping(ApiRoutes.EventSubRoute.GET_ALL_EVENTS_FOR_USERID_BASED_ON_MONTH)
    public ResponseEntity<Response<List<EventResponseModel>>> getAllEventsForUserIdBasedOnMonth(@RequestParam long userId, @RequestParam int month) {
        return ResponseEntity.ok(accessor.getAllEventsForUserIdBasedOnMonth(userId, month));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('" + Authorizations.INSERT_EVENTS_PERMISSION + "')")
    @PutMapping(ApiRoutes.EventSubRoute.CREATE_EVENT)
    public ResponseEntity<Response<Long>> createEvent(@RequestBody EventRequestModel eventRequestModel) {
        return ResponseEntity.ok(accessor.createEvent(eventRequestModel));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('" + Authorizations.UPDATE_EVENTS_PERMISSION + "')")
    @PostMapping(ApiRoutes.EventSubRoute.UPDATE_EVENT)
    public ResponseEntity<Response<Boolean>> updateEvent(@RequestBody EventRequestModel eventRequestModel) {
        return ResponseEntity.ok(accessor.updateEvent(eventRequestModel));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('" + Authorizations.TOGGLE_EVENTS_PERMISSION + "')")
    @DeleteMapping(ApiRoutes.EventSubRoute.TOGGLE_EVENT)
    public ResponseEntity<Response<Boolean>> toggleEvent(@RequestParam long eventId) {
        return ResponseEntity.ok(accessor.toggleEvent(eventId));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('" + Authorizations.VIEW_EVENTS_PERMISSION + "')")
    @GetMapping(ApiRoutes.EventSubRoute.GET_EVENT_DETAILS_BY_ID)
    public ResponseEntity<Response<EventResponseModel>> getEventDetailsById(@RequestParam long eventId) {
        return ResponseEntity.ok(accessor.getEventDetailsById(eventId));
    }
}