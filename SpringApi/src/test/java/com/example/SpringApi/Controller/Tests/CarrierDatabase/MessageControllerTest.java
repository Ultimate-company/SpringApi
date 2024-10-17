package com.example.SpringApi.Controller.Tests.CarrierDatabase;

import com.example.SpringApi.Controllers.CarrierDatabase.MessageController;
import com.example.SpringApi.Services.CarrierDatabase.MessageDataAccessor;
import org.example.Models.CommunicationModels.CarrierModels.Message;
import org.example.Models.RequestModels.ApiRequestModels.MessageRequestModel;
import org.example.Models.RequestModels.GridRequestModels.PaginationBaseRequestModel;
import org.example.Models.ResponseModels.ApiResponseModels.MessageResponseModel;
import org.example.Models.ResponseModels.ApiResponseModels.PaginationBaseResponseModel;
import org.example.Models.ResponseModels.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MessageControllerTest {
    @InjectMocks
    MessageController messageController;
    @Mock
    MessageDataAccessor messageDataAccessor;

    @Test
    public void testGetMessagesInBatches() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock the request data
        PaginationBaseRequestModel paginationBaseRequestModel = new PaginationBaseRequestModel();
        paginationBaseRequestModel.setStart(0);
        paginationBaseRequestModel.setEnd(3);
        paginationBaseRequestModel.setFilterExpr("");
        paginationBaseRequestModel.setColumnName("");
        paginationBaseRequestModel.setCondition("");
        paginationBaseRequestModel.setIncludeDeleted(true);

        // mock the data accessor
        Response<PaginationBaseResponseModel<MessageResponseModel>> response = new Response<>(true, "Success", new PaginationBaseResponseModel<>());
        when(messageDataAccessor.getMessagesInBatches(any(PaginationBaseRequestModel.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<PaginationBaseResponseModel<MessageResponseModel>>> responseEntity = messageController.getMessagesInBatches(paginationBaseRequestModel);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isNotNull();
    }

    @Test
    public void testAddMessage() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock the data
        MessageRequestModel messageRequestModel = new MessageRequestModel();
        org.example.Models.CommunicationModels.CarrierModels.Message message = new org.example.Models.CommunicationModels.CarrierModels.Message();
        message.setTitle("Test message");
        message.setPublishDate(new Date());
        message.setDescription("Test Description");
        message.setDescriptionMarkDown("Test Description");
        message.setDescriptionHtml("Test Description Html");
        message.setSendAsEmail(false);

        messageRequestModel.setMessage(message);
        messageRequestModel.setUserIds(List.of(552L));

        // mock the data accessor
        Response<Long> response = new Response<>(true, "Success", 1L);
        when(messageDataAccessor.createMessage(any(MessageRequestModel.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<Long>> responseEntity = messageController.createMessage(messageRequestModel);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(responseEntity.getBody().getItem()).isEqualTo(1L);
    }

    @Test
    public void testEditMessage() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock the data
        MessageRequestModel messageRequestModel = new MessageRequestModel();
        org.example.Models.CommunicationModels.CarrierModels.Message message = new org.example.Models.CommunicationModels.CarrierModels.Message();
        message.setMessageId(1L);
        message.setTitle("Test message edit");
        message.setPublishDate(new Date());
        message.setDescription("Test Description edit");
        message.setDescriptionMarkDown("Test Description");
        message.setDescriptionHtml("Test Description Html");
        message.setSendAsEmail(false);

        messageRequestModel.setMessage(message);
        messageRequestModel.setUserIds(List.of(552L));

        // mock the data accessor
        Response<Long> response = new Response<>(true, "Success", 1L);
        when(messageDataAccessor.updateMessage(any(MessageRequestModel.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<Long>> responseEntity = messageController.updateMessage(messageRequestModel);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(responseEntity.getBody().getItem()).isEqualTo(1L);
    }

    @Test
    public void testToggleMessage() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock the request data
        long id = 1L;

        // mock the data accessor
        Response<Boolean> response = new Response<>(true, "Success", true);
        when(messageDataAccessor.toggleMessage(any(Long.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<Boolean>> responseEntity = messageController.toggleMessage(id);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isEqualTo(true);
    }

    @Test
    public void testGetMessageDetailsById() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock the request data
        long id = 1L;

        // mock the data accessor
        Response<MessageResponseModel> response = new Response<>(true, "Success", new MessageResponseModel());
        when(messageDataAccessor.getMessageDetailsById(any(Long.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<MessageResponseModel>> responseEntity = messageController.getMessageDetailsById(id);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isNotNull();
    }

    @Test
    public void testGetUsersInMessages() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock the request data
        long id = 1L;

        // mock the data accessor
        Response<List<Long>> response = new Response<>(true, "Success", Arrays.asList(1L,2L));
        when(messageDataAccessor.getUsersInMessages(any(Long.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<List<Long>>> responseEntity = messageController.getUsersInMessages(id);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isNotNull();
        assertThat(responseEntity.getBody().getItem().size()).isEqualTo(2);
    }

    @Test
    public void testGetMessagesByUserId() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock the request data
        long id = 1L;

        // mock the data accessor
        Response<List<MessageResponseModel>> response = new Response<>(true, "Success", new ArrayList<>());
        when(messageDataAccessor.getMessagesByUserId(any(Long.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<List<MessageResponseModel>>> responseEntity = messageController.getMessagesByUserId(id);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isNotNull();
    }
}