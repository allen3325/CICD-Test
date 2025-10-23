package com.example.todo.integration;

import com.example.todo.dto.TodoRequest;
import com.example.todo.dto.TodoResponse;
import com.example.todo.model.Todo;
import com.example.todo.repository.TodoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Todo API using Testcontainers with PostgreSQL.
 * Tests the full stack including database operations.
 */
@AutoConfigureMockMvc
public class TodoIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        todoRepository.deleteAll();
    }

    @Test
    void testCreateTodo_ShouldPersistInDatabase() throws Exception {
        // Given
        TodoRequest request = new TodoRequest();
        request.setTitle("Integration Test Todo");
        request.setDescription("Test Description");
        request.setCompleted(false);

        // When
        String response = mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Integration Test Todo"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.completed").value(false))
                .andReturn()
                .getResponse()
                .getContentAsString();

        TodoResponse todoResponse = objectMapper.readValue(response, TodoResponse.class);

        // Then - Verify in database
        Optional<Todo> savedTodo = todoRepository.findById(todoResponse.getId());
        assertThat(savedTodo).isPresent();
        assertThat(savedTodo.get().getTitle()).isEqualTo("Integration Test Todo");
        assertThat(savedTodo.get().getDescription()).isEqualTo("Test Description");
        assertThat(savedTodo.get().getCompleted()).isFalse();
    }

    @Test
    void testGetAllTodos_ShouldReadFromDatabase() throws Exception {
        // Given - Insert test data directly into database
        Todo todo1 = new Todo();
        todo1.setTitle("Todo 1");
        todo1.setDescription("Description 1");
        todo1.setCompleted(false);

        Todo todo2 = new Todo();
        todo2.setTitle("Todo 2");
        todo2.setDescription("Description 2");
        todo2.setCompleted(true);

        todoRepository.save(todo1);
        todoRepository.save(todo2);

        // When & Then
        mockMvc.perform(get("/api/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Todo 1"))
                .andExpect(jsonPath("$[1].title").value("Todo 2"));
    }

    @Test
    void testGetTodoById_ShouldReturnCorrectTodo() throws Exception {
        // Given
        Todo todo = new Todo();
        todo.setTitle("Find Me");
        todo.setDescription("Test Description");
        todo.setCompleted(false);
        Todo savedTodo = todoRepository.save(todo);

        // When & Then
        mockMvc.perform(get("/api/todos/" + savedTodo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedTodo.getId()))
                .andExpect(jsonPath("$.title").value("Find Me"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.completed").value(false));
    }

    @Test
    void testUpdateTodo_ShouldUpdateInDatabase() throws Exception {
        // Given
        Todo todo = new Todo();
        todo.setTitle("Original Title");
        todo.setDescription("Original Description");
        todo.setCompleted(false);
        Todo savedTodo = todoRepository.save(todo);

        TodoRequest updateRequest = new TodoRequest();
        updateRequest.setTitle("Updated Title");
        updateRequest.setDescription("Updated Description");
        updateRequest.setCompleted(true);

        // When
        mockMvc.perform(put("/api/todos/" + savedTodo.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.completed").value(true));

        // Then - Verify in database
        Optional<Todo> updatedTodo = todoRepository.findById(savedTodo.getId());
        assertThat(updatedTodo).isPresent();
        assertThat(updatedTodo.get().getTitle()).isEqualTo("Updated Title");
        assertThat(updatedTodo.get().getDescription()).isEqualTo("Updated Description");
        assertThat(updatedTodo.get().getCompleted()).isTrue();
    }

    @Test
    void testDeleteTodo_ShouldRemoveFromDatabase() throws Exception {
        // Given
        Todo todo = new Todo();
        todo.setTitle("To Be Deleted");
        todo.setDescription("Delete Me");
        todo.setCompleted(false);
        Todo savedTodo = todoRepository.save(todo);

        // Verify it exists
        assertThat(todoRepository.findById(savedTodo.getId())).isPresent();

        // When
        mockMvc.perform(delete("/api/todos/" + savedTodo.getId()))
                .andExpect(status().isNoContent());

        // Then - Verify it's deleted from database
        Optional<Todo> deletedTodo = todoRepository.findById(savedTodo.getId());
        assertThat(deletedTodo).isEmpty();
    }

    @Test
    void testFindByCompleted_ShouldFilterCorrectly() throws Exception {
        // Given - Create todos with different completion status
        Todo completedTodo1 = new Todo();
        completedTodo1.setTitle("Completed 1");
        completedTodo1.setDescription("Done");
        completedTodo1.setCompleted(true);

        Todo completedTodo2 = new Todo();
        completedTodo2.setTitle("Completed 2");
        completedTodo2.setDescription("Done");
        completedTodo2.setCompleted(true);

        Todo incompleteTodo = new Todo();
        incompleteTodo.setTitle("Incomplete");
        incompleteTodo.setDescription("Not Done");
        incompleteTodo.setCompleted(false);

        todoRepository.save(completedTodo1);
        todoRepository.save(completedTodo2);
        todoRepository.save(incompleteTodo);

        // When & Then - Test completed=true filter
        mockMvc.perform(get("/api/todos?completed=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].completed").value(true))
                .andExpect(jsonPath("$[1].completed").value(true));

        // When & Then - Test completed=false filter
        mockMvc.perform(get("/api/todos?completed=false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].completed").value(false))
                .andExpect(jsonPath("$[0].title").value("Incomplete"));

        // Verify database state
        List<Todo> completedTodos = todoRepository.findByCompleted(true);
        List<Todo> incompleteTodos = todoRepository.findByCompleted(false);
        assertThat(completedTodos).hasSize(2);
        assertThat(incompleteTodos).hasSize(1);
    }
}
