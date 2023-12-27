# Description

Bookify is a simple online bookstore. It allows you to browse books, add them to the cart and place an order. The application is based on the monolith architecture. 

## Features

- Register new user
- Login
- Browse books
- Import books from CSV file (only admin)
- Get book details
- Upload book cover (only admin)
- Add books to the cart
- Calculate discounts (strategy pattern as policies)
- Place an order
- Mark abandoned orders (scheduled task)
- Get order details

## Requirements

- Java 17
- Maven
- Docker

## Technologies

- Spring Web
- Spring Data JPA
- Spring Security + JWT
- Hibernate
- PostgreSQL
- Flyway - database migration
- RabbitMQ - event sourcing simulate notify service
- Swagger - API documentation
- Lombok - reduce boilerplate code
- JUnit 5 - unit tests
- Rest Assured - integration tests for API
- Testcontainers - integration tests for database and message broker

## How to run

### Docker

Navigate to the root directory of the project and run the following commands:

```shell
cd store/docker
docker-compose up -d
```

### Set environment variables

- **Application**
  - SERVER_PORT
  - ADMIN_PASSWORD
  - SWAGGER_DEV_URL
- **Dabatase** - check docker-compose.yml and variables.sh
  - BOOKIFY_DB_URL
  - BOOKIFY_DB_USERNAME
  - BOOKIFY_DB_PASSWORD
- **Security**
  - JWT_KEY
  - JWT_EXPIRATION
- **RabbitMQ** - check docker-compose.yml and variables.sh
  - RABBITMQ_HOST
  - RABBITMQ_PORT
  - RABBITMQ_USERNAME
  - RABBITMQ_PASSWORD
  - RABBITMQ_VHOST
  - RABBITMQ_QUEUE_NOTIFICATION

In variables.sh you can find all environment variables. Navigate to the root directory of the project and run the following commands:

```shell
  chmod +x variables.sh
  . ./variables.sh
```
or add them manually in your IDE and run application.

To load project dependencies run the following command:
```shell
  mvn clean install -DskipTests
```

To run tests
```shell
  mvn test
```

To run application
```shell
  mvn spring-boot:run
```

### Swagger

To run swagger navigate to environment variable **SWAGGER_DEV_URL**.

### P.S.

This is only demo. It's not production ready. Missing tests for services and controllers.
