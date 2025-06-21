# MongoDB Atlas Configuration Summary

## Connection Details
- **Connection String**: `mongodb+srv://rohannayakanti:2004%40Atlas@clg.fbq0zx8.mongodb.net/online-compiler?retryWrites=true&w=majority`
- **Database Name**: `online-compiler`
- **Collections Used**:
  - `users` - User accounts and settings
  - `snippets` - Code snippets and sharing data

## Connection String Breakdown
- **Protocol**: `mongodb+srv://` (MongoDB Atlas SRV connection)
- **Username**: `rohannayakanti`
- **Password**: `2004@Atlas` (URL encoded as `2004%40Atlas`)
- **Cluster**: `clg.fbq0zx8.mongodb.net`
- **Database**: `online-compiler`
- **Options**: `retryWrites=true&w=majority` (for reliable writes)

## Application Configuration

### Production (application.yml)
```yaml
spring:
  data:
    mongodb:
      uri: mongodb+srv://rohannayakanti:2004%40Atlas@clg.fbq0zx8.mongodb.net/online-compiler?retryWrites=true&w=majority
      database: online-compiler
```

### Development (application-dev.yml)
```yaml
spring:
  data:
    mongodb:
      uri: mongodb+srv://rohannayakanti:2004%40Atlas@clg.fbq0zx8.mongodb.net/online-compiler?retryWrites=true&w=majority
      database: online-compiler
```

## Data Models

### User Collection (`users`)
- User authentication data
- User settings and preferences
- API keys and permissions
- Activity timestamps

### Snippet Collection (`snippets`)
- Code snippets with metadata
- Sharing information
- Execution history
- User ownership links

## Security Notes
- Password is URL encoded in connection string
- Using write concern `w=majority` for data safety
- Retry writes enabled for network resilience
- Database and collection names are consistent

## Testing Connection
1. Start the application: `./mvnw spring-boot:run`
2. Check logs for successful MongoDB connection
3. Test user registration via `/api/auth/signup`
4. Verify data appears in MongoDB Atlas dashboard

## Fallback Configuration
If MongoDB Atlas is unavailable, the application will:
1. Log connection errors
2. Fall back to H2 in-memory database
3. Continue functioning with limited persistence
