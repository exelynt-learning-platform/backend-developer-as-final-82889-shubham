# backend-developer-as-final-82889-shubham
Final Project Assignment - This repository contains the complete final project code and documentation.
# Resource Booking System

A backend REST API for managing bookable resources and reservations with role-based access control, JWT authentication, validation, pagination, filtering, sorting, and conflict prevention.

## Overview

The Resource Booking System allows administrators to manage resources and reservations, while normal users can browse available resources and create/manage their own reservations.

The application is built using Spring Boot and follows a layered architecture with separate controllers, services, repositories, DTOs, security components, entities, specifications, and exception handling.

## Key Features

### Authentication and Authorization

- JWT-based authentication.
- BCrypt password hashing.
- Two roles:
  - `ADMIN`
  - `USER`
- Stateless Spring Security configuration.
- Role-based authorization for protected APIs.
- User identity for reservations is obtained from the authenticated JWT/security context rather than from request payloads.
- Invalid credentials are rejected.
- Protected endpoints require a valid JWT.

### Resource Management

Administrators can:

- Create resources.
- View a resource by ID.
- View all resources.
- Update resources.
- Delete resources.

Users can:

- View resources.
- View a specific resource.

A resource contains:

- ID
- Name
- Description
- Type
- Price
- Availability

### Reservation Management

Users can:

- Create reservations.
- View their own reservations.
- View an individual reservation when authorized.
- Cancel their own reservations.

Administrators can:

- View all reservations.
- Manage reservation status.
- Delete reservations.
- Access reservations across users.

Reservation statuses:

```text
PENDING
CONFIRMED
CANCELLED
```

### Reservation Validation

The system validates:

- Resource existence.
- Start time and end time.
- Resource availability.
- Overlapping reservations.
- Valid reservation status transitions.
- Minimum and maximum price filters.
- Request body validation.

The system prevents overlapping active reservations for the same resource and time range.

The reservation stores the resource price at the time of booking as a price snapshot. This prevents a later resource-price change from modifying the historical reservation price.

### Pagination, Filtering and Sorting

The reservation listing API supports:

- Page number.
- Page size.
- Sorting.
- Status filtering.
- Minimum price filtering.
- Maximum price filtering.
- Combined filters.

Example:

```text
GET /reservations?page=0&size=10&sort=price,desc
```

Example with filters:

```text
GET /reservations?status=PENDING&minPrice=500&maxPrice=3000&page=0&size=10&sort=price,asc
```

### Validation and Error Handling

The application uses Bean Validation for request validation.

Examples include:

- Required resource name.
- Required resource type.
- Positive resource price.
- Required availability.
- Positive resource ID.
- Required reservation start and end time.

A centralized `GlobalExceptionHandler` handles application-level exceptions and validation errors.

Common response statuses include:

- `200 OK`
- `201 CREATED`
- `204 NO CONTENT`
- `400 BAD REQUEST`
- `401 UNAUTHORIZED`
- `403 FORBIDDEN`
- `404 NOT FOUND`
- `409 CONFLICT`
- `500 INTERNAL SERVER ERROR`

### API Documentation

Swagger/OpenAPI documentation is available for exploring and testing the REST APIs.

Typical Swagger URLs:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI specification:

```text
http://localhost:8080/v3/api-docs
```

## Technology Stack

### Backend

- Java 21
- Spring Boot
- Spring Web
- Spring Security
- Spring Data JPA
- Hibernate
- Bean Validation
- Lombok

### Database

- MySQL 8
- JPA/Hibernate ORM

### Security

- Spring Security
- JWT
- JJWT
- BCrypt

### API Documentation

- Springdoc OpenAPI / Swagger UI

### Build

- Maven

### Development and Testing

- Postman
- Docker

## Project Architecture

```text
src/main/java/com/booking
│
├── config
│   └── DataSeeder.java
│
├── controller
│   ├── AuthController.java
│   ├── ResourceController.java
│   └── ReservationController.java
│
├── dto
│   ├── request
│   │   ├── LoginRequest.java
│   │   ├── ResourceRequest.java
│   │   └── ReservationRequest.java
│   │
│   └── response
│       ├── LoginResponse.java
│       ├── ResourceResponse.java
│       └── ReservationResponse.java
│
├── entity
│   ├── User.java
│   ├── Resource.java
│   └── Reservation.java
│
├── enums
│   ├── Role.java
│   └── ReservationStatus.java
│
├── exception
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   ├── BadRequestException.java
│   └── DuplicateResourceException.java
│
├── repository
│   ├── UserRepository.java
│   ├── ResourceRepository.java
│   └── ReservationRepository.java
│
├── security
│   ├── JwtService.java
│   ├── JwtAuthenticationFilter.java
│   ├── CustomUserDetailsService.java
│   └── SecurityConfig.java
│
├── service
│   ├── AuthService.java
│   ├── ResourceService.java
│   ├── ReservationService.java
│   │
│   └── impl
│       ├── AuthServiceImpl.java
│       ├── ResourceServiceImpl.java
│       └── ReservationServiceImpl.java
│
└── specification
    └── ReservationSpecification.java
```

## Architecture Explanation

The application follows a layered architecture.

### Controller Layer

Controllers expose REST endpoints and are responsible for:

- Receiving HTTP requests.
- Validating request bodies.
- Reading path/query parameters.
- Calling the appropriate service.
- Returning HTTP responses.

Business logic is kept outside controllers.

### Service Layer

The service layer contains business logic.

The project uses interfaces and implementations:

```text
Service Interface
       ↓
ServiceImpl
       ↓
Repository
```

This provides loose coupling and makes the business layer easier to test and maintain.

### Repository Layer

Repositories use Spring Data JPA to communicate with the database.

They provide:

- CRUD operations.
- User lookup by email.
- Reservation lookup by user.
- Reservation overlap checking.
- Pagination and filtering through `JpaSpecificationExecutor`.

### Entity Layer

The main entities are:

```text
User
  |
  | 1
  |
  | *
Reservation
  |
  | *
  |
  | 1
Resource
```

A user can have multiple reservations.

A resource can have multiple reservations.

Each reservation belongs to exactly one user and one resource.

### DTO Layer

DTOs are used instead of exposing JPA entities directly through REST APIs.

Request DTOs contain client-provided input.

Response DTOs contain data returned to clients.

For example, `ReservationRequest` does not contain:

- `userId`
- `status`
- `price`

This prevents clients from controlling values that should be managed by the backend.

## Database Relationships

### User → Reservation

```text
User 1 -------- * Reservation
```

A user can create multiple reservations.

### Resource → Reservation

```text
Resource 1 -------- * Reservation
```

A resource can be associated with multiple reservations over different time periods.

### Reservation

A reservation contains:

```text
id
user
resource
startTime
endTime
price
status
```

## Authentication Flow

The authentication process works as follows:

```text
Client
  |
  | POST /auth/login
  | email + password
  ↓
AuthController
  ↓
AuthService
  ↓
UserRepository
  ↓
BCrypt password verification
  ↓
JwtService
  ↓
JWT Token
  ↓
Client
```

For protected APIs:

```text
Client
  |
  | Authorization: Bearer <JWT>
  ↓
JwtAuthenticationFilter
  ↓
Validate JWT
  ↓
Extract email
  ↓
Load UserDetails
  ↓
Set Authentication in SecurityContext
  ↓
Controller
```

## JWT

The generated JWT contains:

- User email as the subject.
- User ID.
- User role.
- Issued-at time.
- Expiration time.

The token is signed using a configured secret.

JWT configuration is supplied through environment variables where possible.

Example:

```yaml
jwt:
  secret: ${JWT_SECRET:mySecretKeyForResourceBookingSystemJwt2026VerySecure}
  expiration: 3600000
```

For a real production environment, the default secret should be replaced with a strong secret stored outside source control.

## Authorization Model

### ADMIN

Administrators can:

```text
Resources
├── Create
├── Read
├── Update
└── Delete

Reservations
├── Read all
├── Update status
└── Delete
```

### USER

Normal users can:

```text
Resources
└── Read

Reservations
├── Create
├── Read own
└── Cancel own
```

The backend verifies ownership using the authenticated user rather than trusting a user ID supplied by the client.

## API Endpoints

### Authentication

#### Login

```http
POST /auth/login
```

Request:

```json
{
  "email": "admin@booking.com",
  "password": "admin123"
}
```

Response contains:

```text
token
userId
email
role
```

### Resources

#### Create Resource

```http
POST /resources
```

ADMIN only.

Example request:

```json
{
  "name": "Conference Room A",
  "description": "Large meeting room with projector and Wi-Fi",
  "type": "ROOM",
  "price": 1500.00,
  "available": true
}
```

#### Get All Resources

```http
GET /resources
```

Authenticated users.

#### Get Resource

```http
GET /resources/{id}
```

Authenticated users.

#### Update Resource

```http
PUT /resources/{id}
```

ADMIN only.

#### Delete Resource

```http
DELETE /resources/{id}
```

ADMIN only.

### Reservations

#### Create Reservation

```http
POST /reservations
```

Example:

```json
{
  "resourceId": 1,
  "startTime": "2026-09-29T10:00:00",
  "endTime": "2026-09-29T12:00:00"
}
```

The authenticated user is automatically associated with the reservation.

The client does not provide:

```text
userId
price
status
```

#### Get My Reservations

```http
GET /reservations/my
```

Returns reservations belonging to the authenticated user.

#### Get Reservation

```http
GET /reservations/{id}
```

A normal user can access only their own reservation. An administrator can access reservations according to the administrative authorization rules.

#### Get All Reservations

```http
GET /reservations?page=0&size=10
```

Supports filtering and sorting.

Example:

```text
GET /reservations?status=CONFIRMED&minPrice=500&maxPrice=3000&page=0&size=10&sort=price,desc
```

#### Update Reservation Status

```http
PATCH /reservations/{id}/status
```

Example:

```text
/reservations/1/status?status=CONFIRMED
```

#### Cancel Reservation

```http
PATCH /reservations/{id}/cancel
```

A normal user can cancel only their own reservation. An administrator can cancel any reservation.

#### Delete Reservation

```http
DELETE /reservations/{id}
```

Administrative operation.

## Reservation Overlap Logic

The system prevents conflicting active reservations for the same resource.

The overlap condition is conceptually:

```text
existing.startTime < requested.endTime
AND
existing.endTime > requested.startTime
```

Cancelled reservations are excluded from the overlap check.

This allows reservations such as:

```text
10:00 - 11:00
11:00 - 12:00
```

while preventing:

```text
10:00 - 11:30
11:00 - 12:00
```

for the same resource.

## Pagination

Spring Data's `Pageable` is used for reservation pagination.

Example:

```text
?page=0&size=10
```

Where:

- `page=0` means the first page.
- `size=10` means ten records per page.

Sorting example:

```text
?sort=price,desc
```

Multiple sorting criteria can also be supplied when supported by Spring Data's `Pageable`.

## Filtering

Reservation filtering is implemented using JPA `Specification`.

Supported filters:

```text
status
minPrice
maxPrice
```

The specification dynamically creates predicates only for the filters provided by the client.

For example:

```text
status=CONFIRMED
```

creates a status predicate.

```text
minPrice=500
```

creates a minimum-price predicate.

Multiple filters are combined using logical AND.

## Validation Examples

Resource price must be greater than zero.

```text
price > 0
```

Resource name cannot be blank.

Resource type cannot be blank.

Reservation resource ID must be positive.

Reservation start and end time must be provided.

The service layer additionally validates:

```text
startTime < endTime
```

## Exception Handling

The application uses custom exceptions such as:

```text
ResourceNotFoundException
BadRequestException
DuplicateResourceException
```

`GlobalExceptionHandler` converts exceptions into structured API responses.

Example error:

```json
{
  "timestamp": "2026-09-28T15:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Resource not found with id: 100",
  "path": "/resources/100"
}
```

Validation errors are returned as structured field-level errors.

## Configuration

Application configuration is stored in:

```text
src/main/resources/application.yml
```

Important configuration areas include:

- Application name.
- Database connection.
- JPA/Hibernate.
- Server port.
- JWT configuration.

Example structure:

```yaml
spring:
  application:
    name: resource-booking-system

  datasource:
    url: ${DB_URL:jdbc:mysql://localhost:3306/resource_booking_db}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:your-password}
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

server:
  port: 8080
```

Environment variables are preferred for credentials and secrets.

## Seeded Users

For local development/demo purposes, the application seeds two users if they do not already exist.

### ADMIN

```text
Email: admin@booking.com
Password: admin123
Role: ADMIN
```

### USER

```text
Email: user@booking.com
Password: user123
Role: USER
```

These credentials are intended for development/demo use. Production credentials should be managed securely and should not be hardcoded.

## Sample Resources

Example resource:

```json
{
  "name": "Conference Room A",
  "description": "Large meeting room with projector and Wi-Fi",
  "type": "ROOM",
  "price": 1500.00,
  "available": true
}
```

Additional example:

```json
{
  "name": "Toyota Innova",
  "description": "7-seater vehicle",
  "type": "VEHICLE",
  "price": 3000.00,
  "available": true
}
```

## Local Setup

### Prerequisites

Install:

- Java 21
- Maven
- MySQL 8
- Git

### Clone the Repository

```bash
git clone <repository-url>
cd Resource-Booking-System
```

### Configure MySQL

Create the database:

```sql
CREATE DATABASE resource_booking_db;
```

Configure the database credentials in `application.yml` or through environment variables.

### Build

Using Maven:

```bash
mvn clean package
```

To skip tests:

```bash
mvn clean package -DskipTests
```

### Run

```bash
mvn spring-boot:run
```

Or run the generated JAR:

```bash
java -jar target/Resource-Booking-System-0.0.1-SNAPSHOT.jar
```

The application starts on:

```text
http://localhost:8080
```

## Testing with Postman

Recommended testing sequence:

```text
1. Login as ADMIN
2. Copy JWT token
3. Create resource
4. Update resource
5. Delete resource
6. Login as USER
7. Read resources
8. Create reservation
9. Read own reservations
10. Test overlapping reservation
11. Test unauthorized resource operations
12. Test reservation filters
13. Test pagination
14. Test sorting
15. Test validation errors
```

### Authorization Header

For protected endpoints:

```http
Authorization: Bearer <JWT_TOKEN>
```

## Security Considerations

The application implements several security practices:

- Stateless JWT authentication.
- BCrypt password hashing.
- Role-based endpoint authorization.
- Ownership checks for user reservations.
- Server-controlled user identity.
- Client cannot directly set reservation price.
- Client cannot directly assign reservation ownership.
- Client cannot directly set reservation status during creation.
- Secrets can be supplied through environment variables.
- Application can run under a non-root operating-system user.

For a production deployment, additionally use:

- Strong randomly generated JWT secret.
- HTTPS/TLS.
- Secure secret management.
- Database least-privilege credentials.
- Proper database backups.
- Monitoring and centralized logging.
- Rate limiting where appropriate.
- Strict CORS configuration based on actual clients.
- Database migrations instead of relying on `ddl-auto: update`.

## Design Decisions

### Why DTOs?

DTOs prevent direct exposure of persistence entities and allow API contracts to remain independent of the database model.

### Why Service Interfaces?

The service layer uses interfaces and implementations to provide loose coupling and make the business layer easier to test and replace.

### Why Store Reservation Price?

The reservation stores a price snapshot.

If the resource price changes later:

```text
Resource price:
1500 → 2000
```

an existing reservation still retains:

```text
Reservation price:
1500
```

This preserves historical booking information.

### Why JWT?

JWT provides stateless authentication suitable for REST APIs.

The server validates the token on protected requests rather than maintaining an HTTP session.

### Why Specification?

`JpaSpecificationExecutor` and `Specification` allow dynamic filtering without creating a separate repository method for every possible combination of filters.

For example, instead of creating methods for:

```text
findByStatus
findByMinPrice
findByMaxPrice
findByStatusAndMinPrice
findByStatusAndMaxPrice
...
```

a specification dynamically builds the required predicates.

## Project Flow

The complete reservation flow is:

```text
Client
  ↓
JWT Authentication
  ↓
ReservationController
  ↓
ReservationService
  ↓
Validate authenticated user
  ↓
Find resource
  ↓
Validate time range
  ↓
Check resource availability
  ↓
Check overlapping reservations
  ↓
Create reservation
  ↓
Set authenticated user
  ↓
Take current resource price as snapshot
  ↓
Set status = PENDING
  ↓
Save to database
  ↓
Return ReservationResponse
```

## Future Improvements

Potential improvements for a larger production system include:

- Database migration with Flyway or Liquibase.
- Refresh-token mechanism.
- Redis-based caching.
- Rate limiting.
- Centralized logging.
- Metrics with Micrometer and Prometheus.
- Distributed tracing.
- Automated integration tests.
- Testcontainers for database integration tests.
- CI/CD pipeline.
- HTTPS and reverse proxy.
- External secret management.
- Amazon RDS for managed database hosting.
- Health checks and observability.
- Reservation locking/stronger concurrency controls for high-traffic booking scenarios.

## Author

**Shubham Mungase**

Java Backend Developer | Spring Boot | REST APIs | JPA/Hibernate | Spring Security | JWT | MySQL

GitHub: `https://github.com/Shubham-Mungase`

Portfolio: `https://shubhammungase.netlify.app`

LinkedIn: `https://www.linkedin.com/in/shubham-mungase-b635222a5/`

## License

This project was developed as a backend developer assignment and learning project.
