# Instagram Application - Spring Boot 3

A Spring Boot 3 backend application for an Instagram-like social media platform.

## Technologies Used

- **Spring Boot 3.2.0**
- **Java 17** (configured, but project works with Java 21+)
- **Maven** for dependency management
- **Spring Web** for REST APIs
- **Spring Data JPA** for database operations
- **Spring Security** for authentication and authorization
- **MySQL** as the database
- **Lombok** for reducing boilerplate code
- **Validation** for request validation

## Project Structure

```
src/main/java/com/instagram/
├── config/           # Configuration classes
├── controller/       # REST controllers
├── service/          # Business logic services
├── repository/       # Data access repositories
├── entity/          # JPA entities
├── dto/             # Data Transfer Objects
├── exception/       # Exception handling
└── InstagramApplication.java  # Main application class

src/main/resources/
└── application.yml   # Application configuration
```

## Features Implemented

### User Management
- User registration with validation
- User retrieval by ID or username
- Update user information
- Delete users
- List all users

### API Response Format
All API responses follow a consistent format:
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": {}
}
```

### Error Handling
- Global exception handling with `@RestControllerAdvice`
- Validation error handling
- Data integrity violation handling
- Resource not found handling

## Setup Instructions

### Prerequisites
1. **Java 17 or higher** (Java 21+ recommended)
2. **MySQL 8.0+** database
3. **Maven 3.6+** (or use the Maven wrapper)

### Database Setup
1. Create a MySQL database:
```sql
CREATE DATABASE instagram_db;
```

2. Update database credentials in `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/instagram_db
    username: your_username
    password: your_password
```

### Running the Application

#### Option 1: Using Maven Wrapper (Recommended)
```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

#### Option 2: Using installed Maven
```bash
mvn spring-boot:run
```

#### Option 3: Build and Run JAR
```bash
mvn clean package
java -jar target/instagram-application-0.0.1-SNAPSHOT.jar
```

### API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/users` | Create a new user |
| GET | `/api/users/{id}` | Get user by ID |
| GET | `/api/users/username/{username}` | Get user by username |
| GET | `/api/users` | Get all users |
| PUT | `/api/users/{id}` | Update user |
| DELETE | `/api/users/{id}` | Delete user |

### Sample User Creation Request
```json
POST /api/users
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "password123",
  "bio": "Software developer"
}
```

### Sample Response
```json
{
  "success": true,
  "message": "User created successfully",
  "data": {
    "id": 1,
    "username": "johndoe",
    "email": "john@example.com",
    "bio": "Software developer",
    "createdAt": "2026-02-06T22:05:00"
  }
}
```

## Configuration

The application is configured via `application.yml`:

- **Server port**: 8080
- **Database**: MySQL (localhost:3306)
- **JPA**: Auto DDL update enabled
- **Logging**: DEBUG level for application packages

## Development Notes

- The application uses Lombok annotations to reduce boilerplate code
- Entity validation is handled through Bean Validation annotations
- Passwords are stored as plain text (for demonstration - implement proper password encoding in production)
- CORS is configured for localhost:3000 and localhost:4200
- Spring Security is included but basic authentication is configured (replace with JWT in production)

## Next Steps

1. Implement password encoding using BCrypt
2. Add JWT authentication and authorization
3. Create additional entities (Post, Comment, Like, etc.)
4. Add pagination for user listings
5. Implement file upload for profile pictures
6. Add caching with Redis
7. Implement proper security configuration

## Troubleshooting

### Common Issues

1. **Database connection failed**: Check MySQL is running and credentials are correct
2. **Port already in use**: Change server port in `application.yml`
3. **ClassNotFoundException**: Ensure all Maven dependencies are downloaded

### Logging
Enable detailed logging by checking the `logging.level` configuration in `application.yml`.