package com.example.SpringApi.Controllers.CarrierDatabase;

import com.example.SpringApi.Services.CarrierDatabase.MessageDataAccessor;
import org.example.ApiRoutes;
import org.example.Models.Authorizations;
import org.example.Models.CommunicationModels.CarrierModels.Message;
import org.example.Models.RequestModels.ApiRequestModels.MessageRequestModel;
import org.example.Models.RequestModels.GridRequestModels.PaginationBaseRequestModel;
import org.example.Models.ResponseModels.ApiResponseModels.MessageResponseModel;
import org.example.Models.ResponseModels.ApiResponseModels.PaginationBaseResponseModel;
import org.example.Models.ResponseModels.Response;
import org.example.Translators.CarrierDatabaseTranslators.Interfaces.IMessageSubTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/" + ApiRoutes.ApiControllerNames.MESSAGE + "/")
public class MessageController {
    private final IMessageSubTranslator accessor;
    @Autowired
    public MessageController(MessageDataAccessor accessor) {
        this.accessor = accessor;
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_MESSAGES_PERMISSION +"')")
    @PostMapping(ApiRoutes.MessagesSubRoute.GET_MESSAGES_IN_BATCHES)
    public ResponseEntity<Response<PaginationBaseResponseModel<MessageResponseModel>>> getMessagesInBatches(@RequestBody PaginationBaseRequestModel paginationBaseRequestModel) {
        return ResponseEntity.ok(accessor.getMessagesInBatches(paginationBaseRequestModel));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.INSERT_MESSAGES_PERMISSION +"')")
    @PutMapping(ApiRoutes.MessagesSubRoute.CREATE_MESSAGE)
    public ResponseEntity<Response<Long>> createMessage(@RequestBody MessageRequestModel messageRequestModel) {
        return ResponseEntity.ok(accessor.createMessage(messageRequestModel));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.UPDATE_MESSAGES_PERMISSION +"')")
    @PostMapping(ApiRoutes.MessagesSubRoute.UPDATE_MESSAGE)
    public ResponseEntity<Response<Long>> updateMessage(@RequestBody MessageRequestModel messageRequestModel) {
        return ResponseEntity.ok(accessor.updateMessage(messageRequestModel));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.DELETE_MESSAGES_PERMISSION +"')")
    @DeleteMapping(ApiRoutes.MessagesSubRoute.TOGGLE_MESSAGE)
    public ResponseEntity<Response<Boolean>> toggleMessage(@RequestParam long id) {
        return ResponseEntity.ok(accessor.toggleMessage(id));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_MESSAGES_PERMISSION +"')")
    @GetMapping(ApiRoutes.MessagesSubRoute.GET_MESSAGE_DETAILS_BY_ID)
    public ResponseEntity<Response<MessageResponseModel>> getMessageDetailsById(@RequestParam long id) {
        return ResponseEntity.ok(accessor.getMessageDetailsById(id));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_MESSAGES_PERMISSION +"')")
    @GetMapping(ApiRoutes.MessagesSubRoute.GET_USERS_IN_MESSAGE)
    public ResponseEntity<Response<List<Long>>> getUsersInMessages(@RequestParam long id) {
        return ResponseEntity.ok(accessor.getUsersInMessages(id));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_MESSAGES_PERMISSION +"')")
    @GetMapping(ApiRoutes.MessagesSubRoute.GET_USER_GROUPS_IN_MESSAGE)
    public ResponseEntity<Response<List<Long>>> getUserGroupsInMessage(@RequestParam long id) {
        return ResponseEntity.ok(accessor.getUserGroupsInMessage(id));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_MESSAGES_PERMISSION +"')")
    @GetMapping(ApiRoutes.MessagesSubRoute.GET_MESSAGES_BY_USER_ID)
    public ResponseEntity<Response<List<MessageResponseModel>>> getMessagesByUserId(@RequestParam long id) {
        return ResponseEntity.ok(accessor.getMessagesByUserId(id));
    }

    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_MESSAGES_PERMISSION +"')")
    @PostMapping(ApiRoutes.MessagesSubRoute.SET_MESSAGE_READ_BY_USER_ID_AND_MESSAGE_ID)
    public ResponseEntity<Response<Boolean>> setMessageReadByUserIdAndMessageId(@RequestParam long userId, @RequestParam long messageId) {
        return ResponseEntity.ok(accessor.setMessageReadByUserIdAndMessageId(userId, messageId));
    }
}
