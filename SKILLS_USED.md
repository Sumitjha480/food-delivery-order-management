# SKILLS_USED.md

## Skills and Technologies Used

This file summarizes the technical skills, frameworks, and concepts used in the Food Delivery Order Management project.

---

## Programming Language

### Java 21

Used for:

- Object-oriented domain modeling
- Service-layer business logic
- DTOs
- Exception handling
- Integration tests

---

## Backend Framework

### Spring Boot

Used for:

- Application bootstrapping
- REST API development
- Dependency injection
- Configuration management
- Integration testing support

### Spring MVC

Used for:

- REST controllers
- Request mapping
- Path variables
- Query parameters
- Request/response handling

### Spring Security

Used for:

- Basic Authentication
- Role-based access control
- API authorization
- Ownership-aware service checks

### Spring Data JPA

Used for:

- Repository abstraction
- CRUD persistence
- Custom JPQL queries
- Entity relationships
- Pessimistic locking

### Hibernate

Used as the JPA provider for:

- ORM mapping
- Lazy loading
- Schema generation during development
- Transactional persistence

---

## Database

### PostgreSQL

Used as the primary application database.

### H2

Used for integration testing with PostgreSQL compatibility mode.

---

## Validation

### Jakarta Bean Validation

Used for request validation:

- `@NotNull`
- `@NotBlank`
- `@Positive`
- `@PositiveOrZero`
- `@Min`
- `@Max`
- `@Email`
- `@Size`
- `@Valid`

---

## Transaction Management

### Spring Transactions

Used for:

- Atomic order placement
- Payment rollback behavior
- Stock consistency
- Delivery partner assignment consistency
- Review creation consistency

Important annotation:

```java
@Transactional
```

---

## Concurrency Control

### Pessimistic Locking

Used for:

- Preventing stock overselling
- Preventing multiple delivery partners from claiming the same order
- Preventing a busy partner from being assigned twice

Implemented with:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
```

---

## Security Concepts

Implemented:

- Basic Auth
- RBAC
- Service-layer ownership authorization
- Admin bypass permissions
- Customer self-profile access
- Restaurant owner scoped access
- Delivery partner scoped access

---

## REST API Design

Skills used:

- Resource-based endpoint design
- HTTP methods
- Status codes
- DTO-based request/response design
- Query filtering
- Lifecycle action endpoints
- Error response consistency

---

## Asynchronous Processing

### Spring Events and `@Async`

Used for:

- Order status notification fan-out
- Delivery partner assignment notifications
- Post-commit notification processing

Concepts used:

- Application events
- Transactional event listeners
- Async execution
- New transaction propagation for listeners

---

## Testing

### JUnit 5

Used for integration tests.

### Spring Boot Test

Used for loading the full application context.

### MockMvc

Used for testing REST APIs without starting a real server.

### AssertJ

Used for readable assertions.

### H2 Test Database

Used for isolated repeatable tests.

Test categories:

- Order placement
- Payment rollback
- Stock validation
- Concurrency
- Lifecycle
- Authorization
- Ownership
- Reviews
- Notifications
- Filtering

---

## API Testing

### Postman

Used for:

- Full E2E workflow testing
- Environment variable capture
- RBAC testing
- Negative cases
- Manual reviewer-friendly testing

Artifacts:

```text
food_delivery_complete_e2e.postman_collection.json
food_delivery_local.postman_environment.json
```

---

## Documentation

Created:

- `README.md`
- `AGENTS.md`
- `CLAUDE.md`
- `SKILLS_USED.md`
- `SUBMISSION_CHECKLIST.md`

Documentation covers:

- Setup
- API usage
- Demo users
- Architecture
- Order flow
- Tests
- Troubleshooting
- Assumptions
- Future improvements

---

## Build Tool

### Maven

Used for:

- Dependency management
- Build lifecycle
- Test execution
- Packaging

Commands:

```bash
./mvnw spring-boot:run
./mvnw test
./mvnw clean package
```

---

## Design Concepts Applied

- Layered architecture
- DTO pattern
- Repository pattern
- Service-layer transaction boundaries
- Domain-driven entity modeling
- Ownership-based authorization
- Optimistic API design with pessimistic DB locking
- Audit trail
- Event-driven notifications
- Centralized exception handling

---

## Main Learning/Implementation Areas

The project demonstrates practical knowledge of:

```text
Spring Boot REST APIs
Spring Security
Spring Data JPA
Database relationships
Transaction management
Concurrent request safety
Role-based authorization
Ownership checks
Async event handling
Integration testing
Postman E2E testing
Technical documentation
```
