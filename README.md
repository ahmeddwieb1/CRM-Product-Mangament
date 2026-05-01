#  elmorshedy

## 🎯 Project Summary 

**elmorshedy is a backend CRM system designed to manage leads, products, and sales workflows.**

This project is not just a typical CRUD application — it represents my backend engineering journey, where I applied and experimented with real-world concepts such as authentication, API design, AI integration, and system architecture.

> **Project Status:** This project is actively evolving and primarily focused on learning and experimentation rather than production deployment.

---

##  Table of contents
- Project Summary
- Why this project exists? 
- Features (Restructured)
- Tech Stack
- Architecture Overview (شاملة + System Diagram)
- Installation & Environment Variables (.env example مدمج)
- Running the app (Docker)
- API & Usage
- Project Structure (مبسطة)
- Learning Highlights
- Future Improvements 

---

##  Why this project exists? 

This project was built as a **hands-on way to learn backend development** by building a real system instead of isolated examples.

Instead of following tutorials, I continuously improved this system by adding new concepts as I learned them (JWT, DTOs, AI integration, Docker, etc.).

---

##  Features 

###  Authentication & Security
- JWT-based authentication
- Role-based authorization (Admin / Sales)

###  CRM Core Features
- Lead management (create, assign, update status)
- Product management with stock tracking
- Meetings and notes modules

###  AI Integration 
- **AI-Powered Features:** Integrated with Google Gemini API to generate intelligent replies
- Designed as an experimental step toward AI-assisted CRM workflows

###  Developer Experience
- Swagger UI for API exploration
- Actuator for monitoring

---

##  Tech Stack
- Java 17
- Spring Boot 3.x (Web, Security, Data MongoDB, Validation, WebFlux)
- MongoDB
- JWT (jjwt)
- Google Gemini API (via WebClient)
- Docker / Docker Compose
- Maven

---

## 🗺️ Architecture Overview + System Design Diagram 

### Layered Architecture
```
[Client] → [Controller Layer] → [Service Layer] → [Repository Layer] → [MongoDB]
                ↑                       ↑
                │                       │
           [JWT Filter]           [Gemini AI Client]
                │                       │
           [JwtUtils]              [WebClient Bean]
```

### 🔐 Security Flow
1. User signs in → `/api/auth/public/signin`
2. Server validates credentials → returns JWT
3. Client sends JWT in `Authorization: Bearer <token>`
4. `AuthTokenFilter` extracts & validates token
5. Request reaches Controller with Security Context

### 🤖 AI Integration Flow
```
User Request → LeadController → AiService.generateReply()
                                         ↓
                              GeminiConfig (WebClient)
                                         ↓
                              Google Gemini API
                                         ↓
                              AI-generated response
```

---

## 🛠️ Installation & Environment Variables

### Prerequisites
- Java 17, Maven, MongoDB (local or Docker)

### Clone & Build
```bash
git clone <repo-url>
cd elmorshedy
mvn clean package -DskipTests
```

###  Environment Variables 
Create a `.env` file in the project root:

```env
# MongoDB
MONGO_URI=mongodb://localhost:27017
MONGO_DB=elmorshedydb

# JWT (ضع قيمة طويلة ومعقدة)
JWT_SECRET=your_super_secret_key_that_is_at_least_32_characters
JWT_EXPIRATION_MS=17280000

# Google Gemini AI (مطلوب)
GEMINI_API_KEY=your_gemini_api_key_here

# Email (اختياري)
EMAIL_USER=you@gmail.com
EMAIL_PASS=your_app_password
```

>  **Important:** Never commit the `.env` file. Add it to `.gitignore`.

---

##  Running the app

### Local development
```bash
mvn spring-boot:run
# App runs on http://localhost:8080
```

### Docker 
```bash
docker build -t elmorshedy:latest .
docker-compose up -d
```

---

##  API & Usage Examples

### 1. Authentication
```bash
curl -X POST http://localhost:8080/api/auth/public/signin \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"admin","password":"admin123"}'
```
Response: `{ "accessToken": "eyJhbGciOiJ..." }`

### 2. Use JWT for authorized calls
```bash
curl -H "Authorization: Bearer eyJhbGciOiJ..." \
  http://localhost:8080/api/auth/user
```

### 3. Get all leads (Admin only)
```bash
curl -H "Authorization: Bearer <token>" \
  http://localhost:8080/api/lead
```

### Swagger UI
Open your browser at: `http://localhost:8080/swagger-ui.html`

---

##  Project Structure 

```
elmorshedy/
├── src/main/java/org/elmorshedy/
│   ├── AI/                 # Gemini AI integration
│   ├── security/           # JWT filters, config, utils
│   ├── user/               # User module (controller, service, repo)
│   ├── lead/               # Lead module
│   ├── product/            # Product module with stock
│   ├── meeting/            # Meetings module
│   ├── note/               # Notes module
│   └── email/              # Email service
├── pom.xml
├── Dockerfile
├── docker-compose.yml
└── .env (ignore)
```

---

## Learning Highlights & Engineering Decisions

- **DTO Pattern:** Used to avoid exposing MongoDB entities directly in API responses after facing issues with direct model usage.

- **Stateless JWT Authentication:** Implemented a custom filter-based authentication system using Spring Security and JWT.

- **MongoDB & Query Optimization:** Improved query performance using pagination and better query design while working with Spring Data MongoDB.

- **AI Integration (Gemini):** Built an experimental AI feature to explore how LLMs can be integrated into backend systems.

- **CI/CD & Deployment:** 
  - Used GitLab for version control and collaboration.
  - Deployed the application on Railway with automatic deployment pipelines.

- **Containerization:** Used Docker and Docker Compose to simplify local development and environment consistency.

- **Environment Management:** Used `.env` + java-dotenv to manage sensitive configurations securely.
---

## Future Improvements 

- [ ] Add Redis caching layer for frequently accessed data
- [ ] Complete n8n workflow integration
- [ ] Improve error handling and global exception management
- [ ] Add unit and integration tests (JUnit + Mockito)
- [ ] Move toward microservices architecture (future phase)
- [ ] Enhance security (refresh tokens, rate limiting, better validation)