# SUBMISSION_CHECKLIST.md

## Food Delivery Order Management Submission Checklist

Use this checklist before final submission.

---

## 1. Repository

- [ ] Code is pushed to GitHub.
- [ ] Repository is public or accessible to reviewers.
- [ ] Commit history shows multiple meaningful commits.
- [ ] No local-only files are required to run the project.
- [ ] No secrets or real credentials are committed.

---

## 2. Required Source Code

- [ ] City management implemented.
- [ ] Restaurant management implemented.
- [ ] Restaurant owner mapping implemented.
- [ ] Menu management implemented.
- [ ] Customer management implemented.
- [ ] Customer-user ownership mapping implemented.
- [ ] Delivery partner management implemented.
- [ ] Delivery partner-user ownership mapping implemented.
- [ ] Order placement implemented.
- [ ] Payment simulation implemented.
- [ ] Stock locking implemented.
- [ ] Order lifecycle implemented.
- [ ] Delivery partner assignment/claim implemented.
- [ ] Delivery partner city validation implemented.
- [ ] Busy partner prevention implemented.
- [ ] Async notifications implemented.
- [ ] Reviews after delivery implemented.
- [ ] Order status history implemented.
- [ ] Filtered order APIs implemented.
- [ ] RBAC implemented.
- [ ] Ownership checks implemented.
- [ ] Centralized error handling implemented.

---

## 3. Required Documentation

- [ ] `README.md` exists.
- [ ] README includes project overview.
- [ ] README includes tech stack.
- [ ] README includes local setup instructions.
- [ ] README includes database setup.
- [ ] README includes demo users.
- [ ] README includes API examples.
- [ ] README includes order lifecycle.
- [ ] README includes testing instructions.
- [ ] README includes assumptions.
- [ ] README includes troubleshooting.
- [ ] README includes future improvements.
- [ ] `AGENTS.md` or `CLAUDE.md` exists.
- [ ] `SKILLS_USED.md` exists.
- [ ] Assignment PDF or raw requirement file is included under `docs/`.

Recommended location:

```text
docs/Food Delivery Order Management.pdf
```

---

## 4. Postman

- [ ] Postman collection included.
- [ ] Postman environment included.
- [ ] Collection runs against local app.
- [ ] Collection folders are documented.
- [ ] Collection covers positive flows.
- [ ] Collection covers negative RBAC/ownership flows.
- [ ] Collection covers validation failures.
- [ ] Collection covers lifecycle and reviews.

Recommended location:

```text
postman/food_delivery_complete_e2e.postman_collection.json
postman/food_delivery_local.postman_environment.json
```

---

## 5. Tests

- [ ] Test profile exists.
- [ ] H2 test DB is configured.
- [ ] Order placement success test exists.
- [ ] Payment failure rollback test exists.
- [ ] Insufficient stock test exists.
- [ ] Concurrent stock safety test exists.
- [ ] Lifecycle test exists.
- [ ] Restaurant owner ownership test exists.
- [ ] Customer ownership test exists.
- [ ] Delivery partner ownership test exists.
- [ ] Delivery partner city mismatch test exists.
- [ ] Busy partner test exists.
- [ ] Review after delivery test exists.
- [ ] Duplicate review test exists.
- [ ] Notification test exists.
- [ ] Order history test exists.
- [ ] Order filtering test exists.
- [ ] RBAC test exists.

Run:

```bash
./mvnw test
```

Expected:

```text
BUILD SUCCESS
```

---

## 6. Manual Smoke Test

Run the application:

```bash
./mvnw spring-boot:run
```

Check:

```bash
curl -i http://localhost:8080/health
```

Expected:

```text
200 OK
```

Check authenticated endpoint:

```bash
curl -i http://localhost:8080/restaurants -u admin:admin123
```

Expected:

```text
200 OK
```

---

## 7. Demo Flow

Before recording video, verify this flow:

- [ ] Admin lists cities.
- [ ] Admin creates restaurant assigned to `owner`.
- [ ] Owner creates menu item.
- [ ] Customer places order with payment token `PAY_OK`.
- [ ] Owner accepts order.
- [ ] Owner marks order preparing.
- [ ] Partner claims order.
- [ ] Partner marks out for delivery.
- [ ] Partner marks delivered.
- [ ] Customer reviews order.
- [ ] Admin views order history.
- [ ] Admin views notifications.

---

## 8. Negative Demo Cases

Show at least a few:

- [ ] Customer cannot create city.
- [ ] Customer cannot place order using another `customerId`.
- [ ] Owner cannot create menu item for owner2 restaurant.
- [ ] Wrong owner cannot accept another owner's order.
- [ ] Partner cannot claim using another partner's profile.
- [ ] Partner from another city cannot claim.
- [ ] Busy partner cannot claim another order.
- [ ] Review before delivery fails.
- [ ] Duplicate review fails.
- [ ] Invalid lifecycle transition fails.

---

## 9. Video Explanation

Recommended video structure:

```text
1. Project overview
2. Architecture and packages
3. Database/domain model
4. Auth and RBAC
5. Order lifecycle
6. Stock locking and payment transaction
7. Delivery partner assignment and contention
8. Ownership checks
9. Notifications and history
10. Tests and Postman demo
11. Assumptions and future improvements
```

Keep the video focused and practical.

---

## 10. Final Cleanup

- [ ] Remove unused imports.
- [ ] Format code.
- [ ] Ensure no commented-out debug code.
- [ ] Ensure no personal credentials.
- [ ] Ensure app starts from fresh DB.
- [ ] Ensure tests pass.
- [ ] Ensure README is accurate.
- [ ] Ensure Postman environment points to `http://localhost:8080`.
- [ ] Ensure final zip/repo includes all required files.

---

## Suggested Final Repository Layout

```text
food-delivery-order-management
├── README.md
├── AGENTS.md
├── CLAUDE.md
├── SKILLS_USED.md
├── SUBMISSION_CHECKLIST.md
├── pom.xml
├── mvnw
├── mvnw.cmd
├── docs
│   └── Food Delivery Order Management.pdf
├── postman
│   ├── food_delivery_complete_e2e.postman_collection.json
│   └── food_delivery_local.postman_environment.json
└── src
    ├── main
    └── test
```
