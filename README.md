# Wayfare Transportation API

> A robust, secure, and high-performance RESTful API for public bus transportation booking — built with **Quarkus 3** on Java 21.

[![Build](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com/ngirimana/wayfarer)
[![Tests](https://img.shields.io/badge/tests-47%20passed-brightgreen)](https://github.com/ngirimana/wayfarer)
[![Java](https://img.shields.io/badge/Java-21-blue)](https://openjdk.org/)
[![Quarkus](https://img.shields.io/badge/Quarkus-3.34.6-blue)](https://quarkus.io/)

---

## 🚀 Key Features

| Feature | Details |
|---|---|
| **User Authentication** | JWT-based stateless auth with RS256 signing |
| **OTP Verification** | Email OTP for account activation & password reset |
| **Role-Based Access** | `ADMIN` and `USER` roles enforced on all protected routes |
| **Trip Management** | Create, browse, and cancel bus trips (admin-managed) |
| **Booking System** | Seat reservation with loyalty-discount calculation |
| **Password Security** | Bcrypt hashing via Elytron Security |
| **Schema Migrations** | Flyway with timestamp-based versioning |
| **Global Error Handling** | Consistent JSON error responses across all endpoints |
| **API Docs** | Interactive Swagger UI included |

---

## 🛠 Technology Stack

- **Runtime**: Java 21, Quarkus 3.34.6
- **REST Layer**: Quarkus REST (RESTEasy Reactive) + Jackson
- **Persistence**: Hibernate ORM with Panache (Active Record Pattern)
- **Database**: PostgreSQL
- **Migrations**: Flyway
- **Security**: SmallRye JWT (RS256), Elytron Bcrypt
- **Email**: Quarkus Mailer (SMTP — Mailtrap / Gmail / etc.)
- **Docs**: SmallRye OpenAPI + Swagger UI
- **Testing**: JUnit 5, REST Assured, Mockito, Panache Mock

---

## 📋 Prerequisites

- **Java 21+**
- **Maven 3.9+** (or use the included `./mvnw` wrapper)
- **PostgreSQL** database (running locally or remotely)

---

## ⚙️ Configuration

Copy the template below into a `.env` file at the project root (already in `.gitignore`):

```env
# Database
DB_USERNAME=postgres
DB_PASSWORD=your_password
DB_URL=jdbc:postgresql://localhost:5432/wayfarer

# Mailer (SMTP — example for Mailtrap)
MAILER_USERNAME=your_mailtrap_user
MAILER_PASSWORD=your_mailtrap_pass
MAILER_FROM=noreply@wayfare.com

# Admin seeding (auto-created on first startup)
ADMIN_EMAIL=admin@wayfare.com
ADMIN_PASSWORD=admin123
```

All keys map to `application.properties` via `${ENV_VAR}` substitution.

---

## 🏃 Running the Application

### Development mode (live reload)
```bash
./mvnw quarkus:dev
```
API available at `http://localhost:8080`

### Run tests
```bash
./mvnw test
```
> 47 tests across Auth, Trip, Booking controllers and Trip service.

### Package as Uber-JAR
```bash
./mvnw package -Dquarkus.package.type=uber-jar
java -jar target/wayfare-1.0.0-SNAPSHOT-runner.jar
```

### Native executable (requires GraalVM)
```bash
./mvnw package -Dnative
./target/wayfare-1.0.0-SNAPSHOT-runner
```

---

## 📖 API Reference

Base URL: `http://localhost:8080/api/v1`

Interactive docs: **`http://localhost:8080/api/swagger`**
OpenAPI spec: `http://localhost:8080/api/openapi`

### 🔐 Authentication — `/api/v1/auth`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/signup` | Public | Register a new user account |
| `POST` | `/verify` | Public | Verify account with OTP |
| `POST` | `/login` | Public | Login — returns JWT token |
| `POST` | `/reset-password-request?email=` | Public | Send OTP for password reset |
| `POST` | `/reset-password?email=&otp=&password=` | Public | Reset password using OTP |

**Signup request:**
```json
{ "email": "user@example.com", "phone": "+250700000000", "password": "secret123" }
```

**Login response:**
```json
{ "token": "<JWT>", "email": "user@example.com", "role": "USER" }
```

---

### 🚌 Trips — `/api/v1/trips`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `GET` | `/` | Public | List all active trips (filterable by `origin`, `destination`) |
| `GET` | `/{id}` | Public | Get a single trip by ID |
| `POST` | `/` | `ADMIN` | Create a new trip |
| `PATCH` | `/{id}/cancel` | `ADMIN` | Cancel a trip |

**Query params for GET `/`:** `origin`, `destination`, `sort`

---

### 🎫 Bookings — `/api/v1/bookings`

> All booking endpoints require a valid JWT (`Authorization: Bearer <token>`).

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/` | `USER` | Book a seat on a trip |
| `GET` | `/` | `USER` / `ADMIN` | List your bookings |
| `DELETE` | `/{id}` | `USER` / `ADMIN` | Cancel a booking — `204 No Content` |

**Booking request:**
```json
{ "tripId": 1, "seatNumber": 12 }
```

---

### ⚠️ Error Responses

All error responses follow a consistent JSON envelope:

```json
{ "message": "Account not verified" }
```

| Status | Meaning |
|--------|---------|
| `400` | Validation error / bad request |
| `401` | Unauthorized (missing / invalid JWT) |
| `403` | Forbidden (wrong role) |
| `404` | Resource not found |
| `409` | Conflict (e.g. email already exists) |
| `500` | Internal server error |

---

## 📂 Project Structure

```
src/
├── main/
│   ├── java/org/acme/
│   │   ├── config/           # DataInitializer (admin seeding)
│   │   ├── controller/v1/    # REST endpoints (Auth, Trip, Booking)
│   │   ├── entity/           # Panache entities (User, Trip, Booking)
│   │   ├── exception/        # GlobalExceptionMapper, ConflictException
│   │   ├── payload/
│   │   │   ├── request/      # SignupRequest, LoginRequest, BookingRequest…
│   │   │   └── response/     # AuthResponse, MessageResponse
│   │   ├── security/         # PasswordUtils (Bcrypt), TokenService (JWT)
│   │   └── service/          # AuthService, TripService, BookingService
│   └── resources/
│       ├── application.properties
│       ├── db/migration/     # Flyway SQL scripts
│       ├── privateKey.pem    # JWT RS256 signing key
│       └── publicKey.pem     # JWT RS256 verification key
└── test/
    └── java/org/acme/
        ├── controller/v1/    # AuthControllerTest, TripControllerTest, BookingControllerTest
        ├── mock/             # MockBookingService
        └── service/          # TripServiceTest
```

---

## 🗄 Database Migrations

Migrations run automatically on startup via Flyway.

- **Location**: `src/main/resources/db/migration/`
- **Naming**: `V<YYYYMMDDHHmmss>__Description.sql`

| File | Description |
|------|-------------|
| `V20260427102900__Consolidated_Schema.sql` | Initial schema (users, trips, bookings) |
| `V20260427103300__Add_Trip_Description.sql` | Adds `description` column to trips |
| `V20260427103400__Add_User_Bio.sql` | Adds `bio` column to users |

---

## 📄 License

This project is proprietary and confidential.
