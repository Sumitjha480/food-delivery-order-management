# AGENTS.md

## AI Usage and Development Notes

This project was developed with AI assistance for code design, implementation planning, test planning, README drafting, and Postman collection preparation.

The final code was reviewed, compiled, and tested locally by the developer before submission.

---

## Project

**Food Delivery Order Management**

A Spring Boot REST API for managing a multi-restaurant food delivery platform with:

- City management
- Restaurant ownership
- Menu management
- Customer order placement
- Payment simulation
- Atomic stock deduction
- Delivery partner claim/assignment
- Order lifecycle
- Async notifications
- Reviews
- Role-based access control
- Ownership-scoped APIs
- Order status history
- Integration tests

---

## AI Tools Used

AI was used as a development assistant during the implementation of this project. The assistance primarily focused on improving productivity and validating implementation ideas rather than generating the entire application.

AI assistance was used for:

1. Clarifying assignment requirements and discussing possible implementation approaches.
2. Suggesting improvements to the overall project structure and package organization.
3. Reviewing service logic and identifying potential edge cases.
4. Explaining Spring Boot, Spring Security, and JPA concepts when required.
5. Suggesting improvements to validation, exception handling, and API design.
6. Helping debug compile-time and runtime issues encountered during development.
7. Reviewing transaction handling, concurrency, and locking strategies.
8. Assisting in preparing project documentation (README, Postman guide, and supporting documents).
9. Assisting in creating sample test cases and Postman requests for verification.

---

## Development and Validation

The project was implemented incrementally and verified throughout development.

The development process included:

- Designing the database model and entity relationships.
- Implementing REST APIs module by module.
- Testing each feature manually using curl and Postman.
- Fixing compile-time and runtime issues during implementation.
- Verifying role-based access control and ownership restrictions.
- Testing order lifecycle transitions and business rules.
- Verifying payment rollback, stock consistency, and notification flow.
- Running integration tests and resolving failing scenarios before final submission.
---

## Important Implementation Decisions

### Authentication

The project uses Spring Security Basic Authentication for simplicity.

Demo users are seeded using `DataInitializer`.

### Authorization

Authorization is implemented in two layers:

1. Route-level RBAC in `SecurityConfig`.
2. Ownership-level checks in service layer through `CurrentUserService`.

This prevents users from acting on resources they do not own.

### Ownership Model

The following ownership mappings are implemented:

- `Restaurant.owner -> User`
- `Customer.user -> User`
- `DeliveryPartner.user -> User`

These mappings enforce:

- Restaurant owner can manage only owned restaurants.
- Customer can order/review only for their own profile.
- Delivery partner can claim/update only through their own partner profile.

### Transactions

Order creation is transactional.

If payment fails after stock is reduced, the transaction rolls back and stock remains unchanged.

### Locking

Pessimistic locking is used for:

- Menu item stock deduction.
- Order delivery partner assignment.
- Delivery partner claim contention.

### Notifications

Notifications are generated asynchronously after transaction commit.

This prevents notification creation from interfering with the main order transaction.

### Status History

Order lifecycle changes are stored in `OrderStatusHistory` for auditability.

---

## Common Commands

Run the application:

```bash
./mvnw spring-boot:run
```

Run tests:

```bash
./mvnw test
```

Package the application:

```bash
./mvnw clean package
```

---

## Demo Credentials

| Role | Username | Password |
|---|---|---|
| Admin | `admin` | `admin123` |
| Restaurant Owner | `owner` | `owner123` |
| Restaurant Owner 2 | `owner2` | `owner2123` |
| Customer | `customer` | `customer123` |
| Customer 2 | `customer2` | `customer2123` |
| Delivery Partner | `partner` | `partner123` |
| Delivery Partner 2 | `partner2` | `partner2123` |

---

## Documentation Support

AI assistance was also used to help draft project documentation and testing resources, including:

- README.md
- AGENTS.md
- CLAUDE.md
- SKILLS_USED.md
- SUBMISSION_CHECKLIST.md
- Postman collection and environment

All documentation was reviewed and updated to accurately reflect the implemented project.

---

## Verification Strategy

The implementation was verified through:

1. Manual curl flows.
2. Postman E2E collection.
3. Spring Boot integration tests.
4. Negative RBAC/ownership checks.
5. Transaction rollback tests.
6. Concurrency tests.

---

## Notes for Reviewers

The system intentionally uses a fake payment service.

Tokens that fail payment:

```text
FAIL
DECLINE
INVALID
```

Any other token is treated as successful.

The notification mechanism stores notification rows in the database instead of sending real email/SMS/push messages.

The project uses `ddl-auto: update` for local development. For production readiness, Flyway or Liquibase migrations should be added.
