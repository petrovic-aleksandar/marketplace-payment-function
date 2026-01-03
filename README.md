# Marketplace Payment Function

An Azure Functions application that processes payments for the marketplace, integrated with the `marketplace-spring-ca` application using a shared PostgreSQL database.

## Overview

This serverless payment processing function is built with:
- **Azure Functions** for serverless computing
- **Java 25** runtime
- **PostgreSQL** database (shared with marketplace-spring-ca)
- **JDBC** for lightweight database access
- **HikariCP** for connection pooling

## Features

✅ Process user payment transactions  
✅ Update user account balance atomically  
✅ Record payment transfers in database  
✅ Transaction safety with automatic rollback  
✅ Lightweight and optimized for serverless  

## Prerequisites

- Java 25+
- Maven 3.8+
- PostgreSQL 12+ (running locally or remote)
- Azure Functions Core Tools (for local development)
- Git

## Project Structure

```
marketplace-payment-function/
├── src/
│   ├── main/java/me/aco/marketplace/
│   │   ├── functions/          # Azure Function endpoint
│   │   └── payment/
│   │       ├── core/           # Business logic
│   │       └── database/       # JDBC repositories
│   └── test/java/              # Unit tests
├── pom.xml                      # Maven dependencies
├── local.settings.json          # Local configuration (excluded from git)
└── DATABASE.md                  # Database setup guide
```

## Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd marketplace-payment-function
```

### 2. Configure Database

Edit `local.settings.json` with your database credentials:

```json
{
  "IsEncrypted": false,
  "Values": {
    "AzureWebJobsStorage": "",
    "FUNCTIONS_WORKER_RUNTIME": "java",
    "DB_URL": "jdbc:postgresql://localhost:5432/marketplace",
    "DB_USERNAME": "postgres",
    "DB_PASSWORD": "your-password"
  }
}
```

Ensure PostgreSQL is running with the `marketplace` database created.

### 3. Build the Project

```bash
mvn clean package
```

### 4. Run Locally

```bash
mvn azure-functions:run
```

The function will start at `http://localhost:7071`

## Testing

### Run Unit Tests

```bash
mvn test
```

Expected output: 4 tests passing
- `testProcessPaymentSuccess` - Valid JSON handling
- `testProcessPaymentMissingBody` - Missing body validation
- `testProcessPaymentInvalidJson` - Invalid JSON handling
- Additional validation tests

## API Endpoint

### Process Payment

**POST** `/api/Payment`

**Request Body:**
```json
{
  "userId": 1,
  "amount": 50.00
}
```

**Success Response (200 OK):**
```json
{
  "transferId": 123,
  "userId": 1,
  "amount": 50.00,
  "newBalance": 150.00,
  "timestamp": "2026-01-03T10:30:00",
  "status": "SUCCESS"
}
```

**Error Response (400/500):**
```json
{
  "transferId": null,
  "userId": 1,
  "amount": 50.00,
  "newBalance": null,
  "timestamp": "2026-01-03T10:30:00",
  "status": "FAILED: User not found"
}
```

### Test with cURL

```bash
curl -X POST http://localhost:7071/api/Payment \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "amount": 100.00}'
```

## Database Integration

This function shares the same PostgreSQL database as `marketplace-spring-ca`, using the following tables:

- **users** - User account information and balance
- **transfers** - Parent table for all transfer types
- **payment_transfers** - Payment-specific transfer records

For detailed schema information, see [DATABASE.md](DATABASE.md)

## Connection to marketplace-spring-ca

Both applications use:
- Same PostgreSQL database
- Same domain models (User, Transfer, PaymentTransfer)
- Same validation rules

The payment function can be used as a standalone microservice or integrated into the Spring application's workflow.

## Deployment to Azure

### 1. Create Azure Function App

```bash
az functionapp create \
  --resource-group <resource-group> \
  --consumption-plan-location <region> \
  --runtime java \
  --runtime-version 21 \
  --functions-version 4 \
  --name <function-app-name>
```

### 2. Set Environment Variables

```bash
az functionapp config appsettings set \
  --resource-group <resource-group> \
  --name <function-app-name> \
  --settings \
    DB_URL="jdbc:postgresql://server.postgres.database.azure.com:5432/marketplace?ssl=true&sslmode=require" \
    DB_USERNAME="username@server" \
    DB_PASSWORD="password"
```

### 3. Deploy

```bash
mvn azure-functions:deploy
```

## Performance Considerations

- **Cold Start**: Optimized with lightweight JDBC (no Spring overhead)
- **Connection Pooling**: HikariCP configured for serverless environments
- **Transaction Handling**: Short, atomic transactions
- **Error Handling**: Detailed logging for debugging

## Configuration

### Database Connection Pool Settings

Adjust in `DatabaseConfig.java`:

```java
config.setMaximumPoolSize(5);      // Max connections
config.setMinimumIdle(1);           // Min idle connections
config.setConnectionTimeout(10000); // 10 second timeout
```

For serverless, these conservative settings prevent resource exhaustion.

## Troubleshooting

### Database Connection Errors

1. Verify PostgreSQL is running
2. Check `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` in `local.settings.json`
3. Ensure firewall allows connections
4. Test connection: `psql -h localhost -U postgres -d marketplace`

### Build Failures

```bash
# Clean and rebuild
mvn clean install

# Check Java version
java -version
# Should be 21.x or higher
```

### Test Failures

- Ensure you're not relying on database for unit tests
- Database integration tests require PostgreSQL running
- Run `mvn test -DskipTests` to skip tests during builds

## Project Status

✅ Core payment processing implemented  
✅ Database integration complete  
✅ Unit tests passing  
⏳ Integration tests (with real DB)  
⏳ Load testing  

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/description`
3. Commit changes: `git commit -am 'Add feature'`
4. Push to branch: `git push origin feature/description`
5. Submit a Pull Request

## License

[Your License Here]

## Support

For issues, questions, or contributions, please open an issue on GitHub.

## Related Projects

- [marketplace-spring-ca](https://github.com/your-org/marketplace-spring-ca) - Main Spring Boot application
- [jpa-hibernate](https://github.com/your-org/jpa-hibernate) - Database abstraction layer

## Author

Aleksandar Petrovic
