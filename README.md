# Library Management System

[![Java](https://img.shields.io/badge/Java-21-blue?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-19-61DAFB?logo=react)](https://react.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.8-3178C6?logo=typescript)](https://www.typescriptlang.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A full-stack library management application with a Spring Boot REST API backend and a React/TypeScript frontend. Librarians can manage books and members; members can browse the catalogue. Authentication is JWT-based with role-based access control.

---

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [How It Works](#how-it-works)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Usage](#usage)
- [Project Structure](#project-structure)
- [API Endpoints](#api-endpoints)
- [Limitations](#limitations)
- [License](#license)

---

## Features

- JWT authentication with three roles: `ADMIN`, `LIBRARIAN`, `MEMBER`
- Book management — create, read, update, soft-delete, search by title/author/ISBN
- Member management — register, view, update, deactivate
- Swagger UI for interactive API exploration
- React frontend with login, sign-up, and book catalogue pages
- H2 in-memory database (zero-config local setup)
- Integration and unit tests with JUnit 5 and Mockito

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 21, Spring Boot 3.2, Spring Security, Spring Data JPA |
| Auth | JWT (JJWT 0.11.5) |
| Database | H2 (in-memory, dev) |
| API Docs | SpringDoc OpenAPI 2.3 (Swagger UI) |
| Frontend | React 19, TypeScript 5.8, Vite 6, React Router, Axios |
| Testing | JUnit 5, Mockito, Spring Boot Test |

---

## How It Works

1. A user signs in via `POST /api/v1/auth/signin` and receives a JWT.
2. Subsequent requests include the token in the `Authorization: Bearer <token>` header.
3. Spring Security validates the token and enforces role-based access on each endpoint.
4. Books and members are stored in H2 (in-memory); data resets on each restart.
5. The React frontend consumes the REST API via Axios from `http://localhost:8080`.

---

## Prerequisites

- Java 21+
- Maven 3.8+
- Node.js 18+ and npm (for the frontend)

---

## Installation

### Backend

```bash
git clone <repository-url>
cd library-management-system
cp .env.example .env        # fill in your values (see Configuration)
mvn spring-boot:run
```

The API starts on `http://localhost:8080`.  
Swagger UI: `http://localhost:8080/swagger-ui.html`  
H2 Console: `http://localhost:8080/h2-console`

### Frontend

```bash
cd frontend
npm install
npm run dev
```

The React app starts on `http://localhost:5173`.

---

## Configuration

Copy `.env.example` to `.env` and set:

| Variable | Description |
|----------|-------------|
| `LMS_JWT_SECRET` | Secret key for signing JWTs (min 256 bits, e.g. a long random string) |
| `LIBRARIAN_REG_CODE` | Registration code required to sign up as a Librarian |

Spring Boot reads these as environment variables at startup. The app will start without them but JWTs will use an empty secret, which is not suitable for any shared environment.

---

## Usage

1. **Sign up** as a `MEMBER` at `POST /api/v1/auth/signup` (no code required).
2. **Sign up** as a `LIBRARIAN` at the same endpoint, supplying `registrationCode` matching `LIBRARIAN_REG_CODE`.
3. **Sign in** at `POST /api/v1/auth/signin` to receive a JWT.
4. Use the JWT in the `Authorization: Bearer <token>` header for all subsequent requests.
5. Open the React frontend at `http://localhost:5173` to use the UI.

---

## Project Structure

```
library-management-system/
├── src/
│   ├── main/
│   │   ├── java/com/example/lms/
│   │   │   ├── config/          # Security configuration
│   │   │   ├── controller/      # REST controllers (Auth, Book, Member)
│   │   │   ├── dto/             # Request/response DTOs
│   │   │   ├── exception/       # Custom exception classes
│   │   │   ├── mapper/          # Entity ↔ DTO mappers
│   │   │   ├── model/           # JPA entities (Book, Member, User)
│   │   │   ├── repository/      # Spring Data JPA repositories
│   │   │   ├── security/jwt/    # JWT filter, utils, entry point
│   │   │   └── service/         # Business logic
│   │   └── resources/
│   │       └── application.properties
│   └── test/                    # JUnit 5 unit and integration tests
├── frontend/
│   ├── src/
│   │   ├── components/          # Reusable UI components
│   │   ├── contexts/            # Auth context
│   │   ├── pages/               # Login, Signup, Books, Home
│   │   └── services/            # Axios API client
│   └── package.json
├── library_system_api.postman_collection.json
├── .env.example
├── pom.xml
└── LICENSE
```

---

## API Endpoints

### Authentication

| Method | Path | Access | Description |
|--------|------|--------|-------------|
| POST | `/api/v1/auth/signin` | Public | Sign in, returns JWT |
| POST | `/api/v1/auth/signup` | Public | Register (MEMBER or LIBRARIAN with code) |

### Books

| Method | Path | Access | Description |
|--------|------|--------|-------------|
| GET | `/api/v1/books` | Authenticated | List all books (paginated) |
| GET | `/api/v1/books/{id}` | Authenticated | Get book by ID |
| POST | `/api/v1/books` | ADMIN, LIBRARIAN | Add a book |
| PUT | `/api/v1/books/{id}` | ADMIN, LIBRARIAN | Update a book |
| DELETE | `/api/v1/books/{id}` | ADMIN | Soft-delete a book |

### Members

| Method | Path | Access | Description |
|--------|------|--------|-------------|
| GET | `/api/v1/members` | ADMIN, LIBRARIAN | List all members |
| GET | `/api/v1/members/{id}` | ADMIN, LIBRARIAN | Get member by ID |
| POST | `/api/v1/members` | ADMIN, LIBRARIAN | Register a member |
| PUT | `/api/v1/members/{id}` | ADMIN, LIBRARIAN | Update a member |
| DELETE | `/api/v1/members/{id}` | ADMIN | Deactivate a member |

Full interactive documentation is available at `http://localhost:8080/swagger-ui.html` once the backend is running.

---

## Limitations

- **In-memory database only** — H2 data is not persisted between restarts; there is no PostgreSQL configuration included.
- **No loan tracking** — the current implementation covers books and members only; borrowing/returning books is not implemented.
- **Frontend base URL is hardcoded** to `http://localhost:8080/api/v1` in `frontend/src/services/api.ts` and must be changed manually for any non-local deployment.
- **No email verification or password reset** flow.
- **Single-node only** — no clustering, caching, or message queue integration.
- This is a local development project; it has not been deployed to production.

---

## License

Released under the [MIT License](LICENSE).
