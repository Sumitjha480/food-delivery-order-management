# CLAUDE.md

This file provides guidance for AI coding assistants working on this repository.

---

## Project Summary

This is a Spring Boot food delivery order management application.

Core responsibilities:

- Restaurants operate in cities.
- Restaurant owners manage their own menus and orders.
- Customers place paid orders.
- Stock is decremented safely during order placement.
- Delivery partners claim or are assigned orders.
- Order lifecycle is tracked.
- Notifications are generated asynchronously.
- Customers review delivered orders.
- Access is controlled through both roles and resource ownership.

---

## Build and Test Commands

Run application:

```bash
./mvnw spring-boot:run
```

Run tests:

```bash
./mvnw test
```

Build package:

```bash
./mvnw clean package
```

---

## Architecture

The project follows layered architecture:

```text
controller -> service -> repository -> database
```

Main packages:

```text
com.sumit.fooddelivery.controller
com.sumit.fooddelivery.service
com.sumit.fooddelivery.repository
com.sumit.fooddelivery.entity
com.sumit.fooddelivery.dto
com.sumit.fooddelivery.enums
com.sumit.fooddelivery.security
com.sumit.fooddelivery.config
com.sumit.fooddelivery.event
com.sumit.fooddelivery.notification
com.sumit.fooddelivery.payment
com.sumit.fooddelivery.exception
```

---

## Security Rules

Authentication uses Basic Auth.

Roles:

```text
ADMIN
RESTAURANT_OWNER
CUSTOMER
DELIVERY_PARTNER
```

Authorization is implemented through:

1. `SecurityConfig` for route-level role checks.
2. `CurrentUserService` for ownership checks.

Do not rely only on controller/security matcher rules for ownership-sensitive actions.

Always enforce ownership in the service layer.

---

## Ownership Rules

### Restaurant Owner

Restaurant owner can act only on restaurants where:

```java
restaurant.getOwner().getId().equals(currentUser.getId())
```

Use:

```java
currentUserService.requireAdminOrRestaurantOwner(restaurant);
```

### Customer

Customer can act only on their linked customer profile.

Use:

```java
currentUserService.requireAdminOrCustomer(customer);
```

### Delivery Partner

Delivery partner can act only on their linked delivery partner profile.

Use:

```java
currentUserService.requireAdminOrDeliveryPartner(deliveryPartner);
```

---

## Order Lifecycle

Allowed lifecycle:

```text
PLACED -> ACCEPTED -> PREPARING -> OUT_FOR_DELIVERY -> DELIVERED
```

Rejected flow:

```text
PLACED -> REJECTED
```

Do not allow skipping states.

Do not mark an order out for delivery without an assigned delivery partner.

Do not mark an order delivered unless it is already `OUT_FOR_DELIVERY`.

---

## Transaction Rules

Order creation must remain transactional.

The required sequence is:

```text
Load customer
Verify customer ownership
Load restaurant
Lock menu items
Validate stock
Deduct stock
Calculate total
Charge payment
Save order
Save order items
Record status history
Publish order event
```

If payment fails, stock deduction must roll back.

Do not move payment outside the transaction unless compensating logic is added.

---

## Locking Rules

Use pessimistic locking for stock and delivery partner contention.

Important methods:

```java
MenuItemRepository.findByIdForUpdate(...)
OrderRepository.findByIdForUpdate(...)
DeliveryPartnerRepository.findByIdForUpdate(...)
```

Do not replace these with plain `findById` in order placement or partner claim/assignment flows.

---

## Delivery Partner Rules

A partner can claim an order only when:

```text
Order status is ACCEPTED or PREPARING
Order has no assigned partner
Partner status is AVAILABLE
Partner city matches restaurant city
Logged-in partner owns the delivery partner profile
```

When assigned/claimed:

```text
Partner status -> BUSY
```

When delivered:

```text
Partner status -> AVAILABLE
```

---

## Payment Rules

Payment is simulated by `FakePaymentServiceImpl`.

Failing tokens:

```text
FAIL
DECLINE
INVALID
```

All other tokens succeed.

When successful:

```text
paymentStatus = SUCCESS
paymentReference = PAY-{uuid}
paidAt = now
```

When rejected after successful payment:

```text
paymentStatus = REFUNDED
refundedAt = now
```

---

## Notification Rules

Notifications are created asynchronously after transaction commit.

Listeners should use:

```java
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
@Transactional(propagation = Propagation.REQUIRES_NEW)
@Async
```

Do not create notifications before the main transaction commits.

---

## Review Rules

A review can be created only when:

```text
Order status is DELIVERED
Review does not already exist for the order
Logged-in customer owns the order
```

Rating must be between 1 and 5.

---

## Order Filtering Rules

`GET /orders` supports filters:

```text
status
restaurantId
customerId
deliveryPartnerId
```

Visibility must remain scoped:

```text
ADMIN -> all orders
CUSTOMER -> own orders only
RESTAURANT_OWNER -> owned restaurant orders only
DELIVERY_PARTNER -> assigned orders only
```

---

## Testing Guidance

Integration tests should cover:

```text
Order placement success
Payment rollback
Insufficient stock
Concurrent stock safety
Lifecycle transitions
RBAC forbidden cases
Restaurant ownership
Customer ownership
Delivery partner ownership
City mismatch
Busy partner claim prevention
Review after delivery
Duplicate review prevention
Notifications
Order status history
Order filtering
```

Use `@ActiveProfiles("test")` and H2 PostgreSQL compatibility mode.

---

## Code Style

Follow these conventions:

- Use DTOs for request/response objects.
- Keep business logic in services.
- Keep controllers thin.
- Use `@Transactional` on service methods.
- Use `@Transactional(readOnly = true)` for read-only service methods.
- Throw `IllegalArgumentException` for business rule failures.
- Throw `EntityNotFoundException` for missing entities.
- Throw `AccessDeniedException` for ownership/authorization failures.
- Use Lombok for getters/setters/builders where already used.
- Keep enum columns with sufficient length, usually `length = 50`.

---

## Things to Avoid

Do not:

- Bypass ownership checks.
- Allow customers to pass arbitrary `customerId`.
- Allow partners to pass arbitrary `deliveryPartnerId`.
- Allow restaurant owners to manage other restaurants.
- Skip order lifecycle states.
- Remove pessimistic locks from stock/claim flows.
- Send notifications before transaction commit.
- Make tests depend on existing local PostgreSQL data.
