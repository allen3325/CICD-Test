package com.example.todo.dto;

import com.example.todo.model.Todo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodoResponse {

    private Long id;
    private String title;
    private String description;
    private Boolean completed;
    private String createdAt;
    private String updatedAt;

    public static TodoResponse fromEntity(Todo todo) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        return new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getDescription(),
                todo.getCompleted(),
                todo.getCreatedAt().format(formatter),
                todo.getUpdatedAt().format(formatter)
        );
    }
}
