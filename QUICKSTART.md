# Catalog Service - Quick Start Guide

## Prerequisites
- Java 17+
- Maven 3.6+
- PostgreSQL 14+

## Quick Start (3 Steps)

### 1. Start PostgreSQL
```bash
docker run -d \
  --name postgres-catalog \
  -e POSTGRES_DB=vogueThreads \
  -e POSTGRES_USER=devEccomerce \
  -e POSTGRES_PASSWORD='devEccomerce$' \
  -p 5432:5432 \
  postgres:14
```

### 2. Build the Application
```bash
mvn clean install
```

### 3. Run the Application
```bash
mvn spring-boot:run
```

Or use the run script:
```bash
./run.sh
```

## Quick Test

Health check:
```bash
curl http://localhost:8081/api/catalog/health
```

List products:
```bash
curl http://localhost:8081/api/catalog/products
```

## Using Docker Compose

Start everything with one command:
```bash
# Build and start
mvn clean package -DskipTests
docker-compose up -d

# View logs
docker-compose logs -f

# Stop
docker-compose down
```

## Testing

Run all tests:
```bash
mvn test
```

## Postman Collection

Import the collection from `postman/catalog-service.postman_collection.json` to test all endpoints.

## Common Issues

**Port 8081 already in use:**
```bash
# Kill process on port 8081
lsof -ti:8081 | xargs kill -9
```

**PostgreSQL connection failed:**
```bash
# Check if PostgreSQL is running
docker ps | grep postgres

# Check PostgreSQL logs
docker logs postgres-catalog
```

## Project Structure
```
catalog-service/
├── src/
│   ├── main/
│   │   ├── java/com/voguethreads/catalog/
│   │   │   ├── config/          # Security, configuration
│   │   │   ├── controller/      # REST endpoints
│   │   │   ├── dto/             # Request/Response objects
│   │   │   ├── exception/       # Error handling
│   │   │   ├── mapper/          # Entity-DTO mapping
│   │   │   ├── model/           # JPA entities
│   │   │   ├── repository/      # Data access
│   │   │   ├── security/        # JWT, filters
│   │   │   └── service/         # Business logic
│   │   └── resources/
│   │       ├── db/migration/    # Flyway migrations
│   │       └── application.yml  # Configuration
│   └── test/                    # Unit & integration tests
├── postman/                     # Postman collection
├── pom.xml                      # Maven dependencies
├── Dockerfile                   # Docker image
├── docker-compose.yml           # Docker Compose setup
└── README.md                    # Full documentation
```

## API Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | /health | None | Health check |
| GET | /products | None | List products |
| GET | /products/{id} | None | Get product |
| POST | /products | Admin | Create product |
| PUT | /products/{id} | Admin | Update product |
| DELETE | /products/{id} | Admin | Delete product |

## Next Steps

- See `README.md` for detailed documentation
- Import Postman collection for API testing
- Configure JWT_SECRET for production
- Review security configuration in `SecurityConfig.java`

