# SmartBox Delivery System

A REST API for managing delivery boxes and the items loaded into them, built for the
"Assessment Task for a Java Programmer" brief.

## Tech stack

- Java 21, Spring Boot 3.3.4
- Spring Web, Spring Data JPA, Spring Validation
- PostgreSQL (local, default config) / H2 (test resources override)
- Flyway (schema + seed data migrations)
- Lombok, SLF4J
- springdoc-openapi (Swagger UI)

## Architecture

Feature-based packages / Modular Layered Architecture, sub-divided by role within each feature:

```
com.smartboxdeliverysystem
├── box/
│   ├── controller/    BoxController
│   ├── service/       BoxService (business rules live here)
│   ├── repository/    BoxRepository
│   ├── models/         Box, BoxState
│   └── dto/
│       ├── requests/   CreateBoxRequest
│       └── responses/  CreateBoxResponse, BatteryResponse
├── item/
│   ├── model/          Item
│   ├── repository/    ItemRepository
│   └── dto/
│       ├── requests/   ItemRequest
│       └── responses/  ItemResponse
└── exceptions/         Custom exceptions + global @RestControllerAdvice handler
```

`Controller -> Service -> Repository -> Entity`. Business rules live only in the
service layer; controllers are thin HTTP adapters.

## Domain model

- **Box**: `txref` (≤20 chars, unique), `weightLimit` (≤500g), `batteryCapacity` (%),
  `state` (`IDLE`, `LOADING`, `LOADED`, `DELIVERING`, `DELIVERED`, `RETURNING`)
- **Item**: `name` (letters/numbers/`-`/`_`), `weight`, `code` (upper-case
  letters/numbers/`_`)

`Item` is a **unidirectional** child of `Box` (`@ManyToOne` only, no back-reference
collection) and has no independent lifecycle or endpoint — items are only ever
created inline as part of loading a box.

## Endpoints

| Method | Path                      | Purpose                              |
|--------|---------------------------|---------------------------------------|
| POST   | `/boxes`                  | Create a box                          |
| POST   | `/boxes/{txref}/load`     | Load a box with items                 |
| GET    | `/boxes/{txref}/items`    | List items currently in a box         |
| GET    | `/boxes/available`        | List boxes available for loading      |
| GET    | `/boxes/{txref}/battery`  | Get battery level for a box           |

Swagger UI once running: `http://localhost:8080/swagger-ui.html`
Raw OpenAPI spec: `http://localhost:8080/v3/api-docs`

## Business rules (enforced in `BoxService`)

1. A box cannot be loaded with more total item weight than its `weightLimit`.
2. A box cannot be loaded if its battery is below 25%.

Both checks — plus item persistence and the box's state transition — happen inside
a single `@Transactional` service method, so a failed check leaves nothing
partially committed.

## Assumptions

Per the brief's explicit invitation to make and document assumptions:

1. **Strict scope.** Confirmed directly with the hiring contact: implement only what
   the brief specifies, no additional endpoints. Ideas considered but intentionally
   **not** built: a delivery-progression endpoint (`DELIVERING`/`DELIVERED`/
   `RETURNING` transitions) and battery drain per delivery. Those three states
   exist on the `BoxState` enum (matching the brief's stated state list) but
   nothing transitions a box into them, since no endpoint was requested to do so.
2. **No standalone item endpoint.** Items have no lifecycle outside a box, so they
   are created only via `POST /boxes/{txref}/load`, not through their own CRUD
   endpoint (aggregate-root pattern — `Box` is the aggregate root).
3. **Loading is one-shot, not additive.** A single call to `/load` supplies the
   complete set of items for that load; weight and battery are validated once,
   items are persisted, and the box transitions directly `IDLE -> LOADED`.
4. **A box can only be loaded from `IDLE`.** Attempting to load a box that is
   already `LOADING`/`LOADED`/mid-delivery is rejected (409) rather than silently
   appending items.
5. **`/boxes/available`** returns boxes in `IDLE` state with battery ≥ 25% — i.e.
   boxes that would actually pass the loading checks right now.
6. **Cameras, mentioned in the brief's introduction, are out of scope.** The
   formal field list ("A Box has: txref, weight limit, battery capacity, state")
   doesn't include a camera field, and no endpoint or rule references one.

## Running locally

### Prerequisites

- Java 21
- Maven 3.9+
- A local PostgreSQL instance

### 1. Create the database

```bash
sudo -u postgres psql -c "CREATE DATABASE boxdelivery;"
```

### 2. Configure credentials (optional)

Defaults to `postgres`/`postgres` on `localhost:5432`. Override if needed:

```bash
export DB_USERNAME=postgres
export DB_PASSWORD=yourpassword
```

### 3. Run

```bash
mvn spring-boot:run
```

Flyway runs automatically on startup, creating the schema and preloading seed data
(see below). The API is then available at `http://localhost:8080`.

### 4. Run tests

```bash
mvn test
```

Tests use `src/test/resources/application.yml`, which points at an **in-memory H2
database** (Postgres-compatibility mode). Maven puts test resources ahead of main
resources on the test classpath, so this file automatically overrides the main
Postgres configuration during `mvn test` — no local Postgres instance or network
access required to run the test suite.

## Preloaded (seed) data

Per the requirement that "required data must be preloaded into the database,"
`V3__seed_boxes.sql` inserts four boxes on first startup, each demonstrating a
different rule so every endpoint is testable immediately:

| txref                | state       | battery | notes                                              |
|-----------------------|-------------|---------|-----------------------------------------------------|
| `BOX00000000000001`   | IDLE        | 80%     | Available for loading                                |
| `BOX00000000000002`   | IDLE        | 15%     | Below battery threshold — excluded from `/available`, loading rejected |
| `BOX00000000000003`   | DELIVERING  | 90%     | Not IDLE — excluded from `/available` for a different reason |
| `BOX00000000000004`   | LOADED      | 70%     | Pre-loaded with 2 sample items, so `GET /items` has data immediately |

## Example requests

**Create a box**
```bash
curl -X POST http://localhost:8080/boxes \
  -H "Content-Type: application/json" \
  -d '{"txref": "BOX00000000000005", "weightLimit": 500, "batteryCapacity": 90}'
```

**Load a box**
```bash
curl -X POST http://localhost:8080/boxes/BOX00000000000005/load \
  -H "Content-Type: application/json" \
  -d '[{"name": "widget-1", "weight": 120, "code": "WIDGET_001"}]'
```

**Check available boxes**
```bash
curl http://localhost:8080/boxes/available
```

**Check items in a box**
```bash
curl http://localhost:8080/boxes/BOX00000000000004/items
```

**Check battery**
```bash
curl http://localhost:8080/boxes/BOX00000000000001/battery
```

## API documentation

- **Swagger UI**: `http://localhost:8080/swagger-ui.html` — interactive, generated
  directly from the running code, so it can't drift out of sync with the actual API.
- **Postman collection**: included separately, covering all 5 endpoints plus each
  documented error case (weight limit exceeded, insufficient battery, invalid
  state, duplicate txref, not found, and validation failures).
