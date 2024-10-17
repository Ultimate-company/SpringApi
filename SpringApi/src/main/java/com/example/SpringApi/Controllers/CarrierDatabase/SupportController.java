package com.example.SpringApi.Controllers.CarrierDatabase;

import com.example.SpringApi.Services.CarrierDatabase.SupportDataAccessor;
import org.example.ApiRoutes;
import org.example.Models.Authorizations;
import org.example.Models.RequestModels.ApiRequestModels.SupportRequestModel;
import org.example.Models.ResponseModels.JiraResponseModels.*;
import org.example.Models.ResponseModels.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/" + ApiRoutes.ApiControllerNames.SUPPORT + "/")
public class SupportController {

    private final SupportDataAccessor accessor;
    @Autowired
    public SupportController(SupportDataAccessor accessor) {
        this.accessor = accessor;
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_TICKETS_PERMISSION +"')")
    @GetMapping(ApiRoutes.SupportSubRoute.GET_SUPPORT_TICKETS_IN_BATCHES)
    public ResponseEntity<Response<GetTicketsResponseModel>> getSupportTicketsInBatches(@RequestParam int start, @RequestParam int end) {
        return ResponseEntity.ok(accessor.getSupportTicketsInBatches(start, end));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_TICKETS_PERMISSION +"')")
    @GetMapping(ApiRoutes.SupportSubRoute.GET_TICKET_DETAILS_BY_ID)
    public ResponseEntity<Response<GetTicketDetailsResponseModel>> getTicketDetailsById(@RequestParam String ticketId) {
        return ResponseEntity.ok(accessor.getTicketDetailsById(ticketId));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.DOWNLOAD_ATTACHMENTS_PERMISSION +"')")
    @GetMapping(ApiRoutes.SupportSubRoute.GET_ATTACHMENT_FROM_TICKET)
    public ResponseEntity<Response<Map<String, String>>> getAttachmentFromTicket(@RequestParam String ticketId) {
        return ResponseEntity.ok(accessor.getAttachmentFromTicket(ticketId));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_COMMENTS_PERMISSION +"')")
    @GetMapping(ApiRoutes.SupportSubRoute.GET_COMMENTS_FROM_TICKET)
    public ResponseEntity<Response<GetCommentsResponseModel>> getCommentsFromTicket(@RequestParam String ticketId) {
        return ResponseEntity.ok(accessor.getCommentsFromTicket(ticketId));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.RAISE_TICKETS_PERMISSION +"')")
    @PutMapping(ApiRoutes.SupportSubRoute.CREATE_TICKET)
    public ResponseEntity<Response<CreateTicketResponseModel>> createTicket(@RequestBody SupportRequestModel supportRequestModel) {
        return ResponseEntity.ok(accessor.createTicket(supportRequestModel));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.POST_COMMENTS_PERMISSION +"')")
    @PutMapping(ApiRoutes.SupportSubRoute.ADD_COMMENT)
    public ResponseEntity<Response<AddCommentResponseModel>> addComment(@RequestParam String ticketId, @RequestBody SupportRequestModel supportRequestModel) {
        return ResponseEntity.ok(accessor.addComment(ticketId, supportRequestModel));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.EDIT_TICKETS_PERMISSION +"')")
    @DeleteMapping(ApiRoutes.SupportSubRoute.DELETE_COMMENT)
    public ResponseEntity<Response<Boolean>> deleteComment(@RequestParam String ticketId, @RequestParam String commentId) {
        return ResponseEntity.ok(accessor.deleteComment(ticketId, commentId));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.POST_COMMENTS_PERMISSION +"')")
    @PostMapping(ApiRoutes.SupportSubRoute.EDIT_COMMENT)
    public ResponseEntity<Response<Boolean>> editComment(@RequestParam String ticketId, @RequestParam String commentId, @RequestBody SupportRequestModel supportRequestModel) {
        return ResponseEntity.ok(accessor.editComment(ticketId, commentId, supportRequestModel));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.EDIT_TICKETS_PERMISSION +"')")
    @PostMapping(ApiRoutes.SupportSubRoute.EDIT_TICKET)
    public ResponseEntity<Response<Boolean>> editTicket(@RequestParam String ticketId, @RequestBody SupportRequestModel supportRequestModel) {
        return ResponseEntity.ok(accessor.editTicket(ticketId, supportRequestModel));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.DELETE_TICKETS_PERMISSION +"')")
    @DeleteMapping(ApiRoutes.SupportSubRoute.DELETE_TICKET)
    public ResponseEntity<Response<Boolean>> editTicket(@RequestParam String ticketId) {
        return ResponseEntity.ok(accessor.deleteTicket(ticketId));
    }
}
