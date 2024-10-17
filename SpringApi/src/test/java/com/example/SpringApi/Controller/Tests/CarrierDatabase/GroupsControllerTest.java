package com.example.SpringApi.Controller.Tests.CarrierDatabase;

import com.example.SpringApi.Controllers.CarrierDatabase.UserGroupController;
import com.example.SpringApi.Services.CarrierDatabase.UserGroupDataAccessor;
import com.example.SpringApi.DatabaseModels.CarrierDatabase.UserGroup;
import org.example.Models.RequestModels.ApiRequestModels.UserGroupRequestModel;
import org.example.Models.RequestModels.GridRequestModels.PaginationBaseRequestModel;
import org.example.Models.ResponseModels.ApiResponseModels.PaginationBaseResponseModel;
import org.example.Models.ResponseModels.ApiResponseModels.UserGroupResponseModel;
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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GroupsControllerTest {
    @InjectMocks
    UserGroupController groupsController;
    @Mock
    UserGroupDataAccessor groupsDataAccessor;

    @Test
    public void testGetGroupsInBatches() {
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
        Response<PaginationBaseResponseModel<UserGroupResponseModel>> response = new Response<>(true, "Success", new PaginationBaseResponseModel<>());
        when(groupsDataAccessor.getUserGroupsInBatches(any(PaginationBaseRequestModel.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<PaginationBaseResponseModel<UserGroupResponseModel>>> responseEntity = groupsController.getUserGroupsInBatches(paginationBaseRequestModel);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isNotNull();
    }

    @Test
    public void testGetGroupDetailsById() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock the request data
        long id = 1L;

        // mock the data accessor
        Response<UserGroupResponseModel> response = new Response<>(true, "Success", new UserGroupResponseModel());
        when(groupsDataAccessor.getUserGroupDetailsById(any(Long.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<UserGroupResponseModel>> responseEntity = groupsController.getUserGroupDetailsById(id);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isNotNull();
    }

    @Test
    public void testInsertGroup() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock the request data
        UserGroupRequestModel userGroupRequestModel = new UserGroupRequestModel();
        org.example.Models.CommunicationModels.CarrierModels.UserGroup userGroup = new org.example.Models.CommunicationModels.CarrierModels.UserGroup();
        userGroup.setName("Test Group 1");
        userGroup.setDescription("Test Group Description 1");
        userGroupRequestModel.setUserGroup(userGroup);
        userGroupRequestModel.setUserIds(Arrays.asList(552L, 553L));

        // mock the data accessor
        Response<Long> response = new Response<>(true, "Success", 1L);
        when(groupsDataAccessor.createUserGroup(any(UserGroupRequestModel.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<Long>> responseEntity = groupsController.createUserGroup(userGroupRequestModel);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(responseEntity.getBody().getItem()).isEqualTo(1L);
    }

    @Test
    public void testEditGroup() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock the request data
        UserGroupRequestModel userGroupRequestModel = new UserGroupRequestModel();
        org.example.Models.CommunicationModels.CarrierModels.UserGroup userGroup = new org.example.Models.CommunicationModels.CarrierModels.UserGroup();
        userGroup.setName("Test Group 1 edit");
        userGroup.setDescription("Test Group Description 1");
        userGroupRequestModel.setUserGroup(userGroup);
        userGroupRequestModel.setUserIds(Arrays.asList(552L, 553L));

        // mock the data accessor
        Response<Long> response = new Response<>(true, "Success", 1L);
        when(groupsDataAccessor.updateUserGroup(any(UserGroupRequestModel.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<Long>> responseEntity = groupsController.updateUserGroup(userGroupRequestModel);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(responseEntity.getBody().getItem()).isEqualTo(1L);
    }

    @Test
    public void testToggleGroup(){
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock the request data
        long id = 1L;

        // mock the data accessor
        Response<Boolean> response = new Response<>(true, "Success", true);
        when(groupsDataAccessor.toggleUserGroup(any(Long.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<Boolean>> responseEntity = groupsController.toggleUserGroup(id);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isEqualTo(true);
    }
}
