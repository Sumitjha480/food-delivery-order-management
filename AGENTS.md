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

AI assistance was used for:

1. Understanding the assignment requirements.
2. Breaking the implementation into incremental sections.
3. Designing entities, DTOs, repositories, services, and controllers.
4. Adding Spring Security RBAC.
5. Adding ownership checks for restaurant owners, customers, and delivery partners.
6. Adding order lifecycle APIs.
7. Adding concurrency-safe stock handling.
8. Adding fake payment simulation.
9. Adding asynchronous notification events.
10. Adding reviews after delivered orders.
11. Adding order status history.
12. Adding filtered order APIs.
13. Creating integration test classes.
14. Preparing README and Postman test collection.

---

## Human Review and Validation

The generated code was not accepted blindly.

The following checks were performed during development:

- Code was added section by section.
- The application was restarted after each major change.
- Compile-time issues were fixed.
- Runtime errors were tested through curl/Postman.
- Security matcher ordering was verified.
- Database schema issues were handled during local testing.
- Order lifecycle transitions were manually verified.
- Negative authorization cases were manually verified.
- Integration tests were added for core flows.

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

## AI-Generated Artifacts

The following project-support artifacts were also prepared with AI assistance:

- `README.md`
- `AGENTS.md`
- `CLAUDE.md`
- `SKILLS_USED.md`
- `SUBMISSION_CHECKLIST.md`
- Postman collection
- Postman environment
- Integration test module

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
