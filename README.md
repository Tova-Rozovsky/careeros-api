<div align="center">

<h1>⚙️ CareerOS — API</h1>
<p><strong>Spring Boot backend powering the CareerOS platform</strong></p>

<p>
  <a href="https://github.com/Careers-Os/careeros-api/stargazers"><img src="https://img.shields.io/github/stars/career-os/careeros-api?style=flat-square&color=1A56DB" alt="Stars"></a>
  <a href="https://github.com/Careers-Os/careeros-api/issues"><img src="https://img.shields.io/github/issues/career-os/careeros-api?style=flat-square&color=1A56DB" alt="Issues"></a>
  <a href="https://github.com/Careers-Os/careeros-api/blob/main/LICENSE"><img src="https://img.shields.io/badge/license-MIT-green?style=flat-square" alt="License"></a>
  <a href="CONTRIBUTING.md"><img src="https://img.shields.io/badge/contributions-welcome-brightgreen?style=flat-square" alt="Contributing"></a>
</p>

</div>

---

## 🧱 Services

| Service | Port | Responsibility |
|---------|------|----------------|
| **API Gateway** | 8080 | Routing, rate limiting, auth filter |
| **User Service** | 8081 | Auth, profiles, preferences |
| **Resume Service** | 8082 | Upload, parsing, storage, versioning |
| **Analysis Service** | 8083 | ATS scoring, recruiter simulation |
| **Interview Service** | 8084 | Question generation, session management |
| **Job Tracker Service** | 8085 | Applications CRUD, reminders |
| **Notification Service** | 8086 | Email reminders, in-app alerts |

---

## 🛠️ Tech Stack

- **Java 21** + **Spring Boot 3.2**
- **Spring Security** + **JWT** (authentication)
- **Spring Cloud Gateway** (API gateway)
- **Spring Data JPA** + **Hibernate** (ORM)
- **PostgreSQL 16** (primary database)
- **Redis** (caching, sessions)
- **RabbitMQ** (async AI job queue)
- **Apache Tika** (resume text extraction)
- **MinIO** (file storage, dev) / **AWS S3** (prod)
- **Flyway** (database migrations)
- **Docker Compose** (local dev)

---

## 🚀 Getting Started

### Prerequisites
- Java 21+
- Maven 3.9+
- Docker & Docker Compose

### Run with Docker Compose

```bash
# Clone the repository
git clone https://github.com/Careers-Os/careeros-api.git
cd careeros-api

# Copy environment file
cp .env.example .env

# Start all services (PostgreSQL, Redis, RabbitMQ, MinIO)
docker-compose up -d

# Run the application
./mvnw spring-boot:run
```

API available at: `http://localhost:8080`  
Swagger UI: `http://localhost:8080/swagger-ui.html`

### Environment Variables

```env
DB_URL=jdbc:postgresql://localhost:5432/careeros
DB_USERNAME=careeros
DB_PASSWORD=careeros
REDIS_HOST=localhost
RABBITMQ_HOST=localhost
MINIO_URL=http://localhost:9000
JWT_SECRET=your-secret-key
OPENAI_API_KEY=your-openai-key
```

---

## 📁 Project Structure

```
careeros-api/
├── gateway/                # Spring Cloud Gateway
├── user-service/           # Auth + profiles
├── resume-service/         # Resume management
├── analysis-service/       # ATS + AI analysis
├── interview-service/      # Interview sessions
├── job-tracker-service/    # Application tracking
├── notification-service/   # Emails + alerts
├── shared/                 # Shared DTOs, utils
└── docker-compose.yml      # Local dev environment
```

---

## 📡 Key API Endpoints

```
POST   /api/auth/register
POST   /api/auth/login
POST   /api/resumes/upload
POST   /api/resumes/{id}/analyze
GET    /api/resumes/{id}/analysis
POST   /api/interviews/sessions
POST   /api/interviews/sessions/{id}/answer
GET    /api/jobs/applications
POST   /api/jobs/applications
```

📖 Full API docs → [Swagger UI](http://localhost:8080/swagger-ui.html) when running locally

---

## 🤝 Contributing

Good first issues:

| Issue | Label | Difficulty |
|-------|-------|-----------|
| User profile CRUD API | `backend` `good-first-issue` | Beginner |
| Resume upload endpoint | `backend` | Intermediate |
| JWT refresh token flow | `backend` | Intermediate |

👉 See [CONTRIBUTING.md](CONTRIBUTING.md)

---

## 📄 License

MIT License — see [LICENSE](LICENSE)
