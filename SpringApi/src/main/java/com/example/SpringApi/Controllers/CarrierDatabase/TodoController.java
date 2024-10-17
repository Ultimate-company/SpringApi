package com.example.SpringApi.Controllers.CarrierDatabase;

import com.example.SpringApi.Services.CarrierDatabase.TodoDataAccessor;
import org.example.ApiRoutes;
import org.example.Models.CommunicationModels.CarrierModels.Todo;
import org.example.Models.ResponseModels.Response;
import org.example.Translators.CarrierDatabaseTranslators.Interfaces.ITodoListSubTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/" + ApiRoutes.ApiControllerNames.TODO + "/")
public class TodoController {
    private final ITodoListSubTranslator accessor;

    @Autowired
    public TodoController(TodoDataAccessor accessor) {
        this.accessor = accessor;
    }

    @PreAuthorize("@customAuthorization.validateToken()")
    @PutMapping(ApiRoutes.TodoSubRoute.ADD_ITEM)
    public ResponseEntity<Response<Long>> addItem(@RequestBody Todo todo) {
        return ResponseEntity.ok(accessor.addTodo(todo));
    }

    @PreAuthorize("@customAuthorization.validateToken()")
    @DeleteMapping(ApiRoutes.TodoSubRoute.DELETE_ITEM)
    public ResponseEntity<Response<Boolean>> deleteItem(@RequestParam long id) {
        return ResponseEntity.ok(accessor.deleteTodo(id));
    }

    @PreAuthorize("@customAuthorization.validateToken()")
    @DeleteMapping(ApiRoutes.TodoSubRoute.TOGGLE_DONE)
    public ResponseEntity<Response<Boolean>> toggleTodo(@RequestParam long id) {
        return ResponseEntity.ok(accessor.toggleTodo(id));
    }

    @PreAuthorize("@customAuthorization.validateToken()")
    @GetMapping(ApiRoutes.TodoSubRoute.GET_ITEMS)
    public ResponseEntity<Response<List<Todo>>> getTodoItems() {
        return ResponseEntity.ok(accessor.getItems());
    }
}