# 🛒 BuyNest - Enterprise E-Commerce Microservices Platform

<div align="center">

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025.0.0-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-231F20?style=for-the-badge&logo=apachekafka&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)

**A production-ready, scalable e-commerce platform built with microservices architecture**

[Features](#-features) • [Architecture](#-architecture) • [Quick Start](#-quick-start) • [Services](#-microservices) • [API Documentation](#-api-documentation) • [Monitoring](#-monitoring--observability)

</div>

---

## 📋 Overview

BuyNest is a comprehensive e-commerce platform designed with modern microservices principles. It demonstrates enterprise-grade patterns including service discovery, API gateway, event-driven communication, circuit breakers, and centralized monitoring. The platform is fully containerized and ready for deployment in cloud environments.

## ✨ Features

### Core E-Commerce Functionality
- 🔐 **User Management** - Registration, authentication, and profile management with JWT security
- 📦 **Product Catalog** - Full product lifecycle management with categories and inventory tracking
- 🛒 **Shopping Cart** - Persistent cart with Redis caching for optimal performance
- 📋 **Order Processing** - Complete order workflow with status tracking
- 💳 **Payment Integration** - Secure payment processing with webhook support
- 📧 **Notifications** - Real-time email and SMS notifications via Kafka

### Technical Capabilities
- 🔄 **Service Discovery** - Netflix Eureka for dynamic service registration
- 🚪 **API Gateway** - Centralized routing with Spring Cloud Gateway
- 📨 **Event-Driven Architecture** - Apache Kafka for asynchronous communication
- ⚡ **Caching** - Redis for session management and data caching
- 🛡️ **Resilience** - Circuit breakers with Resilience4j
- 📊 **Observability** - Prometheus metrics and Grafana dashboards
- 🗃️ **Database Per Service** - Isolated PostgreSQL databases for each microservice
- 🔄 **Database Migrations** - Flyway for version-controlled schema migrations

---

## 🏗️ Architecture

```
                                    ┌─────────────────────────────────────────┐
                                    │            Client Applications          │
                                    │         (Web, Mobile, Third-party)      │
                                    └────────────────────┬────────────────────┘
                                                         │
                                                         ▼
                                    ┌─────────────────────────────────────────┐
                                    │           API Gateway (:8080)           │
                                    │    (Routing, Auth, Rate Limiting)       │
                                    └────────────────────┬────────────────────┘
                                                         │
                            ┌────────────────────────────┼────────────────────────────┐
                            │                            │                            │
                            ▼                            ▼                            ▼
              ┌──────────────────────┐    ┌──────────────────────┐    ┌──────────────────────┐
              │   User Service       │    │  Product Service     │    │  Shopping Cart       │
              │      (:8081)         │    │      (:8082)         │    │   Service (:8083)    │
              │   ┌────────────┐     │    │   ┌────────────┐     │    │   ┌────────────┐     │
              │   │ PostgreSQL │     │    │   │ PostgreSQL │     │    │   │ PostgreSQL │     │
              │   │  user_db   │     │    │   │ catalog_db │     │    │   │  cart_db   │     │
              │   └────────────┘     │    │   └────────────┘     │    │   └────────────┘     │
              └──────────────────────┘    └──────────────────────┘    └──────────────────────┘
                            │                            │                            │
                            └────────────────────────────┼────────────────────────────┘
                                                         │
                                    ┌────────────────────┴────────────────────┐
                                    │                                         │
                                    ▼                                         ▼
              ┌──────────────────────────────────┐          ┌──────────────────────────────────┐
              │        Order Service             │          │       Payment Service            │
              │           (:8084)                │          │           (:8085)                │
              │     ┌────────────────┐           │          │     ┌────────────────┐           │
              │     │   PostgreSQL   │           │          │     │   PostgreSQL   │           │
              │     │   order_db     │           │          │     │   payment_db   │           │
              │     └────────────────┘           │          │     └────────────────┘           │
              └──────────────────────────────────┘          └──────────────────────────────────┘
                            │                                         │
                            └─────────────────┬───────────────────────┘
                                              │
                                              ▼
                            ┌──────────────────────────────────────────┐
                            │           Apache Kafka                   │
                            │     (Event Streaming Platform)           │
                            └────────────────────┬─────────────────────┘
                                                 │
                                                 ▼
                            ┌──────────────────────────────────────────┐
                            │       Notification Service (:8086)       │
                            │        (Email, SMS, Push)                │
                            │     ┌──────────────────────┐             │
                            │     │     PostgreSQL       │             │
                            │     │   notification_db    │             │
                            │     └──────────────────────┘             │
                            └──────────────────────────────────────────┘


    ┌───────────────────────────────────────────────────────────────────────────────────────┐
    │                              Supporting Infrastructure                                │
    │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐              │
    │  │   Eureka     │  │    Redis     │  │  Prometheus  │  │   Grafana    │              │
    │  │  (:8761)     │  │   (:6379)    │  │   (:9090)    │  │   (:3000)    │              │
    │  │  Discovery   │  │   Caching    │  │   Metrics    │  │  Dashboards  │              │
    │  └──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘              │
    │  ┌──────────────┐  ┌──────────────┐                                                  │
    │  │  Zookeeper   │  │  PGAdmin     │                                                  │
    │  │  (:2181)     │  │   (:9999)    │                                                  │
    │  │  Kafka Coord │  │   DB Admin   │                                                  │
    │  └──────────────┘  └──────────────┘                                                  │
    └───────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 🚀 Quick Start

### Prerequisites

Ensure you have the following installed on your system:

| Tool | Minimum Version | Purpose |
|------|-----------------|---------|
| **Java** | 17+ | Runtime environment |
| **Maven** | 3.8+ | Build automation |
| **Docker** | 20.10+ | Containerization |
| **Docker Compose** | 2.0+ | Multi-container orchestration |

### 1. Clone the Repository

```bash
git clone https://github.com/DAMMAK/buynest.git
cd buynest
```

### 2. Configure Environment Variables

Copy the example environment file and update the values:

```bash
cp .env.example .env
```

Update the `.env` file with your configuration:

```env
# Database Configuration
POSTGRES_DB=ecommerce_main
POSTGRES_USER=postgres
POSTGRES_PASSWORD=your_secure_password

# Redis Configuration
REDIS_PASSWORD=your_redis_password

# JWT Configuration
JWT_SECRET=your_jwt_secret_key_at_least_32_characters

# Email Configuration (for notifications)
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your_email@gmail.com
SMTP_PASSWORD=your_app_password

# Monitoring
GRAFANA_USER=admin
GRAFANA_PASSWORD=your_grafana_password
```

### 3. Build All Services

```bash
# Build all microservices
./build-all.sh

# Or build individually
cd user-service && ./mvnw clean package -DskipTests && cd ..
cd product-service && ./mvnw clean package -DskipTests && cd ..
cd shopping-cart-service && ./mvnw clean package -DskipTests && cd ..
cd order-service && ./mvnw clean package -DskipTests && cd ..
cd payment-service && ./mvnw clean package -DskipTests && cd ..
cd notification-service && ./mvnw clean package -DskipTests && cd ..
cd api-gateway && ./mvnw clean package -DskipTests && cd ..
cd eureka-server && ./mvnw clean package -DskipTests && cd ..
```

### 4. Start the Platform

```bash
# Start all services with Docker Compose
docker-compose up -d

# View logs
docker-compose logs -f

# Check service status
docker-compose ps
```

### 5. Verify Deployment

| Service | URL | Description |
|---------|-----|-------------|
| **API Gateway** | http://localhost:8080 | Main entry point |
| **Eureka Dashboard** | http://localhost:8761 | Service registry |
| **PGAdmin** | http://localhost:9999 | Database management |
| **Prometheus** | http://localhost:9090 | Metrics collection |
| **Grafana** | http://localhost:3000 | Monitoring dashboards |

---

## 🔧 Microservices

### 🔐 User Service (Port: 8081)

Handles user authentication, authorization, and profile management.

**Key Features:**
- User registration and login with JWT authentication
- Password encryption with BCrypt
- Role-based access control (RBAC)
- Profile management
- Email verification

**Tech Stack:**
- Spring Security
- Spring Data JPA
- PostgreSQL
- Flyway Migrations
- Kafka Producer

**API Endpoints:**
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/register` | User registration |
| POST | `/api/v1/auth/login` | User authentication |
| GET | `/api/v1/users/me` | Get current user profile |
| PUT | `/api/v1/users/me` | Update user profile |

---

### 📦 Product Service (Port: 8082)

Manages the product catalog, categories, and inventory.

**Key Features:**
- Product CRUD operations
- Category management
- Inventory tracking
- Product search and filtering
- Image management

**Tech Stack:**
- Spring Data JPA
- PostgreSQL
- Kafka Producer/Consumer

**API Endpoints:**
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/products` | List all products |
| GET | `/api/v1/products/{id}` | Get product details |
| POST | `/api/v1/products` | Create new product |
| PUT | `/api/v1/products/{id}` | Update product |
| DELETE | `/api/v1/products/{id}` | Delete product |
| GET | `/api/v1/categories` | List categories |

---

### 🛒 Shopping Cart Service (Port: 8083)

Manages user shopping carts with Redis caching for performance.

**Key Features:**
- Add/remove items from cart
- Update item quantities
- Cart persistence
- Redis caching for fast access
- Cart expiration handling

**Tech Stack:**
- Spring Data JPA
- Spring Data Redis
- PostgreSQL
- Redis
- Scheduled Tasks

**API Endpoints:**
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/cart` | Get user's cart |
| POST | `/api/v1/cart/items` | Add item to cart |
| PUT | `/api/v1/cart/items/{id}` | Update cart item |
| DELETE | `/api/v1/cart/items/{id}` | Remove item from cart |
| DELETE | `/api/v1/cart` | Clear cart |

---

### 📋 Order Service (Port: 8084)

Handles the complete order lifecycle from creation to fulfillment.

**Key Features:**
- Order creation and management
- Order status tracking
- Order history
- Integration with payment service
- Kafka event publishing

**Tech Stack:**
- Spring Data JPA
- PostgreSQL
- Kafka Producer/Consumer
- Feign Clients

**API Endpoints:**
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/orders` | Create new order |
| GET | `/api/v1/orders` | List user orders |
| GET | `/api/v1/orders/{id}` | Get order details |
| PUT | `/api/v1/orders/{id}/status` | Update order status |
| DELETE | `/api/v1/orders/{id}` | Cancel order |

---

### 💳 Payment Service (Port: 8085)

Handles secure payment processing and transaction management.

**Key Features:**
- Payment processing
- Transaction management
- Payment method management
- Refund processing
- Webhook handling

**Tech Stack:**
- Spring Data JPA
- PostgreSQL
- Kafka Producer/Consumer
- Payment Gateway Integration

**API Endpoints:**
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/payments` | Process payment |
| GET | `/api/v1/payments/{id}` | Get payment details |
| POST | `/api/v1/payments/{id}/refund` | Process refund |
| POST | `/api/v1/webhooks/payment` | Payment webhook |

---

### 📧 Notification Service (Port: 8086)

Manages all user notifications across multiple channels.

**Key Features:**
- Email notifications
- SMS notifications (configurable)
- Push notifications
- Template management
- Notification history

**Tech Stack:**
- Spring Mail
- PostgreSQL
- Kafka Consumer
- Template Engine

**Kafka Topics Consumed:**
- `user.registered` - Welcome emails
- `order.created` - Order confirmations
- `order.shipped` - Shipping notifications
- `payment.completed` - Payment receipts

---

### 🚪 API Gateway (Port: 8080)

Central entry point for all client requests with routing and security.

**Key Features:**
- Request routing
- JWT validation
- Rate limiting
- Circuit breaker patterns
- CORS configuration
- Request/Response logging

**Tech Stack:**
- Spring Cloud Gateway
- Spring Security
- Resilience4j
- Redis (for rate limiting)

**Route Configuration:**
| Path | Target Service |
|------|----------------|
| `/api/v1/auth/**` | user-service |
| `/api/v1/users/**` | user-service |
| `/api/v1/products/**` | product-service |
| `/api/v1/cart/**` | shopping-cart-service |
| `/api/v1/orders/**` | order-service |
| `/api/v1/payments/**` | payment-service |

---

### ⚙️ Eureka Server (Port: 8761)

Service discovery and registration for all microservices.

**Features:**
- Service registration
- Health monitoring
- Load balancing support
- Service discovery dashboard

---

## 📊 Monitoring & Observability

### Prometheus Metrics

All services expose Actuator endpoints with Prometheus metrics:

```
http://localhost:{service-port}/actuator/prometheus
```

**Available Metrics:**
- JVM metrics (memory, threads, GC)
- HTTP request metrics
- Database connection pool metrics
- Custom business metrics

### Grafana Dashboards

Pre-configured dashboards available at `http://localhost:3000`:

- **Overview Dashboard** - System-wide health
- **JVM Dashboard** - Memory and GC metrics
- **HTTP Dashboard** - Request rates and latencies
- **Database Dashboard** - Connection pool stats
- **Kafka Dashboard** - Message throughput

### Health Endpoints

Each service exposes health information:

```bash
# Check service health
curl http://localhost:{port}/actuator/health

# Get detailed health info
curl http://localhost:{port}/actuator/health | jq
```

---

## 🔒 Security

### Authentication Flow

```
1. User registers/logs in via /api/v1/auth/*
2. User Service validates credentials
3. JWT token generated and returned
4. Client includes token in Authorization header
5. API Gateway validates token for protected routes
6. Request forwarded to appropriate service
```

### JWT Configuration

Tokens are configured with:
- **Algorithm:** HS256
- **Expiration:** 24 hours (configurable)
- **Payload:** User ID, roles, email

### Security Headers

The API Gateway adds security headers:
- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: DENY`
- `X-XSS-Protection: 1; mode=block`

---

## 📁 Project Structure

```
buynest/
├── api-gateway/                 # Spring Cloud Gateway service
│   ├── src/main/java/
│   │   └── dev/dammak/apigateway/
│   │       ├── config/          # Gateway & security configuration
│   │       ├── controller/      # Fallback controllers
│   │       ├── filter/          # JWT & request filters
│   │       └── util/            # Utility classes
│   └── pom.xml
│
├── eureka-server/               # Service discovery server
│   └── src/main/java/
│
├── user-service/                # User management microservice
│   ├── src/main/java/
│   │   └── dev/dammak/userservice/
│   │       ├── config/          # Security, Kafka config
│   │       ├── controller/      # REST controllers
│   │       ├── dto/             # Data transfer objects
│   │       ├── entity/          # JPA entities
│   │       ├── exception/       # Custom exceptions
│   │       ├── filter/          # Auth filters
│   │       ├── repository/      # Data repositories
│   │       ├── service/         # Business logic
│   │       └── util/            # JWT utilities
│   └── pom.xml
│
├── product-service/             # Product catalog microservice
│   ├── src/main/java/
│   │   └── dev/dammak/productservice/
│   │       ├── config/
│   │       ├── controller/
│   │       ├── dto/
│   │       ├── entity/
│   │       ├── exception/
│   │       ├── mapper/          # DTO mappers
│   │       ├── repository/
│   │       ├── service/
│   │       └── util/
│   └── pom.xml
│
├── shopping-cart-service/       # Shopping cart microservice
│   ├── src/main/java/
│   │   └── dev/dammak/shoppingcartservice/
│   │       ├── config/
│   │       ├── controller/
│   │       ├── dto/
│   │       ├── entity/
│   │       ├── exception/
│   │       ├── repository/
│   │       ├── scheduler/       # Cart cleanup jobs
│   │       ├── service/
│   │       └── util/
│   └── pom.xml
│
├── order-service/               # Order management microservice
│   ├── src/main/java/
│   │   └── dev/dammak/orderservice/
│   │       ├── client/          # Feign clients
│   │       ├── config/
│   │       ├── controller/
│   │       ├── dto/
│   │       ├── entity/
│   │       ├── enums/
│   │       ├── exception/
│   │       ├── listener/        # Kafka listeners
│   │       ├── repository/
│   │       ├── service/
│   │       └── util/
│   └── pom.xml
│
├── payment-service/             # Payment processing microservice
│   ├── src/main/java/
│   │   └── dev/dammak/paymentservice/
│   │       ├── config/
│   │       ├── controller/
│   │       ├── dto/
│   │       ├── entity/
│   │       ├── enums/
│   │       ├── exception/
│   │       ├── listener/        # Kafka listeners
│   │       ├── repository/
│   │       ├── service/
│   │       └── util/
│   └── pom.xml
│
├── notification-service/        # Notification microservice
│   ├── src/main/java/
│   │   └── dev/dammak/notificationservice/
│   │       ├── config/
│   │       ├── controller/
│   │       ├── dto/
│   │       ├── entity/
│   │       ├── enums/
│   │       ├── exception/
│   │       ├── listener/        # Kafka listeners
│   │       ├── repository/
│   │       ├── service/
│   │       └── util/
│   └── pom.xml
│
├── config-server/               # Centralized configuration (optional)
│
├── monitoring/                  # Monitoring configuration
│   ├── prometheus.yml           # Prometheus config
│   └── grafana/                 # Grafana dashboards
│
├── init-scripts/                # Database initialization
│   └── init-multiple-databases.sh
│
├── logs/                        # Application logs
├── docker-compose.yml           # Container orchestration
├── .env                         # Environment variables
└── README.md
```

---

## 🧪 Testing

### Running Unit Tests

```bash
# Run tests for all services
./mvnw test

# Run tests for a specific service
cd user-service && ./mvnw test
```

### Running Integration Tests

```bash
# Start test containers
docker-compose -f docker-compose.test.yml up -d

# Run integration tests
./mvnw verify -P integration-tests
```

### API Testing with cURL

```bash
# Register a new user
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "SecurePass123!",
    "firstName": "John",
    "lastName": "Doe"
  }'

# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "SecurePass123!"
  }'

# Get products (with token)
curl http://localhost:8080/api/v1/products \
  -H "Authorization: Bearer <your-jwt-token>"
```

---

## 🚢 Deployment

### Docker Compose (Development)

```bash
docker-compose up -d
```

### Docker Compose (Production)

```bash
docker-compose -f docker-compose.prod.yml up -d
```

### Kubernetes (Coming Soon)

Kubernetes manifests and Helm charts are planned for future releases.

---

## 🔧 Configuration

### Service Ports

| Service | Port | Description |
|---------|------|-------------|
| API Gateway | 8080 | Main entry point |
| User Service | 8081 | User management |
| Product Service | 8082 | Product catalog |
| Shopping Cart | 8083 | Cart management |
| Order Service | 8084 | Order processing |
| Payment Service | 8085 | Payment handling |
| Notification | 8086 | Notifications |
| Eureka Server | 8761 | Service discovery |
| PostgreSQL | 5432-5437 | Databases |
| Redis | 6379 | Caching |
| Kafka | 9092 | Message broker |
| Zookeeper | 2181 | Kafka coordination |
| Prometheus | 9090 | Metrics |
| Grafana | 3000 | Dashboards |
| PGAdmin | 9999 | DB admin |

### Environment Variables

See `.env` file for all configurable variables.

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Style

- Follow Java code conventions
- Use meaningful variable and method names
- Write unit tests for new features
- Update documentation as needed

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👏 Acknowledgments

- [Spring Boot](https://spring.io/projects/spring-boot) - Application framework
- [Spring Cloud](https://spring.io/projects/spring-cloud) - Microservices patterns
- [Apache Kafka](https://kafka.apache.org/) - Event streaming
- [Docker](https://www.docker.com/) - Containerization

---

<div align="center">

**Built with ❤️ by [Dammak](https://github.com/DAMMAK)**

</div>
