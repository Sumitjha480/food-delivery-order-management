# Food Delivery Order Management

A Spring Boot based food delivery order management system for managing restaurants, cities, menus, customer orders, delivery partner assignment, payment simulation, order lifecycle tracking, reviews, notifications, role-based access control, and ownership-scoped APIs.

This project models a multi-restaurant food delivery platform where customers can browse restaurants and menu items, place paid orders, restaurant owners can accept and prepare orders, delivery partners can claim and deliver orders, and administrators can manage platform data.

---

## Demo
https://www.loom.com/share/f44c39e70cb142c28620a021411854ff

---

## Table of Contents

1. [Features](#features)
2. [Tech Stack](#tech-stack)
3. [High-Level Architecture](#high-level-architecture)
4. [Core Domain Model](#core-domain-model)
5. [Order Lifecycle](#order-lifecycle)
6. [Roles and Permissions](#roles-and-permissions)
7. [Authentication](#authentication)
8. [Local Setup](#local-setup)
9. [Database Setup](#database-setup)
10. [Running the Application](#running-the-application)
11. [Demo Users](#demo-users)
12. [Postman Collection](#postman-collection)
13. [Main API Groups](#main-api-groups)
14. [Important End-to-End Flows](#important-end-to-end-flows)
15. [Concurrency and Transaction Handling](#concurrency-and-transaction-handling)
16. [Payment Simulation](#payment-simulation)
17. [Async Notifications](#async-notifications)
18. [Order Status History / Audit Trail](#order-status-history--audit-trail)
19. [Reviews](#reviews)
20. [Validation and Error Handling](#validation-and-error-handling)
21. [Testing](#testing)
22. [Project Structure](#project-structure)
23. [Assumptions](#assumptions)
24. [Troubleshooting](#troubleshooting)
25. [Future Improvements](#future-improvements)

---

## Features

### Restaurant and City Management

- Admin can create, update, list, and delete cities.
- Restaurants are linked to cities.
- Restaurants are assigned to restaurant owner users.
- Customers can browse restaurants by city.
- Restaurant owners can manage only their own restaurant menu.
- Admin can manage all restaurants and menus.

### Menu Management

- Restaurant owners can create, update, and delete menu items for their own restaurants.
- Menu item stock is tracked.
- Stock can be zero.
- Menu items are validated before order placement.
- Menu item stock is reduced atomically during order creation.

### Customer Management

- Each customer profile is linked to a login user.
- Customers can view and update their own profile using `/customers/me`.
- Admin can manage all customer records.
- Customers cannot place orders using another customer's `customerId`.

### Order Placement

- Customers can place orders for menu items from a restaurant.
- Orders support multiple order items.
- Duplicate menu item entries in the same request are merged before stock deduction.
- Order placement validates:
  - Customer ownership
  - Restaurant existence
  - Menu item existence
  - Menu item belongs to the selected restaurant
  - Sufficient stock
  - Payment success
- Order placement is transactional.

### Payment Simulation

- A fake payment gateway simulates payment success and failure.
- Payment failure rolls back order creation and stock deduction.
- Successful payment stores payment method, reference, and paid timestamp.
- Rejected paid orders are marked as refunded.

### Order Lifecycle

Supported lifecycle:

```text
PLACED -> ACCEPTED -> PREPARING -> OUT_FOR_DELIVERY -> DELIVERED
```

Additional terminal states:

```text
REJECTED
CANCELLED
```

Implemented lifecycle operations:

- Restaurant owner accepts order.
- Restaurant owner rejects order.
- Restaurant owner marks order as preparing.
- Delivery partner marks order as out for delivery.
- Delivery partner marks order as delivered.
- Invalid status transitions are blocked.

### Delivery Partner Assignment

- Delivery partners are linked to login users.
- Delivery partners belong to a city.
- Restaurant owner or admin can assign an available partner.
- Delivery partner can claim an eligible order.
- Delivery partner can act only as their own delivery partner profile.
- Delivery partner city must match restaurant city.
- Busy partners cannot claim another order.
- Partner status changes:
  - `AVAILABLE` -> `BUSY` when assigned/claimed.
  - `BUSY` -> `AVAILABLE` when order is delivered.

### Ownership-Scoped Access

The application enforces ownership beyond simple role checks:

- Restaurant owner can manage only their own restaurant.
- Customer can use only their own customer profile.
- Delivery partner can act only as their own partner profile.
- Admin can manage all records.

### Filtered Order APIs

Orders can be filtered by:

- Status
- Restaurant
- Customer
- Delivery partner

Examples:

```http
GET /orders?status=DELIVERED
GET /orders?restaurantId=1
GET /orders?customerId=1
GET /orders?deliveryPartnerId=1
```

Visibility is scoped by role:

- Admin sees all.
- Customer sees only own orders.
- Restaurant owner sees only orders for owned restaurants.
- Delivery partner sees only assigned orders.

### Order Status History

Every major order status transition creates an audit entry.

History includes:

- Order ID
- Old status
- New status
- Changed by username
- Note
- Timestamp

Endpoint:

```http
GET /orders/{id}/history
```

### Async Notifications

Order events create asynchronous notifications for relevant parties:

- Customer
- Restaurant
- Delivery partner

Notifications are generated after transaction commit using Spring events and async listeners.

### Reviews

- Customers can review only delivered orders.
- One review is allowed per order.
- Customers cannot review another customer's order.
- Rating must be between 1 and 5.
- Admin can read/manage reviews.

### Validation and Error Handling

- Request DTO validation with Jakarta Bean Validation.
- Centralized exception handling.
- Consistent API error response format.
- Handles:
  - Validation failures
  - Bad requests
  - Entity not found
  - Access denied
  - Data integrity issues
  - Locking/concurrency issues

---

## Tech Stack

| Area | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot |
| Web | Spring MVC |
| Security | Spring Security Basic Auth |
| Persistence | Spring Data JPA / Hibernate |
| Database | PostgreSQL |
| Test Database | H2 |
| Validation | Jakarta Bean Validation |
| Async Events | Spring Application Events + `@Async` |
| Build Tool | Maven |
| Testing | JUnit 5, Spring Boot Test, MockMvc |
| Boilerplate Reduction | Lombok |

---

## High-Level Architecture

The application follows a layered architecture:

```text
Controller Layer
    |
    v
Service Layer
    |
    v
Repository Layer
    |
    v
Database
```

### Controller Layer

Responsible for:

- Exposing REST APIs
- Accepting request DTOs
- Returning response DTOs
- Applying route-level security through Spring Security

### Service Layer

Responsible for:

- Business logic
- Transaction boundaries
- Ownership checks
- Order lifecycle validation
- Stock deduction and restoration
- Payment simulation
- Publishing events
- Mapping entities to DTOs

### Repository Layer

Responsible for:

- Database access
- JPA queries
- Pessimistic locking for stock and delivery partner contention
- Filtered order search

### Event Layer

Responsible for:

- Asynchronous notification fan-out
- Post-commit event processing

---

## Core Domain Model

### User

Represents an authenticated login user.

Important fields:

```text
username
password
role
```

Supported roles:

```text
ADMIN
RESTAURANT_OWNER
CUSTOMER
DELIVERY_PARTNER
```

### City

Represents a serviceable city.

Important fields:

```text
name
status
```

Supported city statuses:

```text
ACTIVE
INACTIVE
```

### Restaurant

Represents a restaurant.

Important fields:

```text
name
address
city
cityEntity
owner
status
estimatedDeliveryTime
```

### MenuItem

Represents a restaurant menu item.

Important fields:

```text
restaurant
name
price
stock
```

### Customer

Represents a customer profile linked to a user.

Important fields:

```text
user
name
email
phone
address
```

### DeliveryPartner

Represents a delivery partner profile linked to a user.

Important fields:

```text
user
name
phone
city
status
```

Supported partner statuses:

```text
AVAILABLE
BUSY
OFFLINE
```

### Order

Represents a customer order.

Important fields:

```text
customer
restaurant
deliveryPartner
orderStatus
totalAmount
paymentStatus
paymentMethod
paymentReference
paidAt
refundedAt
acceptedAt
preparingAt
outForDeliveryAt
deliveredAt
rejectedAt
rejectionReason
```

### OrderItem

Represents a menu item inside an order.

Important fields:

```text
order
menuItem
quantity
price
```

### Review

Represents customer feedback for a delivered order.

Important fields:

```text
order
customer
restaurant
rating
comment
```

### Notification

Represents a message sent to a customer, restaurant, or delivery partner.

Important fields:

```text
order
recipientType
recipientId
recipientName
message
status
sentAt
readAt
```

### OrderStatusHistory

Represents an audit record for status changes.

Important fields:

```text
order
oldStatus
newStatus
changedByUsername
note
changedAt
```

---

## Order Lifecycle

The main lifecycle is:

```text
PLACED
  |
  v
ACCEPTED
  |
  v
PREPARING
  |
  v
OUT_FOR_DELIVERY
  |
  v
DELIVERED
```

Restaurant rejection is allowed from `PLACED`:

```text
PLACED -> REJECTED
```

### Lifecycle Rules

| Action | Required Current Status | Allowed Role |
|---|---:|---|
| Create order | N/A | Customer/Admin |
| Accept order | PLACED | Restaurant Owner/Admin |
| Reject order | PLACED | Restaurant Owner/Admin |
| Assign partner | ACCEPTED or PREPARING | Restaurant Owner/Admin |
| Claim order | ACCEPTED or PREPARING | Delivery Partner/Admin |
| Mark preparing | ACCEPTED | Restaurant Owner/Admin |
| Mark out for delivery | PREPARING | Assigned Delivery Partner/Admin |
| Mark delivered | OUT_FOR_DELIVERY | Assigned Delivery Partner/Admin |
| Review order | DELIVERED | Owning Customer/Admin |

---

## Roles and Permissions

### Admin

Admin can:

- Manage cities.
- Manage restaurants.
- Manage customers.
- Manage delivery partners.
- Manage all menu items.
- View all orders.
- Assign delivery partners.
- View notifications.
- View reviews.
- Delete records where supported.

### Restaurant Owner

Restaurant owner can:

- Manage menu items only for owned restaurants.
- Accept/reject orders only for owned restaurants.
- Mark owned restaurant orders as preparing.
- Assign delivery partners for owned restaurant orders.
- View orders for owned restaurants.
- View order history for owned restaurant orders.

Restaurant owner cannot:

- Manage another owner's restaurant.
- Accept another owner's order.
- View another owner's order details.

### Customer

Customer can:

- View/update own profile through `/customers/me`.
- Browse restaurants and menu items.
- Place orders using own customer profile.
- View own orders.
- View own order history.
- Review own delivered orders.

Customer cannot:

- Use another customer's `customerId`.
- List all customer records.
- Accept/reject orders.
- Claim delivery orders.
- Review someone else's order.

### Delivery Partner

Delivery partner can:

- Claim eligible orders using own delivery partner profile.
- View orders assigned to them.
- Mark assigned order out for delivery.
- Mark assigned order delivered.

Delivery partner cannot:

- Claim using another partner's `deliveryPartnerId`.
- Claim an order in another city.
- Claim another order while busy.
- Update delivery status of another partner's assigned order.

---

## Authentication

The application uses Spring Security Basic Authentication.

Example:

```bash
curl -i http://localhost:8080/restaurants \
  -u admin:admin123
```

All protected APIs require a valid username and password.

---

## Local Setup

### Prerequisites

Install:

- Java 21
- Maven
- PostgreSQL
- Git
- Postman, optional but recommended

Check Java version:

```bash
java -version
```

Expected major version:

```text
21
```

---

## Database Setup

Create a PostgreSQL database:

```sql
CREATE DATABASE food_delivery;
```

Create a PostgreSQL user if needed:

```sql
CREATE USER food_user WITH PASSWORD 'food_password';
GRANT ALL PRIVILEGES ON DATABASE food_delivery TO food_user;
```

Update `src/main/resources/application.yml` or `application.properties` according to your local credentials.

Example configuration:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/food_delivery
    username: food_user
    password: food_password

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

During development this project uses:

```yaml
spring.jpa.hibernate.ddl-auto: update
```

For production, replace this with Flyway or Liquibase migrations.

---

## Running the Application

From the project root:

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

The application starts on:

```text
http://localhost:8080
```

---

## Demo Users

The application seeds demo users through `DataInitializer`.

| Role | Username | Password |
|---|---|---|
| Admin | `admin` | `admin123` |
| Restaurant Owner | `owner` | `owner123` |
| Restaurant Owner 2 | `owner2` | `owner2123` |
| Customer | `customer` | `customer123` |
| Customer 2 | `customer2` | `customer2123` |
| Delivery Partner | `partner` | `partner123` |
| Delivery Partner 2 | `partner2` | `partner2123` |

If a user is missing, add it in `DataInitializer`.

---

## Postman Collection

A complete Postman collection and environment are included for end-to-end testing.

Recommended import files:

```text
food_delivery_complete_e2e.postman_collection.json
food_delivery_local.postman_environment.json
```

### How to Use

1. Start the application.
2. Open Postman.
3. Import the collection.
4. Import the environment.
5. Select the `Food Delivery Local` environment.
6. Run folders in order from top to bottom.

### Collection Coverage

The collection covers:

```text
Health and RBAC smoke tests
Setup data and ID capture
Full order lifecycle
Payment success
Restaurant owner acceptance
Delivery partner claim
Delivery status updates
Order history
Notifications
Reviews
Ownership negative cases
Partner city mismatch
Busy partner claim prevention
Order filtering
Validation failures
Payment failure
Insufficient stock
```

---

## Main API Groups

### Cities

| Method | Endpoint | Description | Roles |
|---|---|---|---|
| GET | `/cities` | List cities | All authenticated roles |
| GET | `/cities/{id}` | Get city | All authenticated roles |
| POST | `/cities` | Create city | Admin |
| PUT | `/cities/{id}` | Update city | Admin |
| DELETE | `/cities/{id}` | Delete city | Admin |

Example:

```bash
curl -i -X POST http://localhost:8080/cities \
  -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Delhi"
  }'
```

---

### Restaurants

| Method | Endpoint | Description | Roles |
|---|---|---|---|
| GET | `/restaurants` | List restaurants | All authenticated roles |
| GET | `/restaurants?cityId=1` | Filter restaurants by city | All authenticated roles |
| GET | `/restaurants/{id}` | Get restaurant | All authenticated roles |
| POST | `/restaurants` | Create restaurant | Admin |
| PUT | `/restaurants/{id}` | Update restaurant | Admin |
| DELETE | `/restaurants/{id}` | Delete restaurant | Admin |

Create restaurant:

```bash
curl -i -X POST http://localhost:8080/restaurants \
  -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Pizza Palace",
    "address": "Connaught Place",
    "cityId": 1,
    "ownerUsername": "owner",
    "estimatedDeliveryTime": 30
  }'
```

---

### Menu Items

| Method | Endpoint | Description | Roles |
|---|---|---|---|
| GET | `/restaurants/{restaurantId}/menu-items` | List menu items | All authenticated roles |
| POST | `/restaurants/{restaurantId}/menu-items` | Create menu item | Restaurant Owner/Admin |
| GET | `/menu-items/{id}` | Get menu item | All authenticated roles |
| PUT | `/menu-items/{id}` | Update menu item | Owning Restaurant Owner/Admin |
| DELETE | `/menu-items/{id}` | Delete menu item | Owning Restaurant Owner/Admin |

Create menu item:

```bash
curl -i -X POST http://localhost:8080/restaurants/1/menu-items \
  -u owner:owner123 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Margherita Pizza",
    "price": 299,
    "stock": 20
  }'
```

---

### Customers

| Method | Endpoint | Description | Roles |
|---|---|---|---|
| GET | `/customers/me` | Get current customer profile | Customer |
| PUT | `/customers/me` | Update current customer profile | Customer |
| GET | `/customers` | List customers | Admin |
| GET | `/customers/{id}` | Get customer | Admin |
| POST | `/customers` | Create customer profile | Admin |
| PUT | `/customers/{id}` | Update customer | Admin |
| DELETE | `/customers/{id}` | Delete customer | Admin |

Get current customer:

```bash
curl -i http://localhost:8080/customers/me \
  -u customer:customer123
```

---

### Delivery Partners

| Method | Endpoint | Description | Roles |
|---|---|---|---|
| GET | `/delivery-partners` | List delivery partners | Admin |
| GET | `/delivery-partners?cityId=1` | List partners by city | Admin |
| GET | `/delivery-partners/{id}` | Get partner | Admin |
| POST | `/delivery-partners` | Create partner profile | Admin |
| PUT | `/delivery-partners/{id}` | Update partner profile | Admin |
| DELETE | `/delivery-partners/{id}` | Delete partner profile | Admin |

Create delivery partner:

```bash
curl -i -X POST http://localhost:8080/delivery-partners \
  -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{
    "username": "partner",
    "name": "Ravi Partner",
    "phone": "9999990000",
    "cityId": 1,
    "status": "AVAILABLE"
  }'
```

---

### Orders

| Method | Endpoint | Description | Roles |
|---|---|---|---|
| POST | `/orders` | Create order | Customer/Admin |
| GET | `/orders` | List/filter scoped orders | All authenticated roles |
| GET | `/orders/{id}` | Get scoped order | All authenticated roles |
| GET | `/orders/{id}/history` | Get order status history | Scoped by role |
| PATCH | `/orders/{id}/accept` | Accept order | Owning Restaurant Owner/Admin |
| PATCH | `/orders/{id}/reject` | Reject order | Owning Restaurant Owner/Admin |
| PATCH | `/orders/{id}/preparing` | Mark preparing | Owning Restaurant Owner/Admin |
| PATCH | `/orders/{id}/assign-partner` | Assign delivery partner | Owning Restaurant Owner/Admin |
| PATCH | `/orders/{id}/claim` | Claim order | Delivery Partner/Admin |
| PATCH | `/orders/{id}/out-for-delivery` | Mark out for delivery | Assigned Delivery Partner/Admin |
| PATCH | `/orders/{id}/delivered` | Mark delivered | Assigned Delivery Partner/Admin |

Create order:

```bash
curl -i -X POST http://localhost:8080/orders \
  -u customer:customer123 \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "restaurantId": 1,
    "items": [
      {
        "menuItemId": 1,
        "quantity": 1
      }
    ],
    "payment": {
      "method": "UPI",
      "token": "PAY_OK"
    }
  }'
```

Accept order:

```bash
curl -i -X PATCH http://localhost:8080/orders/1/accept \
  -u owner:owner123
```

Claim order:

```bash
curl -i -X PATCH http://localhost:8080/orders/1/claim \
  -u partner:partner123 \
  -H "Content-Type: application/json" \
  -d '{
    "deliveryPartnerId": 1
  }'
```

Get history:

```bash
curl -i http://localhost:8080/orders/1/history \
  -u admin:admin123
```

---

### Reviews

| Method | Endpoint | Description | Roles |
|---|---|---|---|
| POST | `/orders/{orderId}/review` | Create review for delivered order | Owning Customer/Admin |
| GET | `/orders/{orderId}/review` | Get order review | Scoped roles |
| GET | `/reviews` | List reviews | Admin |
| GET | `/reviews/{id}` | Get review | Authenticated roles |
| GET | `/restaurants/{restaurantId}/reviews` | List restaurant reviews | Authenticated roles |
| GET | `/customers/{customerId}/reviews` | List customer reviews | Admin/Owning Customer |
| PUT | `/reviews/{id}` | Update review | Owning Customer/Admin |
| DELETE | `/reviews/{id}` | Delete review | Admin |

Create review:

```bash
curl -i -X POST http://localhost:8080/orders/1/review \
  -u customer:customer123 \
  -H "Content-Type: application/json" \
  -d '{
    "rating": 5,
    "comment": "Great food and fast delivery"
  }'
```

---

### Notifications

| Method | Endpoint | Description | Roles |
|---|---|---|---|
| GET | `/notifications` | List notifications | Authenticated roles |
| GET | `/notifications/order/{orderId}` | Notifications for an order | Authenticated roles |
| GET | `/notifications/recipient` | Notifications for recipient | Authenticated roles |
| PATCH | `/notifications/{id}/read` | Mark notification read | Authenticated roles |

Example:

```bash
curl -i http://localhost:8080/notifications/order/1 \
  -u admin:admin123
```

---

## Important End-to-End Flows

### Full Successful Order Flow

```text
1. Customer places order.
2. Payment succeeds.
3. Stock is reduced.
4. Order status is PLACED.
5. Restaurant owner accepts order.
6. Order status becomes ACCEPTED.
7. Restaurant owner marks preparing.
8. Delivery partner claims order.
9. Partner status becomes BUSY.
10. Delivery partner marks out for delivery.
11. Delivery partner marks delivered.
12. Partner status becomes AVAILABLE.
13. Customer creates review.
14. Notifications and history are available.
```

### Rejected Order Flow

```text
1. Customer places paid order.
2. Restaurant owner rejects order while status is PLACED.
3. Stock is restored.
4. Payment status becomes REFUNDED.
5. Order status becomes REJECTED.
6. History and notifications are created.
```

### Payment Failure Flow

```text
1. Customer places order with payment token FAIL.
2. Payment service returns failure.
3. Transaction rolls back.
4. No order is saved.
5. No order item is saved.
6. Stock remains unchanged.
```

### Delivery Partner Contention Flow

```text
1. Order is accepted.
2. Partner claims order.
3. Partner becomes BUSY.
4. Same partner tries to claim another order.
5. Request fails because partner is BUSY.
```

---

## Concurrency and Transaction Handling

### Stock Safety

Menu item stock is locked during order creation using pessimistic locking.

Repository method:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("select m from MenuItem m where m.id = :id")
Optional<MenuItem> findByIdForUpdate(@Param("id") Long id);
```

This prevents two concurrent orders from overselling the same menu item.

### Delivery Partner Claim Safety

Orders and delivery partners are locked when assigning or claiming a delivery partner.

This prevents multiple partners from claiming the same order and prevents a busy partner from being assigned twice.

### Transactional Order Placement

Order placement happens in a single transaction:

```text
Lock menu item
Validate stock
Deduct stock
Calculate total
Charge payment
Save order
Save order items
Record history
Publish event
```

If payment fails, the transaction rolls back.

---

## Payment Simulation

Payment request:

```json
{
  "method": "UPI",
  "token": "PAY_OK"
}
```

Supported payment methods:

```text
CARD
UPI
WALLET
```

Mock behavior:

| Token | Result |
|---|---|
| `PAY_OK` | Success |
| Any normal token | Success |
| `FAIL` | Failure |
| `DECLINE` | Failure |
| `INVALID` | Failure |

On success:

```text
paymentStatus = SUCCESS
paymentReference = PAY-{uuid}
paidAt = current timestamp
```

On rejection after successful payment:

```text
paymentStatus = REFUNDED
refundedAt = current timestamp
```

---

## Async Notifications

Order lifecycle changes publish events.

Events are handled asynchronously after transaction commit.

Examples:

```text
Order placed
Order accepted
Order preparing
Order out for delivery
Order delivered
Delivery partner assigned
```

Notifications are stored in the database and can be fetched later.

---

## Order Status History / Audit Trail

Each major status change creates a history row.

Example response:

```json
[
  {
    "id": 1,
    "orderId": 10,
    "oldStatus": null,
    "newStatus": "PLACED",
    "changedByUsername": "customer",
    "note": "Order created",
    "changedAt": "2026-07-05T12:00:00"
  },
  {
    "id": 2,
    "orderId": 10,
    "oldStatus": "PLACED",
    "newStatus": "ACCEPTED",
    "changedByUsername": "owner",
    "note": "Order accepted by restaurant",
    "changedAt": "2026-07-05T12:01:00"
  }
]
```

---

## Reviews

Review rules:

```text
Only delivered orders can be reviewed.
Only the owning customer can review.
Only one review is allowed per order.
Rating must be between 1 and 5.
```

Example:

```bash
curl -i -X POST http://localhost:8080/orders/1/review \
  -u customer:customer123 \
  -H "Content-Type: application/json" \
  -d '{
    "rating": 5,
    "comment": "Excellent"
  }'
```

---

## Validation and Error Handling

Validation errors return:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/orders"
}
```

Access denied errors return:

```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "You can manage only your own restaurant",
  "path": "/orders/1/accept"
}
```

Not found errors return:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Order not found",
  "path": "/orders/999"
}
```

Business validation errors return:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Insufficient stock for item: Margherita Pizza. Available stock: 10, requested quantity: 999",
  "path": "/orders"
}
```

---

## Testing

### Run Tests

```bash
./mvnw test
```

Windows:

```bash
mvnw.cmd test
```

### Test Profile

Tests use:

```text
src/test/resources/application-test.yml
```

The test profile uses H2 in PostgreSQL compatibility mode.

### Test Coverage

The integration test module covers:

```text
Order placement success
Payment failure rollback
Insufficient stock
Duplicate menu item merge
Concurrent stock safety
Full order lifecycle
Invalid lifecycle transition
Restaurant owner ownership checks
Customer ownership checks
Delivery partner ownership checks
Delivery partner city mismatch
Busy partner conflict
Review after delivery
Duplicate review prevention
Invalid review rating
Async notifications
Order history
Order filtering and scoped visibility
RBAC forbidden cases
```

---

## Project Structure

Typical package layout:

```text
src/main/java/com/sumit/fooddelivery
├── config
│   ├── AsyncConfig.java
│   ├── DataInitializer.java
│   └── SecurityConfig.java
├── controller
│   ├── CityController.java
│   ├── CustomerController.java
│   ├── DeliveryPartnerController.java
│   ├── MenuItemController.java
│   ├── NotificationController.java
│   ├── OrderController.java
│   ├── RestaurantController.java
│   └── ReviewController.java
├── dto
│   ├── request
│   └── response
├── entity
│   ├── City.java
│   ├── Customer.java
│   ├── DeliveryPartner.java
│   ├── MenuItem.java
│   ├── Notification.java
│   ├── Order.java
│   ├── OrderItem.java
│   ├── OrderStatusHistory.java
│   ├── Restaurant.java
│   ├── Review.java
│   └── User.java
├── enums
├── event
├── exception
├── notification
├── payment
├── repository
├── security
└── service
```

---

## Assumptions

- Basic Auth is sufficient for this assignment.
- Admin users are seeded by the application.
- Payment is simulated and no external payment provider is called.
- Notification delivery is simulated by storing notifications in the database.
- Restaurant city and delivery partner city must match.
- A delivery partner can handle only one active order at a time.
- A customer can review an order only once.
- Existing old database rows may need ownership fields populated after schema changes.

---

## Troubleshooting

### 1. `Restaurant does not have an owner configured`

Cause:

An old restaurant row was created before owner mapping was added.

Fix:

```bash
curl -i -X PUT http://localhost:8080/restaurants/1 \
  -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Pizza Palace",
    "address": "Connaught Place",
    "cityId": 1,
    "ownerUsername": "owner",
    "estimatedDeliveryTime": 30
  }'
```

### 2. `Customer profile not found for current user`

Cause:

The logged-in customer user is not linked to a customer profile.

Fix:

Restart the app so `DataInitializer` links it, or create/update the customer profile as admin.

### 3. `Delivery partner profile not found for current user`

Cause:

The logged-in partner user is not linked to a delivery partner profile.

Fix:

Restart the app so `DataInitializer` links it, or create/update the delivery partner profile as admin.

### 4. `Delivery partner city does not match restaurant city`

Cause:

The delivery partner belongs to a different city than the restaurant.

Fix:

Use a partner from the same city or update the partner city as admin.

### 5. `Delivery partner is not available. Current status is BUSY`

Cause:

The partner already has an active assigned order.

Fix:

Complete the current order or use a different available partner.

### 6. Database enum or constraint errors after code changes

Cause:

Old PostgreSQL schema constraints may not match new enum values or columns.

Fix during development:

```sql
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
```

Then restart the application.

Use this only for local development because it deletes data.

### 7. Async notification test returns fewer notifications

Cause:

Notifications are created asynchronously after transaction commit.

Fix:

Wait briefly and retry the notification endpoint.

---

## Future Improvements

Possible enhancements:

```text
JWT authentication instead of Basic Auth
Refresh tokens
Pagination and sorting for all list APIs
Swagger/OpenAPI documentation
Flyway database migrations
Separate Payment entity
Real payment gateway integration
Real notification providers such as email/SMS/WebSocket
Order cancellation by customer before acceptance
Partner location tracking
Restaurant operating hours
Menu item availability window
Idempotency-Key for order placement
Rate limiting
Docker Compose setup for app + PostgreSQL
CI pipeline with test execution
```

---

## Quick Demo Script

Use this sequence for a manual demo:

```text
1. Login as admin and list cities.
2. Create a restaurant for owner.
3. Login as owner and add menu item.
4. Login as customer and place paid order.
5. Login as owner and accept order.
6. Login as owner and mark preparing.
7. Login as partner and claim order.
8. Login as partner and mark out for delivery.
9. Login as partner and mark delivered.
10. Login as customer and create review.
11. Login as admin and view order history.
12. Login as admin and view notifications.
13. Show negative cases:
    - customer cannot use another customerId
    - owner cannot manage owner2 restaurant
    - partner cannot claim as another partner
    - partner from wrong city cannot claim
```
