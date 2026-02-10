# Authentication API Testing Guide

## API Endpoints

### 1. Signup API
**POST** `/api/auth/signup`

**Request Body:**
```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "password123",
  "bio": "Software developer"
}
```

**Response:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "type": "Bearer",
    "id": 1,
    "username": "johndoe",
    "email": "john@example.com",
    "role": "USER"
  }
}
```

### 2. Login API
**POST** `/api/auth/login`

**Request Body:**
```json
{
  "email": "john@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "type": "Bearer",
    "id": 1,
    "username": "johndoe",
    "email": "john@example.com",
    "role": "USER"
  }
}
```

### 3. Protected User API (requires JWT)
**GET** `/api/users/1`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

**Response:**
```json
{
  "success": true,
  "message": "User retrieved successfully",
  "data": {
    "id": 1,
    "username": "johndoe",
    "email": "john@example.com",
    "bio": "Software developer",
    "createdAt": "2026-02-06T22:05:00"
  }
}
```

## Testing with curl

### Signup
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "password": "password123",
    "bio": "Software developer"
  }'
```

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "password123"
  }'
```

### Access Protected Endpoint
```bash
curl -X GET http://localhost:8080/api/users/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

## Security Features Implemented

1. **Password Encryption**: Uses BCryptPasswordEncoder with strength 10
2. **JWT Authentication**: HS512 algorithm with 24-hour expiration
3. **Stateless Sessions**: No server-side session storage
4. **Role-Based Access**: Users have USER role by default
5. **Protected Endpoints**: All endpoints except /api/auth/** require authentication
6. **Input Validation**: Comprehensive validation on all request bodies
7. **Error Handling**: Proper error responses for authentication failures

## Database Setup

Make sure your MySQL database is running and configured in `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/instagram_db
    username: your_username
    password: your_password
```

## Running the Application

```bash
# Using Maven wrapper
.\mvnw.cmd spring-boot:run

# Or with JAVA_HOME set
$env:JAVA_HOME="C:\Program Files\Java\jdk-21"; .\mvnw.cmd spring-boot:run
```

The application will start on port 8080.