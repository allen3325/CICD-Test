package com.example.todo.controller;

import com.example.todo.dto.TodoRequest;
import com.example.todo.exception.TodoNotFoundException;
import com.example.todo.model.Todo;
import com.example.todo.service.TodoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TodoController.class)
class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TodoService todoService;

    @Test
    void testGetAllTodos_Success() throws Exception {
        // Arrange
        Todo todo1 = createTodo(1L, "Test Todo 1", "Description 1", false);
        Todo todo2 = createTodo(2L, "Test Todo 2", "Description 2", true);
        List<Todo> todos = Arrays.asList(todo1, todo2);
        when(todoService.getAllTodos()).thenReturn(todos);

        // Act & Assert
        mockMvc.perform(get("/api/todos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Test Todo 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].title").value("Test Todo 2"));

        verify(todoService, times(1)).getAllTodos();
    }

    @Test
    void testGetTodoById_Success() throws Exception {
        // Arrange
        Todo todo = createTodo(1L, "Test Todo", "Test Description", false);
        when(todoService.getTodoById(1L)).thenReturn(todo);

        // Act & Assert
        mockMvc.perform(get("/api/todos/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Todo"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.completed").value(false));

        verify(todoService, times(1)).getTodoById(1L);
    }

    @Test
    void testGetTodoById_NotFound() throws Exception {
        // Arrange
        when(todoService.getTodoById(999L)).thenThrow(new TodoNotFoundException(999L));

        // Act & Assert
        mockMvc.perform(get("/api/todos/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Todo not found with id: 999"));

        verify(todoService, times(1)).getTodoById(999L);
    }

    @Test
    void testCreateTodo_Success() throws Exception {
        // Arrange
        TodoRequest request = new TodoRequest("New Todo", "New Description", false);
        Todo createdTodo = createTodo(1L, "New Todo", "New Description", false);
        when(todoService.createTodo(any(TodoRequest.class))).thenReturn(createdTodo);

        // Act & Assert
        mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("New Todo"))
                .andExpect(jsonPath("$.description").value("New Description"))
                .andExpect(jsonPath("$.completed").value(false));

        verify(todoService, times(1)).createTodo(any(TodoRequest.class));
    }

    @Test
    void testCreateTodo_InvalidInput() throws Exception {
        // Arrange - title is blank
        TodoRequest request = new TodoRequest("", "Description", false);

        // Act & Assert
        mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors").isArray());

        verify(todoService, never()).createTodo(any(TodoRequest.class));
    }

    @Test
    void testUpdateTodo_Success() throws Exception {
        // Arrange
        TodoRequest request = new TodoRequest("Updated Todo", "Updated Description", true);
        Todo updatedTodo = createTodo(1L, "Updated Todo", "Updated Description", true);
        when(todoService.updateTodo(eq(1L), any(TodoRequest.class))).thenReturn(updatedTodo);

        // Act & Assert
        mockMvc.perform(put("/api/todos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Updated Todo"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.completed").value(true));

        verify(todoService, times(1)).updateTodo(eq(1L), any(TodoRequest.class));
    }

    @Test
    void testDeleteTodo_Success() throws Exception {
        // Arrange
        doNothing().when(todoService).deleteTodo(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/todos/1"))
                .andExpect(status().isNoContent());

        verify(todoService, times(1)).deleteTodo(1L);
    }

    // Helper method to create Todo objects
    private Todo createTodo(Long id, String title, String description, Boolean completed) {
        Todo todo = new Todo();
        todo.setId(id);
        todo.setTitle(title);
        todo.setDescription(description);
        todo.setCompleted(completed);
        todo.setCreatedAt(LocalDateTime.now());
        todo.setUpdatedAt(LocalDateTime.now());
        return todo;
    }
}
