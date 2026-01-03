# Payment Function Database Integration

This Azure Function now integrates with PostgreSQL, sharing the same database as marketplace-spring-ca.

## Database Configuration

### Local Development
Database credentials are configured in `local.settings.json`:
```json
{
  "DB_URL": "jdbc:postgresql://localhost:5432/marketplace",
  "DB_USERNAME": "postgres",
  "DB_PASSWORD": "postgres"
}
```

### Azure Deployment
Set these environment variables in Azure Function App Configuration:
- `DB_URL` - Your PostgreSQL connection string
- `DB_USERNAME` - Database username
- `DB_PASSWORD` - Database password

## Database Schema

The function uses the same tables as marketplace-spring-ca:

### users table
- `id` - Primary key
- `username` - Unique username
- `name` - User's name
- `email` - Email address
- `balance` - User's account balance (DECIMAL)
- `active` - Whether user account is active
- `updated_at` - Last update timestamp

### transfers table (parent)
- `id` - Primary key
- `amount` - Transfer amount (DECIMAL)
- `transfer_type` - Discriminator ('PAYMENT', 'WITHDRAWAL', 'PURCHASE')
- `created_at` - Timestamp

### payment_transfers table (child)
- `id` - Foreign key to transfers.id
- `user_id` - Foreign key to users.id

## Testing

Test the payment endpoint:
```bash
curl -X POST http://localhost:7071/api/Payment \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "amount": 100.00}'
```

Expected response:
```json
{
  "transferId": 123,
  "userId": 1,
  "amount": 100.00,
  "newBalance": 150.00,
  "timestamp": "2026-01-03T...",
  "status": "SUCCESS"
}
```

## Features

- **Transaction Safety**: Uses database transactions with automatic rollback on errors
- **Connection Pooling**: HikariCP for efficient connection management
- **Validation**: User existence and active status checks
- **Error Handling**: Detailed error messages for debugging
