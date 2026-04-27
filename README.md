# Wayfare Transportation API

Wayfare is a robust, secure, and high-performance RESTful API designed for public bus transportation booking services. Built with the **Quarkus** framework, it follows a clean modular architecture and provides a scalable foundation for modern transit management.

---

## 🚀 Key Features

- **User Management**: Registration, Login, and Profile management.
- **Secure Authentication**: Stateless authentication using **JWT (JSON Web Tokens)**.
- **OTP Verification**: Email-based OTP for account verification using Mailer integration.
- **Trip Management**: Comprehensive API for managing bus routes, schedules, and capacity.
- **Booking System**: Real-time seat reservation with loyalty-based discount calculation.
- **Database Versioning**: Managed schema migrations using **Flyway**.
- **Automated Auditing**: `createdAt` and `updatedAt` tracking for all core entities.
- **API Documentation**: Interactive documentation via **Swagger UI**.

---

## 🛠 Technology Stack

- **Core**: Java 21, Quarkus 3.34.6
- **Persistence**: Hibernate ORM with Panache (Active Record Pattern)
- **Database**: PostgreSQL
- **Migrations**: Flyway (Timestamp-based versioning)
- **Security**: SmallRye JWT, Bcrypt Password Encryption
- **API Documentation**: SmallRye OpenAPI (Swagger UI)
- **Email**: Quarkus Mailer (configured for Mailtrap)

---

## 📋 Prerequisites

- **Java 21** or higher
- **Maven 3.9+**
- **PostgreSQL** instance
- **Docker** (optional, for containerized deployment)

---

## ⚙️ Configuration

The application uses environment variables for sensitive configuration. Create a `.env` file in the root directory (or use the provided one) with the following variables:

```env
# Database
DB_USERNAME=postgres
DB_PASSWORD=your_password
DB_URL=jdbc:postgresql://localhost:5432/wayfarer

# Mailer (Example for Mailtrap)
MAILER_USERNAME=your_mailtrap_user
MAILER_PASSWORD=your_mailtrap_pass
MAILER_FROM=noreply@wayfare.com

# Admin Seeding
ADMIN_EMAIL=admin@wayfare.com
ADMIN_PASSWORD=admin123
```

---

## 🏃 Running the Application

### Development Mode
To run the application with live-coding enabled:
```bash
./mvnw quarkus:dev
```
The API will be available at `http://localhost:8080`.

### Database Migrations
Migrations are managed by Flyway and run automatically on startup.
- **Location**: `src/main/resources/db/migration/`
- **Format**: `V<YYYYMMDDHHMMSS>__Description.sql` (e.g., `V20260427102900__Consolidated_Schema.sql`)

### API Documentation
Once the app is running, you can explore and test the endpoints via Swagger UI:
- **Swagger UI**: `http://localhost:8080/api/swagger`
- **OpenAPI Spec**: `http://localhost:8080/api/openapi`

---

## 📦 Packaging and Deployment

### Uber-JAR
```bash
./mvnw package -Dquarkus.package.type=uber-jar
java -jar target/wayfare-1.0.0-SNAPSHOT-runner.jar
```

### Native Executable
To build a native image (requires GraalVM):
```bash
./mvnw package -Dnative
./target/wayfare-1.0.0-SNAPSHOT-runner
```

---

## 📂 Project Structure

- `org.acme.controller`: REST Resource endpoints (v1).
- `org.acme.service`: Business logic interfaces and implementations.
- `org.acme.entity`: Hibernate Panache entities.
- `org.acme.security`: JWT and Password utility classes.
- `org.acme.exception`: Global error handling and mapping.
- `resources/db/migration`: Flyway SQL migration scripts.

---

## 📄 License
This project is proprietary and confidential.
