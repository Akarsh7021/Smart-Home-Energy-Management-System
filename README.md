# NightGrid Smart Home Energy Management System

NightGrid is a school project for monitoring smart-home devices and household energy usage. A React frontend communicates with this Spring Boot REST API using JSON and a stateful, database-backed login session.

## Technology

- Java 21 and Spring Boot 4
- Spring Web and Bean Validation
- Spring Security and Spring Session JDBC
- Spring Data JPA and PostgreSQL
- OpenAPI/Swagger
- Docker Compose for the development database

## Local setup

Requirements: Java 21, Docker Desktop, and IntelliJ IDEA.

1. Copy `.env.example` to `.env` and change the development credentials if needed.
2. Start PostgreSQL from the project root:

   ```powershell
   docker compose up -d
   ```

3. Open the project in IntelliJ and run `Application.java`.
4. Open Swagger UI at <http://localhost:8080/swagger-ui/index.html>.

PostgreSQL is exposed only on `127.0.0.1`. On the first run, Docker creates the database and development user, then runs the scripts in `src/main/resources/init-db`. Hibernate only validates the domain schema; it does not create or update application tables. Spring Session creates and manages only its own session tables.

## API endpoints

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `GET` | `/api/auth/csrf` | Create or refresh the CSRF token used by the React client |
| `POST` | `/api/auth/login` | Authenticate a user and create a session |
| `POST` | `/api/auth/logout` | End the current session |
| `GET` | `/api/dashboard` | Return the current user, devices, members, overview, and weekly energy summary |
| `PATCH` | `/api/devices/{deviceId}/state` | Turn a device on or off |
| `PATCH` | `/api/members/{memberId}/permissions` | Change a member's device-control and energy-view permissions |

API responses use one common `ApiEnvelope` containing the operation status, message, returned data, or error details. The React Axios interceptor can unwrap this envelope and display notifications before passing data to components.

The React client must use credentialed requests and call `GET /api/auth/csrf` when the application starts and again after login or logout. Axios reads the raw value from the `XSRF-TOKEN` cookie and sends it in the `X-XSRF-TOKEN` header for each `POST`, `PATCH`, or `DELETE`. The token value in Spring's controller argument is masked for BREACH protection and is intentionally not returned in the JSON body.

```javascript
const api = axios.create({
  baseURL: "http://localhost:8080/api",
  withCredentials: true,
  withXSRFToken: true,
  xsrfCookieName: "XSRF-TOKEN",
  xsrfHeaderName: "X-XSRF-TOKEN",
});

await api.get("/auth/csrf");
await api.post("/auth/login", { email, password });
```

## API documentation

Swagger UI is available at <http://localhost:8080/swagger-ui/index.html> while the application is running. It documents each operation's name, purpose, parameters, possible response statuses, request examples, response fields, and permission requirements. The generated OpenAPI description also identifies the `NIGHTGRID_SESSION` cookie and `X-XSRF-TOKEN` header security schemes.

## Project structure

```text
.
|-- docker-compose.yml             # Local PostgreSQL container
|-- .env.example                   # Safe development configuration template
|-- pom.xml                        # Maven dependencies and build settings
|-- src
|   |-- main
|   |   |-- java/eco/nightgrid/_0
|   |   |   |-- Application.java  # Spring Boot entry point
|   |   |   |-- config            # Session and security configuration
|   |   |   |-- controller        # Single REST controller from the SDD
|   |   |   |-- service           # Business logic and summary calculations
|   |   |   |-- dao               # Spring Data database access interfaces
|   |   |   |-- dto               # Request/response records and API envelope
|   |   |   |-- entity            # JPA entities and enums
|   |   |   `-- exception         # Global API exception handling
|   |   `-- resources
|   |       |-- application.properties
|   |       `-- init-db
|   |           |-- 01_create_tables.sql
|   |           `-- 02_insert_mock_data.sql
|   `-- test                     # Automated backend tests
```

The request flow is:

```text
React -> Spring Security -> SmartHomeController -> SmartHomeService -> DAO -> PostgreSQL
```

DTOs keep API data separate from JPA entities. The service performs permission checks, device operations, dashboard totals, and weekly energy calculations. The DAOs handle persistence only.

## Current backend status

The core backend described in the SDD is implemented. Before the first complete frontend demonstration, the following work remains:

- add unit, controller/security, and PostgreSQL integration tests for the SDD acceptance cases;
- run a full end-to-end test with the React application and Docker database;

Run the automated tests with the database container started:

```powershell
.\mvnw.cmd test
```

