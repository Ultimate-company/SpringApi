package com.example.SpringApi.Services.CarrierDatabase;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Repository.CarrierDatabase.TodoRepository;
import com.example.SpringApi.Repository.CentralDatabase.CarrierRepository;
import com.example.SpringApi.Services.BaseDataAccessor;
import com.example.SpringApi.Services.CentralDatabase.UserLogDataAccessor;
import com.example.SpringApi.SuccessMessages;
import jakarta.servlet.http.HttpServletRequest;
import org.example.ApiRoutes;
import com.example.SpringApi.DatabaseModels.CarrierDatabase.Todo;
import org.example.CommonHelpers.HelperUtils;
import org.example.Models.ResponseModels.Response;
import org.example.Translators.CarrierDatabaseTranslators.Interfaces.ITodoListSubTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TodoDataAccessor extends BaseDataAccessor implements ITodoListSubTranslator {

    private final TodoRepository todoRepository;
    private final UserLogDataAccessor userLogDataAccessor;
    @Autowired
    public TodoDataAccessor(HttpServletRequest request,
                            CarrierRepository carrierRepository,
                            TodoRepository todoRepository,
                            UserLogDataAccessor userLogDataAccessor) {
        super(request, carrierRepository);
        this.todoRepository = todoRepository;
        this.userLogDataAccessor = userLogDataAccessor;
    }

    @Override
    public Response<List<org.example.Models.CommunicationModels.CarrierModels.Todo>> getItems() {
        return new Response<>(true, SuccessMessages.TodoSuccessMessages.GetTodoItems,
                HelperUtils.copyFields(todoRepository.findAllByUserId(getUserId()).stream()
                .sorted(Comparator.comparing(Todo::getTodoId).reversed())
                .collect(Collectors.toList()), org.example.Models.CommunicationModels.CarrierModels.Todo.class) );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<Long> addTodo(org.example.Models.CommunicationModels.CarrierModels.Todo todo) {
        todo.setUserId(getUserId());
        Todo savedTodo = todoRepository.save(HelperUtils.copyFields(todo, Todo.class));
        userLogDataAccessor.logData(getUserId(),
                SuccessMessages.TodoSuccessMessages.InsertTodo + " " + savedTodo.getTodoId(),
                ApiRoutes.TodoSubRoute.ADD_ITEM);
        return new Response<>(true, SuccessMessages.TodoSuccessMessages.InsertTodo, savedTodo.getTodoId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<Boolean> deleteTodo(long toDoId) {
        todoRepository.deleteById(toDoId);
        userLogDataAccessor.logData(getUserId(),
                SuccessMessages.TodoSuccessMessages.DeleteTodo + " " + toDoId,
                ApiRoutes.TodoSubRoute.DELETE_ITEM);
        return new Response<>(true, SuccessMessages.TodoSuccessMessages.DeleteTodo, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<Boolean> toggleTodo(long toDoId) {
        Optional<Todo> todo = todoRepository.findById(toDoId);
        if(todo.isEmpty()){
            return new Response<>(false, ErrorMessages.TodoErrorMessages.InvalidId, false);
        }

        todo.get().setDone(!todo.get().isDone());
        todoRepository.save(todo.get());
        userLogDataAccessor.logData(getUserId(),
                SuccessMessages.TodoSuccessMessages.ToggleTodo + " " + toDoId,
                ApiRoutes.TodoSubRoute.TOGGLE_DONE);
        return new Response<>(true, SuccessMessages.TodoSuccessMessages.ToggleTodo, true);
    }
}