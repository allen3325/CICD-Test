# Todo API - CICD Teaching Demo

A simple RESTful Todo API built with Spring Boot, designed for teaching CI/CD concepts with comprehensive testing and automated workflows.

![CI](https://github.com/allen3325/CICD-Test/workflows/CI/badge.svg)
[![codecov](https://codecov.io/gh/allen3325/CICD-Test/branch/main/graph/badge.svg)](https://codecov.io/gh/allen3325/CICD-Test)
![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen)
![License](https://img.shields.io/badge/License-MIT-blue.svg)

## Features

This API provides 5 RESTful endpoints for managing todo items:

| HTTP Method | Endpoint | Description |
|-------------|----------|-------------|
| GET | `/api/todos` | Retrieve all todos |
| GET | `/api/todos/{id}` | Retrieve a specific todo |
| POST | `/api/todos` | Create a new todo |
| PUT | `/api/todos/{id}` | Update an existing todo |
| DELETE | `/api/todos/{id}` | Delete a todo |

## Tech Stack

- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Database**: H2 (in-memory)
- **Build Tool**: Maven
- **Testing**: JUnit 5, MockMvc
- **Code Coverage**: JaCoCo (>= 70%)
- **CI/CD**: GitHub Actions
- **Coverage Reporting**: Codecov

## Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.8+

### Run Application

```bash
mvn spring-boot:run
```

Application runs on: http://localhost:8080

### Access H2 Console

Navigate to: http://localhost:8080/h2-console

- JDBC URL: `jdbc:h2:mem:tododb`
- Username: `sa`
- Password: (leave blank)

### Run Tests

```bash
mvn test
```

### Generate Coverage Report

```bash
mvn verify
```

View coverage report: `target/site/jacoco/index.html`

## API Usage Examples

### Create a Todo

```bash
curl -X POST http://localhost:8080/api/todos \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Learn Spring Boot",
    "description": "Complete the Spring Boot tutorial",
    "completed": false
  }'
```

### Get All Todos

```bash
curl http://localhost:8080/api/todos
```

### Get a Specific Todo

```bash
curl http://localhost:8080/api/todos/1
```

### Update a Todo

```bash
curl -X PUT http://localhost:8080/api/todos/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Learn Spring Boot",
    "description": "Complete the Spring Boot tutorial",
    "completed": true
  }'
```

### Delete a Todo

```bash
curl -X DELETE http://localhost:8080/api/todos/1
```

## Testing Strategy

This project uses **Unit Testing** with Mock objects:

- **Controller Layer Tests**: Using `@WebMvcTest` to test HTTP interactions
- **Service Layer Mocking**: All service dependencies are mocked with Mockito
- **Coverage Requirement**: Minimum 70% line coverage enforced by JaCoCo

### Test Cases

The test suite includes 7 comprehensive test cases:

1. `testGetAllTodos_Success` - Verify retrieving all todos
2. `testGetTodoById_Success` - Verify retrieving a specific todo
3. `testGetTodoById_NotFound` - Verify 404 error handling
4. `testCreateTodo_Success` - Verify todo creation
5. `testCreateTodo_InvalidInput` - Verify validation error handling
6. `testUpdateTodo_Success` - Verify todo updates
7. `testDeleteTodo_Success` - Verify todo deletion

## Project Structure

```
todo-api/
├── src/
│   ├── main/
│   │   ├── java/com/example/todo/
│   │   │   ├── controller/          # REST Controllers
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   ├── exception/           # Exception Handlers
│   │   │   ├── model/               # JPA Entities
│   │   │   ├── repository/          # Data Repositories
│   │   │   └── service/             # Business Logic
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/example/todo/
│           └── controller/          # Controller Tests
├── .github/
│   └── workflows/
│       └── ci.yml                   # GitHub Actions Workflow
├── pom.xml                          # Maven Configuration
└── README.md
```

## CI/CD Pipeline

This project uses GitHub Actions for continuous integration:

- **Triggers**: Push to any branch, Pull Request to main
- **Steps**:
  1. Checkout code
  2. Setup Java 17
  3. Cache Maven dependencies
  4. Run tests
  5. Generate JaCoCo coverage report

## Error Responses

| Scenario | HTTP Status | Response Body |
|----------|-------------|---------------|
| Resource not found | 404 | `{"error": "Todo not found with id: {id}"}` |
| Validation failure | 400 | `{"errors": ["title must not be blank", ...]}` |
| Server error | 500 | `{"error": "Internal server error"}` |

