package com.example.SpringApi.Controller.Tests.CarrierDatabase;

import com.example.SpringApi.Controllers.CarrierDatabase.UserController;
import com.example.SpringApi.Services.CentralDatabase.UserDataAccessor;
import org.example.Models.CommunicationModels.CarrierModels.Permissions;
import org.example.Models.CommunicationModels.CentralModels.User;
import org.example.Models.RequestModels.ApiRequestModels.UsersRequestModel;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {
    @InjectMocks
    UserController userController;
    @Mock
    UserDataAccessor userDataAccessor;

    private static UsersRequestModel getUsersRequestModel(org.example.Models.CommunicationModels.CentralModels.User user) {
        org.example.Models.CommunicationModels.CarrierModels.Address address = new org.example.Models.CommunicationModels.CarrierModels.Address();
        address.setLine1("Line 1");
        address.setCity("mumbai");
        address.setState("maharashtra");
        address.setZipCode("400002");
        address.setPhoneOnAddress(user.getPhone());
        address.setNameOnAddress(user.getFirstName() + " " + user.getLastName());

        org.example.Models.CommunicationModels.CarrierModels.Permissions permissions = new org.example.Models.CommunicationModels.CarrierModels.Permissions();
        permissions.setUserPermissions("View, Add, Edit, Delete");

        List<Long> selectedGroupIds = new ArrayList<>(List.of(1L));

        UsersRequestModel usersRequestModel = new UsersRequestModel();
        usersRequestModel.setUser(user);
        usersRequestModel.setAddress(address);
        usersRequestModel.setPermissions(permissions);
        usersRequestModel.setUserGroupIds(selectedGroupIds);
        return usersRequestModel;
    }

    @Test
    public void testIsEmailAvailableInSystem() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // Data
        String email = "nahushrai+1@gmail.com";

        // mock the data accessor
        Response<Boolean> response = new Response<>(true, "Success", true);
        when(userDataAccessor.isEmailAvailableInSystem(any(String.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<Boolean>> responseEntity = userController.isEmailAvailableInSystem(email);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isEqualTo(true);
    }

    @Test
    public void testGetUserByEmail(){
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // Data
        String email = "nahushrai+1@gmail.com";

        // mock the data accessor
        Response<User> response = new Response<>(true, "Success", new User());
        when(userDataAccessor.getUserByEmail(any(String.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<User>> responseEntity = userController.getUserByEmail(email);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isNotNull();
    }

    @Test
    public void testGetUserById(){
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // Data
        long userId = 1L;

        // mock the data accessor
        Response<User> response = new Response<>(true, "Success", new User());
        when(userDataAccessor.getUserById(any(Long.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<User>> responseEntity = userController.getUserById(userId);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isNotNull();
    }

    @Test
    public void testCreateUser() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // Data
        org.example.Models.CommunicationModels.CentralModels.User user = new org.example.Models.CommunicationModels.CentralModels.User();
        user.setLoginName("nahushrai+1@gmail.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPhone("9892555438");
        user.setRole("Admin");
        user.setDob(new SimpleDateFormat("yyyy-MM-dd").parse("1990-01-01"));

        UsersRequestModel usersRequestModel = getUsersRequestModel(user);

        // mock the data accessor
        Response<Long> response = new Response<>(true, "Success", 2L);
        when(userDataAccessor.createUser(any(UsersRequestModel.class)))
                .thenReturn(response);
        
        // test the controller
        ResponseEntity<Response<Long>> responseEntity = userController.createUser(usersRequestModel);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(responseEntity.getBody().getItem()).isNotNull();
        assertThat(responseEntity.getBody().getItem()).isEqualTo(2L);
    }

    @Test
    public void testToggleUser() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock the data accessor
        Response<Long> response = new Response<>(true, "Success", 1L);
        when(userDataAccessor.toggleUser(any(Long.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<Long>> responseEntity = userController.toggleUser(1L);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isNotNull();
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isEqualTo(1L);
    }

    @Test
    public void testUpdateUser() throws Exception{
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // Data
        org.example.Models.CommunicationModels.CentralModels.User user = new org.example.Models.CommunicationModels.CentralModels.User();
        user.setLoginName("nahushrai+1@gmail.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPhone("9892555438");
        user.setRole("Admin");
        user.setDob(new SimpleDateFormat("yyyy-MM-dd").parse("1990-01-01"));

        UsersRequestModel usersRequestModel = getUsersRequestModel(user);

        // mock the data accessor
        Response<Long> response = new Response<>(true, "Success", 2L);
        when(userDataAccessor.updateUser(any(UsersRequestModel.class)))
                .thenReturn(response);

        // test the controller
        ResponseEntity<Response<Long>> responseEntity = userController.updateUser(usersRequestModel);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(responseEntity.getBody().getItem()).isNotNull();
        assertThat(responseEntity.getBody().getItem()).isEqualTo(2L);
    }

//    @Test
//    public void testFetchAllUsersInSystem() {
//        MockHttpServletRequest request = new MockHttpServletRequest();
//        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
//
//        // Data
//        UsersRequestModel usersRequestModel = new UsersRequestModel();
//        usersRequestModel.setIncludeDeleted(false);
//
//        // mock the data accessor
//        Response<List<User>> response = new Response<>(true, "Success", new ArrayList<>());
//        when(userDataAccessor.fetchAllUsersInSystem(any(Boolean.class))).thenReturn(response);
//
//        // test the controller
//        ResponseEntity<Response<List<User>>> responseEntity = userController.fetchAllUsersInSystem(usersRequestModel);
//        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
//        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
//        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isNotNull();
//    }

    @Test
    public void testFetchAllUsersInCarrier() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock the data accessor
        Response<List<User>> response = new Response<>(true, "Success", new ArrayList<>());
        when(userDataAccessor.fetchUsersInCarrier(any(Boolean.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<List<User>>> responseEntity = userController.fetchUsersInCarrier(false);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isNotNull();
    }

    @Test
    public void testGetUserPermissionsById() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock the data
        long userId = 552;

        // mock the data accessor
        Response<Permissions> response = new Response<>(true, "Success", new Permissions());
        when(userDataAccessor.getUserPermissionsById(any(Long.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<Permissions>> responseEntity = userController.getUserPermissionsById(userId);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isNotNull();
    }
}
