# Job Portal — Backend REST API

A production-ready Job Portal backend built with **Java 21** and **Spring Boot 3.2.5**, featuring JWT authentication, role-based access control, AWS S3 resume uploads, async email notifications, and full Docker support.

---

## 🔗 Links

- **GitHub:** https://github.com/kunwaruttkarsh/JobPortal
- **Author:** Kunwar Uttkarsh Singh

---

## 🛠️ Tech Stack

| Category | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.2.5 |
| Security | Spring Security + JWT (jjwt 0.12.6) |
| Database | MySQL 8.0 |
| ORM | Spring Data JPA + Hibernate |
| File Storage | AWS S3 (presigned URLs) |
| Email | JavaMailSender + Gmail SMTP |
| Testing | JUnit 5 + Mockito + MockMvc |
| Containerization | Docker + docker-compose |

---

## 🏗️ Architecture

```
Client (Postman / Frontend)
         │
         ▼
    HTTP Request
         │
         ▼
  JwtFilter (validates token)
         │
         ▼
  SecurityConfig (checks route access)
         │
         ▼
  Controller (receives request)
         │
         ▼
  Service (business logic)
         │
         ▼
  Repository (database queries)
         │
         ▼
    MySQL Database
```

---

## 👥 User Roles

| Role | Permissions |
|---|---|
| **ADMIN** | Full access to everything |
| **RECRUITER** | Post jobs, view applicants, update application status |
| **CANDIDATE** | Search jobs, apply, upload resume, track applications |

---

## 📁 Project Structure

```
com.jobportal
├── config/          ← Security, CORS, S3 configuration
├── controller/      ← REST API endpoints
├── dto/
│   ├── request/     ← Incoming request objects
│   └── response/    ← Outgoing response objects
├── entity/          ← Database entities (User, Job, Application)
├── enums/           ← Role, ApplicationStatus
├── exception/       ← Global exception handler + custom exceptions
├── repository/      ← JPA repositories
├── security/        ← JWT service, filter, UserDetailsService
└── service/         ← Business logic
    └── impl/
```

---

## 🗄️ Database Design

### Entity Relationship

```
users (1) ────────────── (many) jobs
  │                              │
  └── (1) ──── (many) applications (many) ──── (1) jobs
```

### Tables

**users**
| Column | Type | Notes |
|---|---|---|
| id | BIGINT | Primary Key |
| name | VARCHAR | Not null |
| email | VARCHAR | Unique |
| password | VARCHAR | BCrypt hashed |
| role | VARCHAR | ADMIN / RECRUITER / CANDIDATE |
| phone | VARCHAR | Optional |
| created_at | DATETIME | Auto set |

**jobs**
| Column | Type | Notes |
|---|---|---|
| id | BIGINT | Primary Key |
| title | VARCHAR | Not null |
| description | TEXT | Not null |
| company | VARCHAR | Not null |
| location | VARCHAR | Optional |
| skills | VARCHAR | Optional |
| min_salary | DOUBLE | Optional |
| max_salary | DOUBLE | Optional |
| active | BOOLEAN | Soft delete flag |
| recruiter_id | BIGINT | FK → users.id |
| posted_at | DATETIME | Auto set |

**applications**
| Column | Type | Notes |
|---|---|---|
| id | BIGINT | Primary Key |
| job_id | BIGINT | FK → jobs.id |
| candidate_id | BIGINT | FK → users.id |
| resume_url | VARCHAR | S3 key |
| status | VARCHAR | APPLIED / SHORTLISTED / REJECTED / HIRED |
| applied_at | DATETIME | Auto set |

---

## 🔐 Security

- **JWT Authentication** — Stateless, token-based auth
- **BCrypt Password Hashing** — Passwords never stored in plain text
- **Role-Based Access Control** — @PreAuthorize at method level
- **Stateless Sessions** — SessionCreationPolicy.STATELESS
- **CORS** — Configured for localhost:3000 (React frontend)

### JWT Flow

```
1. User logs in with email + password
2. Server verifies credentials
3. Server generates JWT token (valid 24 hours)
4. Client stores token, sends it with every request
5. JwtFilter validates token on each request
6. User info stored in SecurityContextHolder
```

---

## 📬 API Endpoints

### Auth

| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | /api/auth/register | Public | Register new user |
| POST | /api/auth/login | Public | Login and get JWT token |

### Jobs

| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | /api/jobs | RECRUITER | Post a new job |
| GET | /api/jobs | All | Search and browse jobs |
| GET | /api/jobs/{id} | All | Get single job |
| GET | /api/jobs/my-jobs | RECRUITER | Get recruiter's posted jobs |
| DELETE | /api/jobs/{id} | RECRUITER | Soft delete a job |

### Applications

| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | /api/applications | CANDIDATE | Apply for a job |
| GET | /api/applications/my-applications | CANDIDATE | View my applications |
| GET | /api/applications/job/{jobId} | RECRUITER | View applicants for a job |
| PUT | /api/applications/{id}/status | RECRUITER | Update application status |
| DELETE | /api/applications/{id} | CANDIDATE | Withdraw application |

### Resume

| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | /api/resume/upload | CANDIDATE | Upload PDF resume to AWS S3 |
| GET | /api/resume/url | All | Get presigned download URL |

---

## 🚀 Getting Started

### Prerequisites

- Java 21
- Maven
- MySQL 8.0
- Docker (optional)

### Run Locally

**1. Clone the repository**
```bash
git clone https://github.com/kunwaruttkarsh/JobPortal.git

```

**2. Create MySQL database**
```sql
CREATE DATABASE job_portal;
CREATE USER 'jobuser'@'localhost' IDENTIFIED BY 'jobpassword123';
GRANT ALL PRIVILEGES ON job_portal.* TO 'jobuser'@'localhost';
FLUSH PRIVILEGES;
```

**3. Configure application.properties**
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/job_portal
spring.datasource.username=
spring.datasource.password=

jwt.secret=
jwt.expiration=86400000

aws.access-key=YOUR_ACCESS_KEY
aws.secret-key=YOUR_SECRET_KEY
aws.region=ap-south-1
aws.bucket-name=your-bucket-name

spring.mail.username=YOUR_GMAIL
spring.mail.password=YOUR_APP_PASSWORD
```

**4. Run the application**
```bash
mvn spring-boot:run
```

App starts at: `http://localhost:8080`

---

### Run with Docker

```bash
docker-compose up --build
```

This starts:
- MySQL container on port 3307
- Spring Boot app on port 8080

---

## 🧪 Testing

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=S3ServiceTest
mvn test -Dtest=ResumeControllerTest
```

### Test Coverage

| Test | Type | What it tests |
|---|---|---|
| S3ServiceTest | Unit | File validation, upload, presigned URL, delete |
| ResumeControllerTest | Integration | HTTP layer, auth, role access |

---

## 📧 Email Notifications

Candidates automatically receive HTML emails when:
- ✅ Application submitted successfully
- ✅ Status updated to SHORTLISTED
- ✅ Status updated to REJECTED
- ✅ Status updated to HIRED

Emails are sent **asynchronously** using @Async so API response is instant.

---

## ☁️ AWS S3 Resume Upload

```
1. Candidate uploads PDF → POST /api/resume/upload
2. File validated (PDF only, max 5MB)
3. File uploaded to S3 under unique key
4. S3 key stored in applications table
5. Recruiter requests download → GET /api/resume/url?key=...
6. Presigned URL generated (valid 1 hour)
7. Recruiter downloads resume via presigned URL
```

---

## ⚠️ Error Handling

All errors return consistent JSON:

```json
{
  "status": 404,
  "message": "Job not found",
  "timestamp": "2025-03-11T10:30:00"
}
```

| Status Code | Meaning |
|---|---|
| 200 | Success |
| 400 | Bad Request (validation error, duplicate email etc.) |
| 401 | Unauthorized (no token or expired token) |
| 403 | Forbidden (wrong role) |
| 404 | Resource not found |
| 500 | Internal server error |

---

## 🐳 Docker Configuration

```yaml
services:
  mysql:    # MySQL 8.0 on port 3307
  app:      # Spring Boot on port 8080
```

Both services run on the same `job_portal_network` and communicate by service name.

---

## 📝 Postman Testing Guide

**1. Register as CANDIDATE**
```json
POST /api/auth/register
{
  "name": "Kunwar Uttkarsh",
  "email": "kunwar@test.com",
  "password": "123456",
  "role": "CANDIDATE"
}
```

**2. Register as RECRUITER**
```json
POST /api/auth/register
{
  "name": "HR Manager",
  "email": "hr@company.com",
  "password": "123456",
  "role": "RECRUITER"
}
```

**3. Add token to all requests**
```
Authorization: Bearer <token from login response>
```

**4. Post a job (as RECRUITER)**
```json
POST /api/jobs
{
  "title": "Java Backend Developer",
  "description": "Spring Boot developer needed",
  "company": "Tech Corp",
  "location": "Delhi",
  "skills": "Java, Spring Boot, MySQL",
  "minSalary": 600000,
  "maxSalary": 1200000
}
```

**5. Upload resume (as CANDIDATE)**
```
POST /api/resume/upload
Body: form-data → file → select PDF
```

**6. Apply for job (as CANDIDATE)**
```json
POST /api/applications
{
  "jobId": 1,
  "resumeUrl": "resumes/kunwar@test.com/abc.pdf"
}
```

---

## 📞 Contact

**Kunwar Uttkarsh Singh**
- Email: kunwaruttkarsh@gmail.com
- GitHub: github.com/kunwaruttkarsh
- LinkedIn: linkedin.com/in/kunwar-uttkarsh-singh
