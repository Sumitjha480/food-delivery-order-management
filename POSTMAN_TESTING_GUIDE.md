# POSTMAN_TESTING_GUIDE.md

## Food Delivery Postman Testing Guide

This guide explains how to use the provided Postman collection for full end-to-end testing.

---

## Files

Import these two files into Postman:

```text
food_delivery_complete_e2e.postman_collection.json
food_delivery_local.postman_environment.json
```

---

## Prerequisites

The Spring Boot application must be running locally:

```bash
./mvnw spring-boot:run
```

Base URL:

```text
http://localhost:8080
```

The following demo users should exist:

| Role | Username | Password |
|---|---|---|
| Admin | `admin` | `admin123` |
| Owner | `owner` | `owner123` |
| Owner 2 | `owner2` | `owner2123` |
| Customer | `customer` | `customer123` |
| Customer 2 | `customer2` | `customer2123` |
| Partner | `partner` | `partner123` |
| Partner 2 | `partner2` | `partner2123` |

---

## Import Steps

1. Open Postman.
2. Click **Import**.
3. Import the collection JSON.
4. Import the environment JSON.
5. Select the environment named:

```text
Food Delivery Local
```

---

## Execution Order

Run folders in order:

```text
00 - Health and RBAC smoke tests
01 - Setup data and capture IDs
02 - Complete happy path: order to review
03 - Ownership and authorization negative cases
04 - Delivery partner edge cases
05 - Order filtering and scoped visibility
06 - Validation and transaction edge cases
```

The collection uses environment variables captured from earlier requests.

Do not start from a later folder unless all required IDs are already populated.

---

## Environment Variables

Important variables:

```text
baseUrl
cityId
mumbaiCityId
customerId
customer2Id
restaurantId
owner2RestaurantId
menuItemId
owner2MenuItemId
partnerId
partner2Id
orderId
owner2OrderId
mismatchOrderId
busyOrder1Id
busyOrder2Id
reviewId
```

Most variables are captured automatically by test scripts.

---

## Folder Details

### 00 - Health and RBAC smoke tests

Checks:

- App is running.
- Protected APIs reject unauthenticated access.
- Admin credentials work.
- Customer cannot access admin-only customer list.

---

### 01 - Setup data and capture IDs

Creates/captures:

- Customer profile IDs
- Active city
- Delivery partner IDs
- Restaurant owned by `owner`
- Restaurant owned by `owner2`
- Menu item for each restaurant

Also verifies that a customer cannot create a city.

---

### 02 - Complete happy path: order to review

Tests:

```text
Customer places paid order
Owner accepts order
Owner marks preparing
Wrong partner cannot claim
Correct partner claims
Partner marks out for delivery
Partner marks delivered
Order history is available
Notifications are available
Customer creates review
Duplicate review fails
```

---

### 03 - Ownership and authorization negative cases

Tests:

```text
Customer cannot order with another customerId
Owner cannot manage owner2 restaurant
Admin can manage owner2 restaurant
Wrong owner cannot accept owner2 order
Correct owner2 can accept own order
Customer cannot accept order
Customer2 cannot view customer1 order
```

---

### 04 - Delivery partner edge cases

Tests:

```text
Partner city mismatch
Partner moved back to valid city
Busy partner cannot claim another order
Partner becomes available after delivery
```

---

### 05 - Order filtering and scoped visibility

Tests:

```text
Admin filters by status
Admin filters by restaurant
Admin filters by customer
Admin filters by delivery partner
Customer sees own orders only
Owner sees owned restaurant orders only
Partner sees assigned orders only
Cross-scope filtering is blocked
```

---

### 06 - Validation and transaction edge cases

Tests:

```text
Order without payment fails
Payment failure fails
Insufficient stock fails
Invalid review rating fails
```

---

## Common Issues

### Partner city mismatch test affects later tests

The collection moves `partner2` back to the original test city after the mismatch test. If a request fails midway, rerun the reset request:

```text
04 - Delivery partner edge cases -> Move partner2 back to test city
```

### Async notification count is lower than expected

Notifications are asynchronous. Wait one second and rerun:

```text
02 - Complete happy path -> Check async notifications
```

---