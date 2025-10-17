Account Service

This module implements a lightweight Account Service that issues JWT tokens for clients and exposes registration and health endpoints.

Quick summary
- Context path: /account
- Port: 8081
- Endpoints:
  - GET /account/           -> service health message
  - POST /account/token     -> login with JSON {"username": "email", "password": "..."} -> returns {"token": "..."}
  - POST /account/register  -> register via Data Service with JSON {"name":"...","email":"...","password":"..."}

Configuration
- `src/main/resources/application.properties`
  - `server.port` (default 8081)
  - `server.servlet.context-path` (default /account)
  - `account.jwt.secret` (change this to a strong secret in production)
  - `account.jwt.expirationSeconds` (default 3600)
  - `dataservice.baseUrl` (base URL for the Data Service; default http://localhost:8080)

Build & run (from the workspace root)

Run with the Gradle wrapper in the `account` folder:

```bash
cd account
./gradlew clean build
./gradlew bootRun
```

If you need to refresh dependencies:

```bash
./gradlew --refresh-dependencies clean build
```

Testing the service (examples)

Health check:

```bash
curl -s http://localhost:8081/account/ | jq -r .
# or
curl http://localhost:8081/account/
```

Register a new user (this will forward to the Data Service configured in `dataservice.baseUrl`):

```bash
curl -X POST http://localhost:8081/account/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice","email":"alice@example.com","password":"secret"}'
```

Obtain a token:

```bash
curl -X POST http://localhost:8081/account/token \
  -H "Content-Type: application/json" \
  -d '{"username":"alice@example.com","password":"secret"}'
```

Notes & next steps
- The JWT secret in `application.properties` must be changed for any real deployment.
- Passwords are compared as plain text to the Customer store for this demo; use hashing (BCrypt) in production.
- The Customer REST endpoints used by `CustomerService` are naive and may need to be adjusted to match the Data Service API (paths, payloads, responses).
- If you want the Account Service to also validate tokens for some endpoints, add a JWT filter to `SecurityConfig` and wire an authentication provider.

Files added/changed
- `src/main/java/com/example/account/controller/AccountController.java` - endpoints
- `src/main/java/com/example/account/service/CustomerService.java` - calls Data Service
- `src/main/java/com/example/account/util/JwtUtil.java` - token generation
- `src/main/java/com/example/account/security/SecurityConfig.java` - permits account endpoints
- DTOs under `src/main/java/com/example/account/dto`
- `build.gradle` updated to include `jjwt` dependencies
- `src/main/resources/application.properties` updated

If you want, I can:
- Add a unit test for the controller/token generation (mocking CustomerService)
- Add password hashing with BCrypt and adjust Customer creation to store hashed passwords
- Implement a JWT authentication filter so other endpoints require tokens
