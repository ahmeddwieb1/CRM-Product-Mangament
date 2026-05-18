elmorshedy

A CRM-like application for managing leads, products, notes, and sales operations.

Features

User authentication with JWT

Role-based access control (Admin, Sales)

Modules for managing Leads, Products, Notes, and Sales

AI-powered features using Google Generative AI (for smart replies and customer interaction assistance)

Caching with Redis for performance improvement

Technologies Used

Java 17

Spring Boot (Web, Security, Data JPA)

MongoDB

Redis

JWT

Maven

Getting Started
Prerequisites

Java 17

Maven

MongoDB

Redis (optional, for caching)

Installation

Clone the repository to your local machine:

git clone <repo-link>
cd elmorshedy


Install the dependencies:

mvn install

Running the application

Run the application using:

mvn spring-boot:run


The application will be available at http://localhost:8080.

Configuration

You can configure the app in src/main/resources/application.properties.
Make sure to provide your MongoDB connection string and JWT secret.

spring.data.mongodb.uri=<your-mongodb-uri>
spring.app.jwtSecret=<your-jwt-secret>

API Endpoints

Some main endpoints:

POST /api/auth/signup – Register a new sales user (Admin only)

POST /api/auth/signin – Authenticate a user and get a JWT token

GET /api/products – Get all products

POST /api/products – Add a new product (Admin only)

PATCH /api/products/{id}/amount – Update product stock

GET /api/leads – Get all leads

POST /api/leads – Create a new lead

... plus endpoints for notes and sales management.

Future Plans

Add API documentation with Swagger

Add more modules (e.g., Meetings)

Enable CSRF protection in production

Integrate with Model Context Protocol (MCP) for advanced AI workflows
