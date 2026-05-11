# Contract Management System - MVP

A microservices-based Contract Management System built with Java Spring Boot, JPA, and Docker.

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              API Gateway                                     │
│                           (Spring Cloud Gateway)                             │
│                              Port: 8080                                      │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
         ┌────────────────────────────┼────────────────────────────┐
         │                            │                            │
         ▼                            ▼                            ▼
┌─────────────────┐      ┌─────────────────────┐      ┌─────────────────────┐
│  Collaborator   │      │     Contract        │      │   Notification      │
│    Service      │      │     Service         │      │     Service         │
│   Port: 8081    │      │    Port: 8082       │      │    Port: 8083       │
└─────────────────┘      └─────────────────────┘      └─────────────────────┘
         │                            │                            │
         ▼                            ▼                            ▼
┌─────────────────┐      ┌─────────────────────┐      ┌─────────────────────┐
│   PostgreSQL    │      │    PostgreSQL       │      │    PostgreSQL       │
│  collaborator_db│      │   contract_db       │      │  notification_db    │
│   Port: 5433    │      │   Port: 5434        │      │    Port: 5435       │
└─────────────────┘      └─────────────────────┘      └─────────────────────┘
```

## Services

| Service | Port | Description |
|---------|------|-------------|
| API Gateway | 8080 | Routes requests to microservices |
| Collaborator Service | 8081 | Manages collaborators and performance reviews |
| Contract Service | 8082 | Manages contracts, renewals, and terminations |
| Notification Service | 8083 | Handles email notifications |
| MailHog | 8025 | Email testing UI |
| PgAdmin | 5050 | Database administration |

## Prerequisites

- Java 17+
- Maven 3.9+
- Docker & Docker Compose
- PostgreSQL (for local development without Docker)

## Quick Start with Docker

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd contract-management-server
   ```

2. **Build and start all services**
   ```bash
   docker-compose up --build
   ```

3. **Access the services**
   - API Gateway: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - MailHog UI: http://localhost:8025
   - PgAdmin: http://localhost:5050 (admin@cms.com / admin)

## Local Development

### Start databases only
```bash
docker-compose up collaborator-db contract-db notification-db mailhog -d
```

### Build the project
```bash
mvn clean install -DskipTests
```

### Run individual services
```bash
# Collaborator Service
cd collaborator-service
mvn spring-boot:run

# Contract Service
cd contract-service
mvn spring-boot:run

# Notification Service
cd notification-service
mvn spring-boot:run

# API Gateway
cd api-gateway
mvn spring-boot:run
```

## API Endpoints

### Collaborator Service (`/api/v1/collaborators`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/` | Create collaborator |
| GET | `/{id}` | Get collaborator by ID |
| GET | `/` | Get all collaborators (paginated) |
| PUT | `/{id}` | Update collaborator |
| DELETE | `/{id}` | Delete collaborator |
| GET | `/{id}/performance-reviews` | Get collaborator's reviews |

### Performance Reviews (`/api/v1/performance-reviews`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/` | Submit performance review |
| GET | `/{id}` | Get review by ID |
| GET | `/collaborator/{id}` | Get reviews by collaborator |
| GET | `/collaborator/{id}/latest` | Get latest review |
| GET | `/collaborator/{id}/average-rating` | Get average rating |

### Contract Service (`/api/v1/contracts`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/` | Create contract |
| GET | `/{id}` | Get contract by ID |
| GET | `/` | Get all contracts (paginated) |
| GET | `/collaborator/{id}` | Get contract by collaborator |
| GET | `/expiring-soon` | Get expiring contracts |
| PUT | `/{id}` | Update contract |
| PUT | `/{id}/renew` | Renew contract |
| PUT | `/{id}/terminate` | Terminate contract |

### Notification Service (`/api/v1/notifications`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/` | Create notification |
| POST | `/send` | Create and send notification |
| GET | `/{id}` | Get notification by ID |
| GET | `/` | Get all notifications (paginated) |
| GET | `/contract/{id}` | Get notifications by contract |
| GET | `/pending` | Get pending notifications |
| PUT | `/{id}/cancel` | Cancel notification |

## Database Schema

### Collaborator Service
- `collaborators` - Employee information
- `performance_reviews` - Performance review records

### Contract Service
- `contracts` - Contract details with self-referencing for renewals

### Notification Service
- `expiry_notifications` - Notification records

## Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_DATASOURCE_URL` | - | Database connection URL |
| `SPRING_DATASOURCE_USERNAME` | cms_user | Database username |
| `SPRING_DATASOURCE_PASSWORD` | cms_password | Database password |
| `COLLABORATOR_SERVICE_URL` | http://localhost:8081 | Collaborator service URL |
| `CONTRACT_SERVICE_URL` | http://localhost:8082 | Contract service URL |
| `NOTIFICATION_SERVICE_URL` | http://localhost:8083 | Notification service URL |
| `SPRING_MAIL_HOST` | localhost | SMTP host |
| `SPRING_MAIL_PORT` | 1025 | SMTP port |

## Testing

### Run unit tests
```bash
mvn test
```

### Run integration tests
```bash
mvn verify
```

## Project Structure

```
contract-management-system/
├── common/                     # Shared DTOs, exceptions, utilities
├── api-gateway/               # Spring Cloud Gateway
├── collaborator-service/      # Collaborator management
├── contract-service/          # Contract management
├── notification-service/      # Notification management
├── docker-compose.yml         # Docker orchestration
└── pom.xml                    # Parent POM
```

## Key Features

- **Microservices Architecture**: Independent, scalable services
- **API Gateway**: Centralized routing and cross-cutting concerns
- **Database per Service**: Each service has its own PostgreSQL database
- **Flyway Migrations**: Version-controlled database schema
- **OpenAPI Documentation**: Swagger UI for API exploration
- **Email Notifications**: Automated contract expiry notifications
- **Performance-based Renewals**: Contract renewal eligibility based on reviews
