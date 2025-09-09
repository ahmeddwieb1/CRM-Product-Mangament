# elmorshedy

A CRM-like application for managing leads, meetings, notes, products, and users.

## Features

*   User authentication with JWT
*   Role-based access control (Admin, User)
*   Modules for managing Leads, Meetings, Notes, and Products
*   AI-powered features
*   A web interface for interacting with the application

## Technologies Used

*   Java 17
*   Spring Boot
*   Spring Web
*   Spring Security
*   MongoDB
*   JWT
*   Maven

## Getting Started

### Prerequisites

*   Java 17
*   Maven
*   MongoDB

### Installation

1.  Clone the repository to your local machine.
2.  Navigate to the project directory:
    ```bash
    cd elmorshedy
    ```
3.  Install the dependencies:
    ```bash
    mvn install
    ```

### Running the application

You can run the application using the following command:

```bash
mvn spring-boot:run
```

The application will be available at `http://localhost:8080`.

## Configuration

The application can be configured in the `src/main/resources/application.properties` file. You will need to provide your own MongoDB connection string and JWT secret.

```properties
spring.data.mongodb.uri=<your-mongodb-uri>
spring.app.jwtSecret=<your-jwt-secret>
```

## API Endpoints

The API is documented using Swagger. You can access the Swagger UI at `http://localhost:8080/swagger-ui.html`.

Here are some of the main endpoints:

*   `POST /api/auth/signup`: Register a new user
*   `POST /api/auth/signin`: Authenticate a user and get a JWT token
*   `GET /api/lead`: Get all leads (Admin only)
*   `POST /api/lead`: Create a new lead
*   `GET /api/lead/{id}`: Get a lead by ID
*   `PUT /api/lead/{id}`: Update a lead
*   `DELETE /api/lead/{id}`: Delete a lead

... and many more for other modules like meetings, notes, and products.
