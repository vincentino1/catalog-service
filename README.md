# Catalog Service

Catalog Service for VogueThreads E-Commerce Platform - A Java/Spring Boot microservice for managing product catalog.

## Overview

The Catalog Service is responsible for:
- CRUD operations for products 
- Product search and filtering
- Inventory management
- Price and availability information

## Technology Stack

- **Java 21**
- **Spring Boot 3.2.1**
- **Spring Data JPA**
- **Spring Security** (JWT authentication)
- **PostgreSQL** (database)
- **Flyway** (database migrations)
- **Maven** (build tool)
- **Lombok** (code generation)

## Prerequisites

- Java 21 or higher
- Maven 3.6+
- PostgreSQL 14+
- Docker (optional, for running PostgreSQL)

## Database Setup

### Option 1: Local PostgreSQL

1. Install PostgreSQL
2. Create database and user:

```sql
CREATE DATABASE vogueThreads;
CREATE USER devEccomerce WITH PASSWORD 'devEccomerce$';
GRANT ALL PRIVILEGES ON DATABASE vogueThreads TO devEccomerce;
```

### Option 2: Docker

```bash
docker run -d \
  --name postgres-catalog \
  -e POSTGRES_DB=vogueThreads \
  -e POSTGRES_USER=devEccomerce \
  -e POSTGRES_PASSWORD='devEccomerce$' \
  -p 5432:5432 \
  postgres:14
```

## Configuration

The service uses `application.yml` for configuration. Key settings:

- **Server Port**: 8081
- **Context Path**: `/api/catalog`
- **Database**: PostgreSQL at `localhost:5432/vogueThreads`
- **JWT Secret**: Configure via `JWT_SECRET` environment variable (production)

### Environment Variables

```bash
export JWT_SECRET="your-secret-key-change-this-in-production-must-be-at-least-256-bits"
```

## Build and Run

### Build the project

```bash
mvn clean install
```

### Run the application

```bash
mvn spring-boot:run
```

The service will be available at: `http://localhost:8081/api/catalog`

### Run tests

```bash
mvn test
```

## API Endpoints

### Health Check

```http
GET /api/catalog/health
```

Response:
```json
{
  "status": "ok"
}
```

### List Products

```http
GET /api/catalog/products?page=1&pageSize=20&query=shirt&category=clothing
```

Query Parameters:
- `page` (optional): Page number (default: 1)
- `pageSize` (optional): Items per page (default: 20, max: 100)
- `query` (optional): Search query (searches name, description, SKU)
- `category` (optional): Filter by category

Response:
```json
{
  "items": [
    {
      "id": "prod_1",
      "sku": "VT-SHIRT-001",
      "name": "Classic White T-Shirt",
      "description": "Premium cotton t-shirt",
      "price": {
        "currency": "USD",
        "amount": 2999
      },
      "inventory": {
        "inStock": true,
        "quantity": 100
      },
      "category": "clothing",
      "tags": ["shirt", "casual"],
      "createdAt": "2026-01-07T12:00:00Z",
      "updatedAt": "2026-01-07T12:00:00Z"
    }
  ],
  "page": 1,
  "pageSize": 20,
  "totalItems": 1,
  "totalPages": 1
}
```

### Get Product by ID

```http
GET /api/catalog/products/{id}
```

Response: Product object (same as above)

### Create Product (Admin Only)

```http
POST /api/catalog/products
Authorization: Bearer {JWT_TOKEN}
```

Request:
```json
{
  "sku": "VT-SHIRT-002",
  "name": "Blue Polo Shirt",
  "description": "Comfortable polo shirt",
  "currency": "USD",
  "amount": 3499,
  "quantity": 75,
  "category": "clothing",
  "tags": ["polo", "casual"]
}
```

Response: Created product (HTTP 201)

### Update Product (Admin Only)

```http
PUT /api/catalog/products/{id}
Authorization: Bearer {JWT_TOKEN}
```

Request: Same as create

Response: Updated product (HTTP 200)

### Delete Product (Admin Only)

```http
DELETE /api/catalog/products/{id}
Authorization: Bearer {JWT_TOKEN}
```

Response: No content (HTTP 204)

## Authentication

The service uses JWT Bearer tokens for authentication. Admin endpoints require the `ADMIN` role.

JWT token should be included in the `Authorization` header:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

Expected JWT claims:
- `sub`: User ID
- `email`: User email
- `roles`: Array of roles (e.g., ["USER", "ADMIN"])
- `exp`: Expiration timestamp

## Error Handling

All errors follow a consistent format:

```json
{
  "error": {
    "code": "PRODUCT_NOT_FOUND",
    "message": "Product not found with id: 999",
    "details": {},
    "traceId": "abc-123-def-456"
  }
}
```

Error Codes:
- `VALIDATION_ERROR` (400): Request validation failed
- `UNAUTHORIZED` (401): Missing or invalid token
- `ACCESS_DENIED` (403): Insufficient permissions
- `PRODUCT_NOT_FOUND` (404): Product not found
- `DUPLICATE_SKU` (409): SKU already exists
- `INTERNAL_ERROR` (500): Server error

## Database Schema

### Products Table

```sql
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    sku VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    currency VARCHAR(3) NOT NULL,
    amount INTEGER NOT NULL,
    quantity INTEGER NOT NULL,
    in_stock BOOLEAN NOT NULL DEFAULT false,
    category VARCHAR(100),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

### Product Tags Table

```sql
CREATE TABLE product_tags (
    product_id BIGINT NOT NULL,
    tag VARCHAR(255),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);
```

## Database Migrations

Migrations are managed by Flyway and located in `src/main/resources/db/migration/`.

To manually run migrations:

```bash
mvn flyway:migrate
```

## Logging

The service uses SLF4J with Logback. Log levels can be configured in `application.yml`:

```yaml
logging:
  level:
    com.voguethreads: DEBUG
    org.springframework.web: INFO
```

## Docker Support

Build Docker image:

```bash
docker build -t catalog-service:latest .
```

Run with Docker:

```bash
docker run -d \
  --name catalog-service \
  -p 8081:8081 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/vogueThreads \
  -e JWT_SECRET=your-secret-key \
  catalog-service:latest
```

## Production Considerations

1. **JWT Secret**: Use a strong, randomly generated secret (at least 256 bits)
2. **Database**: Use connection pooling (already configured via HikariCP)
3. **Logging**: Configure appropriate log levels (INFO or WARN for production)
4. **Monitoring**: Use Spring Boot Actuator endpoints
5. **CORS**: Configure CORS if needed for frontend integration
6. **Rate Limiting**: Consider adding rate limiting for public endpoints
7. **Caching**: Consider adding Redis for frequently accessed products

## Sample Data

The migration includes 5 sample products:
- Classic White T-Shirt
- Slim Fit Denim Jeans
- Running Sneakers
- Leather Messenger Bag
- Minimalist Watch

## Support

For issues or questions, please refer to the technical documentation or contact the development team.

