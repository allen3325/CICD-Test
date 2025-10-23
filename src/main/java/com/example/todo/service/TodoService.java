package com.example.todo.service;

import com.example.todo.dto.TodoRequest;
import com.example.todo.model.Todo;

import java.util.List;

public interface TodoService {

    List<Todo> getAllTodos();

    Todo getTodoById(Long id);

    Todo createTodo(TodoRequest request);

    Todo updateTodo(Long id, TodoRequest request);

    void deleteTodo(Long id);
}
