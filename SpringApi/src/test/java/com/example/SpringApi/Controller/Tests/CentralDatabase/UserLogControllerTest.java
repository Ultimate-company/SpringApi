package com.example.SpringApi.Controller.Tests.CentralDatabase;

import com.example.SpringApi.Controllers.CentralDatabase.UserLogController;
import com.example.SpringApi.Services.CentralDatabase.UserLogDataAccessor;
import org.example.Models.CommunicationModels.CentralModels.UserLog;
import org.example.Models.RequestModels.GridRequestModels.GetUserLogsRequestModel;
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

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserLogControllerTest {

    @InjectMocks
    UserLogController userLogController;
    @Mock
    UserLogDataAccessor userLogDataAccessor;

    @Test
    public void testGetUserLogsInBatchesByUserId() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // Data
        GetUserLogsRequestModel getUserLogsRequestModel = new GetUserLogsRequestModel();
        getUserLogsRequestModel.setStart(0);
        getUserLogsRequestModel.setEnd(3);
        getUserLogsRequestModel.setFilterExpr("getToken");
        getUserLogsRequestModel.setColumnName("Change");
        getUserLogsRequestModel.setCondition("contains");
        getUserLogsRequestModel.setUserId(552);

        // mock the data accessor
        Response<PaginationBaseResponseModel<UserLog>> response = new Response<>(true, "Success", new PaginationBaseResponseModel<UserLog>());
        when(userLogDataAccessor.fetchUserLogsInBatches(any(GetUserLogsRequestModel.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<PaginationBaseResponseModel<UserLog>>> responseEntity = userLogController.fetchUserLogsInBatches(getUserLogsRequestModel);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isNotNull();
    }

}