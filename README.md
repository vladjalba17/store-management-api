# Store Management API

Small Spring Boot service for managing products. CRUD, validation, caching, and clear error handling.

## Tech Stack
- **Java 17**, Spring Boot 3.5
- Spring Web, Spring Data JPA, **H2** (in-memory)
- **MapStruct** for DTO <-> entity
- **Caffeine** cache
- Spring Validation (groups)
- Spring Security (light config)

## Features
- Create, read, update, delete products
- Lookup by **SKU**
- Pagination and filtering by `active`
- Partial updates: **price** and **stock**
- Unique constraints on `sku` and `productName` → returns **409 Conflict**
- DTO validation per operation (validation groups)
- Simple caching for `productBySku`
- Centralized error responses via `GlobalExceptionHandler`

## Authentication
In-memory users defined in `SecurityConfig`:

| Username | Password   | Role     |
|----------|------------|----------|
| admin    | admin123   | ADMIN    |
| manager  | manager123 | MANAGER  |
| employee | employee123| EMPLOYEE |

## API
Base path: `/api/products`

| Method | Path                                       | Description         |
|--------|--------------------------------------------|---------------------|
| POST   | `/api/products`                            | Create product      |
| PUT    | `/api/products/{sku}`                      | Full update by SKU  |
| PATCH  | `/api/products/{sku}/price?value=...`      | Update price        |
| PATCH  | `/api/products/{sku}/stock?value=...`      | Update stock        |
| GET    | `/api/products/{sku}`                      | Get by SKU          |
| GET    | `/api/products?active=true&page=0&size=10` | List paged        |
| DELETE | `/api/products/{sku}`                      | Delete              |

**Error format:** `GlobalExceptionHandler` returns `ErrorResponseDto` with a clear message and details.

## Optimistic Locking
`Product` uses `@Version`. On update, Hibernate checks the `version` column:
- If another write happened after you read the entity, Spring raises `OptimisticLockingFailureException`.

## Build & Run
```bash
mvn spring-boot:run
```

## H2 Console
- **URL:** `http://localhost:8080/h2-console`
- **JDBC URL:** `jdbc:h2:mem:storedb`
- **User:** `sa`  
- **Password:** *(empty)*

## Postman
A Postman collection is included to try the endpoints quickly.

## Notes
- DB enforces unique `sku` and `productName`.
- The `active` status can be modified via PUT `/api/products/{sku}` (full update). Use this endpoint to `enable` or `disable` a product.
