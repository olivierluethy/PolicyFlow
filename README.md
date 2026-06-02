# PolicyFlow — Swiss Insurance Broker Platform

> A production-grade platform for Swiss health-insurance brokers: manage customers,
> calculate KVG premiums, generate offers and policies, schedule appointments and keep
> a complete audit trail — with a polished dark-mode UI and a clean Quarkus backend.

PolicyFlow models the real workflow of a Swiss insurance brokerage. Premiums are
calculated using the same levers the Swiss mandatory health-insurance system (KVG) uses —
age group, cantonal cost factor, deductible (Franchise) and optional accident cover
(Unfalleinschluss). Advisors create offers, accept them to issue binding policies,
export branded PDFs, and every state change is written to an immutable audit log.

---

## Tech Stack

| Technology            | Version  | Purpose                                              |
|-----------------------|----------|------------------------------------------------------|
| Java                  | 21       | Language / runtime                                   |
| Quarkus               | 3.36     | Cloud-native backend framework                       |
| Hibernate ORM + Panache | 6.x    | Persistence with the active-record pattern           |
| PostgreSQL            | 16       | Relational database                                  |
| Flyway                | latest   | Versioned, repeatable database migrations            |
| SmallRye JWT          | latest   | Stateless authentication (RS256)                     |
| SmallRye OpenAPI      | latest   | OpenAPI spec + Swagger UI                            |
| Hibernate Validator   | latest   | Declarative request validation                       |
| OpenPDF               | 1.3.x    | PDF generation for offers and policies               |
| Micrometer            | latest   | Application metrics                                   |
| JUnit 5 + RestAssured + Mockito | — | Unit & integration testing                       |
| Tailwind CSS + Vanilla JS | CDN  | Single-file dark-mode SPA frontend                   |

---

## Architecture

The backend follows a clean, layered separation of concerns:

```
ch.policyflow
├── domain
│   ├── enums      ── Canton, Franchise, AgeGroup, status & role enums
│   └── entity     ── Panache active-record entities (+ BaseEntity)
├── dto
│   ├── request    ── validated input DTOs
│   └── response   ── output DTOs with `from(entity)` mappers
├── service        ── all business logic (the only place that mutates state)
├── resource       ── thin JAX-RS endpoints, no business logic
├── security       ── JWT issuance & current-user resolution
├── pdf            ── PDF rendering
└── exception      ── domain exceptions + global JSON error mappers
```

- **Resources are thin.** They validate, delegate to a service, and map to a DTO — no
  business logic leaks into the web layer.
- **Services own the rules.** Every mutation runs in a transaction and appends an
  `AuditLog` entry.
- **The schema is owned by Flyway**, not Hibernate (`database.generation=none`), so the
  database is deterministic and reviewable.

**Why Quarkus over Spring Boot?** Faster startup and lower memory (ideal for containers),
first-class build-time DI, a batteries-included extension model (JWT, OpenAPI, Flyway,
Dev Services) and effortless Testcontainers integration for tests.

---

## Quick Start

```bash
git clone <repo-url> policyflow && cd policyflow
docker compose up -d postgres   # start ONLY PostgreSQL (published on host port 5433)
./mvnw quarkus:dev              # run the API + UI in live-reload dev mode on port 8090
```

> Dev mode and the containerised `app` service both use host port 8090, so run one or the
> other — for live reload, start only `postgres` from Compose (as above).

Then open:

- **App UI:** http://localhost:8090
- **Swagger UI:** http://localhost:8090/swagger
- **OpenAPI spec:** http://localhost:8090/api/openapi

> Running the whole stack in containers instead? `docker-compose up -d --build` builds the
> app image (multi-stage) and runs it against the bundled PostgreSQL. The containerised app
> is published on **http://localhost:8090** (host port 8090 → container 8080), so it never
> clashes with a `quarkus:dev` instance on 8080.

---

## Default Credentials

| Username  | Password     | Role    |
|-----------|--------------|---------|
| `admin`   | `admin123`   | ADMIN   |
| `advisor` | `advisor123` | ADVISOR |

---

## API Documentation

All endpoints are under `/api` and require a `Authorization: Bearer <token>` header,
except `POST /api/auth/login`.

| Method | Path                                | Description                         | Auth |
|--------|-------------------------------------|-------------------------------------|------|
| POST   | `/api/auth/login`                   | Authenticate, returns a JWT         | —    |
| GET    | `/api/dashboard/stats`              | Portfolio KPIs + recent activity    | ✓    |
| GET    | `/api/customers?search=`            | List / search customers             | ✓    |
| GET    | `/api/customers/{id}`               | Get a customer                      | ✓    |
| POST   | `/api/customers`                    | Create a customer                   | ✓    |
| PUT    | `/api/customers/{id}`               | Update a customer                   | ✓    |
| DELETE | `/api/customers/{id}`               | Soft-delete (400 if active policy)  | ✓    |
| GET    | `/api/providers`                    | List active providers               | ✓    |
| GET    | `/api/providers/{id}`               | Get a provider                      | ✓    |
| GET    | `/api/offers?status=`               | List / filter offers                | ✓    |
| POST   | `/api/offers`                       | Create offer (premium calculated)   | ✓    |
| PUT    | `/api/offers/{id}/accept`           | Accept → issue policy               | ✓    |
| PUT    | `/api/offers/{id}/reject`           | Reject offer                        | ✓    |
| GET    | `/api/offers/{id}/pdf`              | Download offer PDF                   | ✓    |
| GET    | `/api/policies?status=`             | List / filter policies              | ✓    |
| GET    | `/api/policies/customer/{id}`       | Policies for a customer             | ✓    |
| PUT    | `/api/policies/{id}/cancel`         | Cancel a policy                     | ✓    |
| GET    | `/api/policies/{id}/pdf`            | Download policy PDF                  | ✓    |
| GET    | `/api/appointments?upcoming=`       | List (optionally upcoming only)     | ✓    |
| POST   | `/api/appointments`                 | Create an appointment               | ✓    |
| PUT    | `/api/appointments/{id}`            | Update an appointment               | ✓    |
| DELETE | `/api/appointments/{id}`            | Delete an appointment               | ✓    |
| POST   | `/api/premium/calculate`            | Calculate a single premium          | ✓    |
| POST   | `/api/premium/compare`              | Compare all six franchise levels    | ✓    |
| GET    | `/api/audit`                        | Last 50 audit entries               | ✓    |
| GET    | `/api/audit/entity/{type}/{id}`     | Audit trail for an entity           | ✓    |

---

## Premium Calculation

The monthly premium is a multiplicative model:

```
monthly = basePremium × ageFactor × cantonFactor × franchiseFactor × accidentSurcharge
```

| Lever            | Examples                                                       |
|------------------|----------------------------------------------------------------|
| Age factor       | Kind ≤18 → 0.55 · Junger 19–25 → 0.85 · Erwachsener 26+ → 1.00  |
| Canton factor    | GE 1.25 · BS 1.22 · ZH 1.18 · LU 1.02 · UR 0.95 (all 26 cantons)|
| Franchise factor | 300 → 1.00 · 1000 → 0.86 · 2500 → 0.68                          |
| Accident cover   | +8% when included                                              |

---

## Running Tests

```bash
./mvnw test      # unit tests (surefire)
./mvnw verify    # unit + integration tests (surefire + failsafe)
```

Tests requiring a database use Quarkus **Dev Services**, which start a throwaway
PostgreSQL container via Testcontainers — no manual setup needed (Docker required).

| Test class                       | Type        | Covers                                                        |
|----------------------------------|-------------|---------------------------------------------------------------|
| `PremiumCalculatorServiceTest`   | Unit        | 17 cases: age boundaries, all franchises, accident surcharge, canton factors, full calculations, rounding, ordering, error handling |
| `CustomerServiceTest`            | Integration | Create writes audit entry; delete-with-active-policy throws; soft delete; search |
| `OfferResourceIT`                | Integration | 201 with calculated premium, list, accept → policy, 400 on bad/missing input, 401 unauthenticated |

---

## Key Design Decisions

- **Flyway over auto-DDL** — the schema is explicit, versioned and reviewable; production
  databases are never mutated implicitly by an ORM.
- **Panache active-record** — domain finders (`Customer.searchByName`, `Policy.findActive`)
  live with the entity, keeping queries discoverable and services lean. A shared
  `BaseEntity` pins IDs to `IDENTITY` to match the `BIGSERIAL` columns.
- **Vanilla JS frontend** — zero build step, no framework runtime, instant load; it
  demonstrates DOM/fetch fundamentals and keeps the whole UI in one auditable file.
- **Stateless JWT auth** — RS256-signed tokens carry the role in the `groups` claim,
  enabling `@RolesAllowed` checks with no server-side session store.
- **Server-side premium calculation** — premiums are never trusted from the client; the
  API always recomputes them from the provider's base premium.
- **Global JSON error handling** — every error (validation, not-found, business rule,
  auth, unexpected) is returned as a consistent JSON envelope, never HTML.

---

## Author

_Portfolio project — author placeholder._
