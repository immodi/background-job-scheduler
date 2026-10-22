# Background Job Scheduler

A small REST service for scheduling and tracking background jobs, built with Spring Boot, Spring Data JPA, and an embedded SQLite database.

Jobs are created with a future `scheduledAt` time and move through a simple lifecycle. Execution/worker logic is not implemented yet — this service currently owns the job model, the CRUD API, and the state transitions.

## Features

- Create, read, update, and delete jobs over HTTP
- Validate input (`name` required, `scheduledAt` must be in the future)
- Explicit job lifecycle with guarded state transitions
- Cancel pending jobs
- Persistent storage in a local SQLite file (`jobs.db`)
- Consistent `ProblemDetail` error responses
- Unit tests for the service and web-layer tests for the controller

## Tech Stack

| Layer      | Technology                                    |
| ---------- | --------------------------------------------- |
| Language   | Java 17                                       |
| Framework  | Spring Boot 4.1 (Web MVC, Data JPA, Validation) |
| Database   | SQLite (via `sqlite-jdbc` + Hibernate community dialect) |
| Build      | Maven (wrapper included)                      |
| Tests      | JUnit 5, Mockito, Spring MockMvc              |

## Job Lifecycle

```text
PENDING ──► RUNNING ──► COMPLETED
   │           └──────► FAILED
   └────────► CANCELLED
```

- Only `PENDING` jobs can be updated or cancelled.
- Only `PENDING` jobs can start.
- Only `RUNNING` jobs can complete or fail.

Transition helpers (`startJob`, `completeJob`, `failJob`) exist in the service layer; only creation, updates, listing, deletion, and cancellation are currently exposed over HTTP.

## API

Base path: `/jobs`

| Method   | Path               | Description                    | Success |
| -------- | ------------------ | ------------------------------ | ------- |
| `POST`   | `/jobs`            | Create a job                   | `201`   |
| `GET`    | `/jobs`            | List all jobs                  | `200`   |
| `GET`    | `/jobs/{id}`       | Get a single job               | `200`   |
| `PUT`    | `/jobs/{id}`       | Replace a pending job          | `200`   |
| `DELETE` | `/jobs/{id}`       | Delete a job                   | `204`   |
| `POST`   | `/jobs/{id}/cancel`| Cancel a pending job           | `200`   |

### Create a job

```bash
curl -X POST http://localhost:8080/jobs \
  -H 'Content-Type: application/json' \
  -d '{
        "name": "send-invoices",
        "description": "Email monthly invoices",
        "scheduledAt": "2026-11-01T09:30:00"
      }'
```

```json
{
  "id": 1,
  "name": "send-invoices",
  "description": "Email monthly invoices",
  "scheduledAt": "2026-11-01T09:30:00",
  "createdAt": "2026-10-22T17:12:03.551",
  "startedAt": null,
  "completedAt": null,
  "status": "PENDING"
}
```

`name` must not be blank, and `scheduledAt` is required and must be in the future (ISO-8601 local date-time).

### Errors

Errors are returned as RFC 9457 `ProblemDetail` responses:

| Status | When                                            |
| ------ | ----------------------------------------------- |
| `400`  | Invalid request body or illegal state transition |
| `404`  | Job does not exist                              |
| `500`  | Unexpected server error                         |

## Getting Started

### Requirements

- JDK 17 or newer (only a JDK is needed; Maven is provided via the wrapper)

### Run

```bash
./mvnw spring-boot:run
```

The API is then available at `http://localhost:8080/jobs`.

The SQLite database is created on first start at `./jobs.db` (schema is managed by Hibernate `ddl-auto: update`). The file is ignored by Git.

### Test

```bash
./mvnw test
```

### Build

```bash
./mvnw clean package
java -jar target/BackgroundJobScheduler-0.0.1-SNAPSHOT.jar
```

## Project Structure

```text
src/main/java/com/projects/backgroundjobscheduler
├── controller/   REST endpoints (JobController)
├── dto/          Request payloads (CreateJobRequest, UpdateJobRequest)
├── entity/       JPA entity (Job)
├── enums/        JobStatus
├── exception/    Domain exceptions + global handler
├── repository/   Spring Data JPA repository
└── service/      Business logic and state transitions

src/main/resources/application.yaml   App + datasource config
src/test/java/...                      Service and controller tests
```

## Continuous Integration

GitHub Actions runs `./mvnw -B verify` on every push and pull request to `main`/`master`. See [`.github/workflows/ci.yml`](.github/workflows/ci.yml).

## Roadmap

- HTTP endpoints for `start`, `complete`, and `fail`
- A scheduler/worker that actually executes due jobs
- Retries and failure backoff
- Pagination and filtering by status
- Authentication and multi-tenant support
