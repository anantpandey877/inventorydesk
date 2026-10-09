# Inventory Desk

Inventory Desk is a Spring Boot REST API for managing inventory items and the
approval workflows around them. Inventory managers can submit requests for new
items and update stock, while administrators approve manager accounts, approve
inventory requests, and reject requests that should not be published.

## Features

- JWT-based, stateless authentication.
- Self-registration for inventory managers.
- Administrator approval workflow for new manager accounts.
- Inventory request workflow with pending, approved, and rejected states.
- Automatic creation of an active inventory item when an administrator approves
  an inventory request.
- Stock updates and soft deactivation of inventory items.
- MySQL persistence for normal runs and H2 for tests.
- JSON error responses for application validation and state errors.

## Technology stack

- Java 21
- Spring Boot 4.1.1
- Spring Web MVC
- Spring Data JPA and Hibernate
- Spring Security
- JSON Web Tokens using JJWT 0.12.6
- MySQL
- H2 (test scope)
- Maven Wrapper
- Lombok

## Domain model

### Users

Users have one of these roles:

- `ADMIN`: reviews manager accounts and inventory requests. The default
  administrator is created at startup if the configured email does not already
  exist.
- `INVENTORY_MANAGER`: submits inventory requests and updates stock after the
  account has been approved.

New registrations are always created as `INVENTORY_MANAGER` accounts with
`PENDING` status. An administrator must approve the account before it can log
in.

Account statuses are `PENDING`, `APPROVED`, and `REJECTED`.

### Inventory items

Inventory items contain a name, unique SKU, optional description, category,
stock quantity, unit price, active flag, and creation/update timestamps.
Deactivation is a soft delete: the item remains in the database but is no
longer returned by the active inventory endpoints.

### Inventory requests

A request contains the proposed item details and is initially `PENDING`.
Approving a request creates an active inventory item with the requested initial
quantity and changes the request to `APPROVED`. Rejecting it changes the
request to `REJECTED`. A request can only be reviewed once, and its SKU must
not already exist when it is approved.

## Prerequisites

- JDK 21 or later
- MySQL 8 or a compatible MySQL server
- A database named `inventory_desk_db`
- Permission for the configured database user to create/update tables

Create the database before starting the application:

```sql
CREATE DATABASE inventory_desk_db;
```

## Configuration

The default configuration is in
[`src/main/resources/application.properties`](src/main/resources/application.properties).
Environment variables can override the defaults:

| Environment variable | Property | Purpose |
| --- | --- | --- |
| `DB_URL` | `spring.datasource.url` | JDBC URL; defaults to `jdbc:mysql://localhost:3306/inventory_desk_db` |
| `DB_USERNAME` | `spring.datasource.username` | Database username; defaults to `root` |
| `DB_PASSWORD` | `spring.datasource.password` | Database password |
| `JWT_SECRET` | `jwt.secret` | Secret used to sign JWTs; set a strong value outside local development |
| `ADMIN_NAME` | `admin.name` | Name for the initial administrator |
| `ADMIN_EMAIL` | `admin.email` | Email for the initial administrator |
| `ADMIN_PASSWORD` | `admin.password` | Password for the initial administrator |

The application uses `spring.jpa.hibernate.ddl-auto=update`, so Hibernate
updates the schema on startup. Do not use the development JWT fallback or
default administrator credentials in a production deployment. The configured
administrator is created only when that email is not already present.

## Running locally

Run the application with the Maven Wrapper:

```bash
./mvnw spring-boot:run
```

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

The API is available at `http://localhost:8080` unless the server port is
changed through standard Spring Boot configuration.

Build the project:

```powershell
.\mvnw.cmd clean package
```

Run the tests:

```powershell
.\mvnw.cmd test
```

Tests use an in-memory H2 database with MySQL compatibility mode and
`create-drop` schema handling.

## Authentication

1. Register a manager with `POST /api/auth/register`.
2. Log in with `POST /api/auth/login` after an administrator approves the
   account.
3. Send the returned token on protected requests:

```http
Authorization: Bearer <jwt-token>
```

Authentication endpoints are public. All other `/api/**` endpoints require an
approved authenticated user, and `/api/admin/**` endpoints require the
`ADMIN` role. The stock update endpoint additionally requires the
`INVENTORY_MANAGER` role.

## API endpoints

Unless stated otherwise, request and response bodies use `application/json`.
`{userId}`, `{itemId}`, and `{requestId}` are numeric database IDs.

### Authentication

| Method | Endpoint | Access | Description |
| --- | --- | --- | --- |
| `POST` | `/api/auth/register` | Public | Register a pending inventory manager |
| `POST` | `/api/auth/login` | Public | Authenticate an approved user and receive a JWT |

Register request:

```json
{
  "name": "Alex Manager",
  "email": "alex@example.com",
  "password": "change-me"
}
```

Login request:

```json
{
  "email": "alex@example.com",
  "password": "change-me"
}
```

The login response contains `token` and `role`. Registration returns the
created user representation with `PENDING` account status; it does not issue a
token.

### Inventory

| Method | Endpoint | Access | Description |
| --- | --- | --- | --- |
| `GET` | `/api/inventory` | Admin or approved manager | List active inventory items |
| `GET` | `/api/inventory/{itemId}` | Admin or approved manager | Get one active item |
| `PATCH` | `/api/inventory/{itemId}/stock` | Approved manager | Replace the stock quantity |
| `DELETE` | `/api/inventory/{itemId}` | Admin or approved manager | Soft-deactivate an item |

Stock update request:

```json
{
  "stockQuantity": 25
}
```

Stock quantities cannot be negative. A successful deactivation returns
`204 No Content`.

### Manager inventory requests

| Method | Endpoint | Access | Description |
| --- | --- | --- | --- |
| `POST` | `/api/requests` | Approved manager | Submit a new inventory request |
| `GET` | `/api/requests/my` | Approved manager | List the current manager's requests |

Create-request body:

```json
{
  "itemName": "Wireless Keyboard",
  "sku": "KB-WL-001",
  "description": "Compact keyboard for office workstations",
  "category": "Peripherals",
  "initialQuantity": 50,
  "unitPrice": 39.99
}
```

`itemName`, `sku`, `category`, `initialQuantity`, and `unitPrice` are required.
Quantity and unit price must be non-negative. The description is optional.

### Administrator operations

| Method | Endpoint | Access | Description |
| --- | --- | --- | --- |
| `GET` | `/api/admin/users/pending` | Admin | List pending manager accounts |
| `PATCH` | `/api/admin/users/{userId}/approve` | Admin | Approve a manager account |
| `PATCH` | `/api/admin/users/{userId}/reject` | Admin | Reject a manager account |
| `GET` | `/api/admin/requests` | Admin | List all inventory requests |
| `PATCH` | `/api/admin/requests/{requestId}/approve` | Admin | Approve a pending request and create its item |
| `PATCH` | `/api/admin/requests/{requestId}/reject` | Admin | Reject a pending request |

The approval and rejection endpoints do not require a request body.

## Common response shapes

Inventory item responses include:

```json
{
  "id": 1,
  "name": "Wireless Keyboard",
  "sku": "KB-WL-001",
  "description": "Compact keyboard for office workstations",
  "category": "Peripherals",
  "stockQuantity": 50,
  "unitPrice": 39.99,
  "active": true,
  "createdAt": "2026-01-01T12:00:00",
  "updatedAt": "2026-01-01T12:00:00"
}
```

Inventory request responses also include `status`, `requestedBy`,
`reviewedBy`, `reviewedAt`, and `createdAt`.

Application validation errors use this shape:

```json
{
  "status": 400,
  "message": "Name, email, and password are required",
  "timestamp": "2026-01-01T12:00:00"
}
```

Typical statuses are:

- `201 Created`: registration or inventory-request creation succeeded.
- `200 OK`: a read or update succeeded.
- `204 No Content`: an inventory item was deactivated.
- `400 Bad Request`: input validation failed.
- `401 Unauthorized`: authentication is missing or invalid.
- `403 Forbidden`: the authenticated user lacks the required role.
- `409 Conflict`: the requested state transition is invalid, such as logging
  in before approval or reviewing an already-reviewed request.

## Project structure

```text
src/main/java/com/kpit/anant/inventorydesk/
├── config/       Application and security configuration
├── controller/   REST endpoints and exception handling
├── dto/          Request and response payloads
├── entity/       JPA entities
├── enums/        Roles and workflow statuses
├── repository/   Spring Data repositories
├── security/     JWT generation and request filtering
└── service/      Business rules and transactions
```

## Workflow example

1. The administrator account is initialized at startup.
2. A user registers as an inventory manager.
3. An administrator lists pending managers and approves the account.
4. The manager logs in and receives a JWT.
5. The manager submits an inventory request.
6. The administrator approves the request, which creates the inventory item.
7. The manager reads inventory and updates its stock as needed.
8. An administrator or manager deactivates an item when it should no longer
   appear in active inventory.
