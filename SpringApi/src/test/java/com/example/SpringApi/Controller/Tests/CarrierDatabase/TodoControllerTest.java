package com.example.SpringApi.Controller.Tests.CarrierDatabase;

import com.example.SpringApi.Controllers.CarrierDatabase.TodoController;
import com.example.SpringApi.Services.CarrierDatabase.TodoDataAccessor;
import org.example.Models.CommunicationModels.CarrierModels.Todo;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TodoControllerTest {
    @InjectMocks
    TodoController todoController;
    @Mock
    TodoDataAccessor todoDataAccessor;

    @Test
    public void testAddItem() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock the data
        Todo todo = new Todo();
        todo.setTask("Test 1");

        // mock the data accessor
        Response<Long> response = new Response<>(true, "Success", 1L);
        when(todoDataAccessor.addTodo(any(Todo.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<Long>> responseEntity = todoController.addItem(todo);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(responseEntity.getBody().getItem()).isEqualTo(1L);
    }

    @Test
    public void testDeleteItem() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock the data
        long id = 1L;

        // mock the data accessor
        Response<Boolean> response = new Response<>(true, "Success", true);
        when(todoDataAccessor.deleteTodo(any(Long.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<Boolean>> responseEntity = todoController.deleteItem(id);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(responseEntity.getBody().getItem()).isEqualTo(true);
    }

    @Test
    public void testToggleTodo() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock the data
        long toDoId = 1L;

        // mock the data accessor
        Response<Boolean> response = new Response<>(true, "Success", true);
        when(todoDataAccessor.toggleTodo(any(Long.class))).thenReturn(response);

        // test the controller
        ResponseEntity<Response<Boolean>> responseEntity = todoController.toggleTodo(toDoId);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(responseEntity.getBody().getItem()).isEqualTo(true);
    }

    @Test
    public void testGetTodoItems() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // mock the data accessor
        Response<List<Todo>> response = new Response<>(true, "Success", new ArrayList<>());
        when(todoDataAccessor.getItems()).thenReturn(response);

        // test the controller
        ResponseEntity<Response<List<Todo>>> responseEntity = todoController.getTodoItems();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(Objects.requireNonNull(responseEntity.getBody()).isSuccess()).isEqualTo(true);
        assertThat(responseEntity.getBody().getItem()).isNotNull();
    }
}