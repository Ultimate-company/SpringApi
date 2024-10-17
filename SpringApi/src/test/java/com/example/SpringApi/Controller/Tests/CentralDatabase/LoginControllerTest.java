package com.example.SpringApi.Controller.Tests.CentralDatabase;

import static org.assertj.core.api.Assertions.assertThat;
import java.text.SimpleDateFormat;
import com.example.SpringApi.Controllers.CentralDatabase.LoginController;
import com.example.SpringApi.Services.CentralDatabase.LoginDataAccessor;
import org.example.Models.CommunicationModels.CentralModels.User;
import org.example.Models.RequestModels.ApiRequestModels.LoginRequestModel;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import java.util.Objects;

@ExtendWith(MockitoExtension.class)
public class LoginControllerTest {

    @InjectMocks
    LoginController loginController;
    @Mock
    LoginDataAccessor loginDataAccessor;

    @Test
    public void testSignUp() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // Data
        User user = new User();
        user.setLoginName("nahushrai+1@gmail.com");
        user.setPassword("Code@123");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPhone("9892555438");
        user.setRole("Admin");
        user.setDob(new SimpleDateFormat("yyyy-MM-dd").parse("1990-01-01"));

        // mock the data accessor
        Response<String> response = new Response<>(true, "Success", "some api key");
        when(loginDataAccessor.signUp(any(User.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<String>> responseEntity = loginController.signUp(user);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(responseEntity.getBody().getItem()).isNotBlank();
        assertThat(responseEntity.getBody().getItem()).isNotNull();
        assertThat(responseEntity.getBody().getItem()).isNotEmpty();
        assertThat(responseEntity.getBody().getItem()).isEqualTo("some api key");
    }

    @Test
    public void testConfirmAccount() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // Data
        LoginRequestModel loginRequestModel = new LoginRequestModel();
        loginRequestModel.setUserId(123L);
        loginRequestModel.setApiKey("some api key");

        // mock the data accessor
        Response<Boolean> response = new Response<>(true, "Success", true);
        when(loginDataAccessor.confirmEmail(any(LoginRequestModel.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<Boolean>> responseEntity = loginController.confirmEmail(loginRequestModel);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isEqualTo(true);
    }

    @Test
    public void testGetToken() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // Data
        LoginRequestModel loginRequestModel = new LoginRequestModel();
        loginRequestModel.setLoginName("nahushrai@gmail.com");
        loginRequestModel.setApiKey("some api key");

        // mock the data accessor
        Response<String> response = new Response<>(true, "Success", "some token");
        when(loginDataAccessor.getToken(any(LoginRequestModel.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<String>> responseEntity = loginController.getToken(loginRequestModel);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(responseEntity.getBody().getItem()).isNotBlank();
        assertThat(responseEntity.getBody().getItem()).isNotNull();
        assertThat(responseEntity.getBody().getItem()).isNotEmpty();
        assertThat(responseEntity.getBody().getItem()).isEqualTo("some token");
    }

    @Test
    public void testSignIn() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // Data
        LoginRequestModel loginRequestModel = new LoginRequestModel();
        loginRequestModel.setLoginName("nahushrai@gmail.com");
        loginRequestModel.setPassword("Code@123");

        // mock the data accessor
        Response<User> response = new Response<>(true, "Success", new User());
        when(loginDataAccessor.signIn(any(LoginRequestModel.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<User>> responseEntity = loginController.signIn(loginRequestModel);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(Objects.requireNonNull(responseEntity.getBody()).getItem()).isNotNull();
    }
}
