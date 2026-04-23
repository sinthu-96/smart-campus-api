# Smart Campus – Sensor & Room Management API

A fully-featured RESTful web service built with **JAX-RS (Jersey 2.35)** and embedded **Tomcat 7**, fulfilling the 5COSC022W Client-Server Architectures coursework requirements.

---

## Table of Contents

1. [API Design Overview](#api-design-overview)
2. [Project Structure](#project-structure)
3. [Build & Run Instructions](#build--run-instructions)
4. [Sample curl Commands](#sample-curl-commands)
5. [Conceptual Report – Question Answers](#conceptual-report--question-answers)

---

## API Design Overview

| Base URL | `http://localhost:8080/api/v1` |
|---|---|
| Protocol | HTTP/1.1 |
| Data format | JSON (application/json) |
| Architecture | JAX-RS resource classes, in-memory HashMap store, Jersey 2.35 |

### Resource Hierarchy

```
/api/v1                          ← Discovery (HATEOAS root)
├── /rooms                       ← Room collection
│   ├── GET    /rooms            ← List all rooms
│   ├── POST   /rooms            ← Create a room
│   ├── GET    /rooms/{roomId}   ← Get room by ID
│   └── DELETE /rooms/{roomId}   ← Delete room (blocked if sensors present)
└── /sensors                     ← Sensor collection
    ├── GET    /sensors[?type=X] ← List / filter sensors
    ├── POST   /sensors          ← Register sensor (validates roomId)
    ├── GET    /sensors/{id}     ← Get sensor by ID
    ├── DELETE /sensors/{id}     ← Remove sensor
    └── /sensors/{id}/readings   ← Sub-resource (SensorReadingResource)
        ├── GET  /readings       ← Full reading history
        └── POST /readings       ← Append reading; updates currentValue
```

### HTTP Status Codes Used

| Code | Meaning | When used |
|---|---|---|
| 200 | OK | Successful GET |
| 201 | Created | Successful POST |
| 204 | No Content | Successful DELETE |
| 400 | Bad Request | Missing required fields |
| 403 | Forbidden | POST reading to MAINTENANCE/OFFLINE sensor |
| 404 | Not Found | Resource does not exist |
| 409 | Conflict | Delete room with sensors / duplicate ID |
| 415 | Unsupported Media Type | Wrong Content-Type (handled by JAX-RS) |
| 422 | Unprocessable Entity | Sensor references non-existent roomId |
| 500 | Internal Server Error | Unexpected runtime error (global catch-all) |

---

## Project Structure

```
smart-campus-api/
├── pom.xml
├── README.md
└── src/main/
    ├── java/com/smartcampus/
    │   ├── config/
    │   │   └── ApplicationConfig.java       # @ApplicationPath("/api/v1")
    │   ├── models/
    │   │   ├── Room.java
    │   │   ├── Sensor.java
    │   │   └── SensorReading.java
    │   ├── storage/
    │   │   └── DataStore.java               # Singleton in-memory store
    │   ├── resources/
    │   │   ├── DiscoveryResource.java       # GET /api/v1
    │   │   ├── RoomResource.java            # /rooms CRUD
    │   │   ├── SensorResource.java          # /sensors CRUD + sub-resource locator
    │   │   └── SensorReadingResource.java   # /sensors/{id}/readings
    │   ├── exceptions/
    │   │   ├── RoomNotEmptyException.java
    │   │   ├── RoomNotEmptyExceptionMapper.java          # → 409
    │   │   ├── LinkedResourceNotFoundException.java
    │   │   ├── LinkedResourceNotFoundExceptionMapper.java # → 422
    │   │   ├── SensorUnavailableException.java
    │   │   ├── SensorUnavailableExceptionMapper.java     # → 403
    │   │   └── GlobalExceptionMapper.java               # → 500 (catch-all)
    │   └── filters/
    │       └── ApiLoggingFilter.java        # Request + Response logging
    └── webapp/WEB-INF/
        └── web.xml
```

---

## Build & Run Instructions

### Prerequisites

| Tool | Version |
|---|---|
| Java JDK | 11 or higher |
| Apache Maven | 3.6+ |

### Steps

```bash
# 1. Clone / unzip the project
cd smart-campus-api

# 2. Build the project
mvn clean package

# 3. Start the embedded Tomcat server
mvn tomcat7:run
```

The server starts at **http://localhost:8080**  
The API root is **http://localhost:8080/api/v1**

To stop the server press `Ctrl+C`.

---

## Sample curl Commands

> The DataStore is pre-seeded with rooms `LIB-301`, `ENG-102` and sensors `TEMP-001`, `CO2-001`, `OCC-001`.

### 1. Discovery endpoint
```bash
curl -s http://localhost:8080/api/v1 | json_pp
```

### 2. List all rooms
```bash
curl -s http://localhost:8080/api/v1/rooms | json_pp
```

### 3. Create a new room
```bash
curl -s -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"CS-201","name":"Computer Science Lab","capacity":40}' | json_pp
```

### 4. Get room by ID
```bash
curl -s http://localhost:8080/api/v1/rooms/LIB-301 | json_pp
```

### 5. Attempt to delete a room that has sensors (expect 409 Conflict)
```bash
curl -s -X DELETE http://localhost:8080/api/v1/rooms/LIB-301 | json_pp
```

### 6. Register a new sensor with a valid roomId
```bash
curl -s -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"HUM-001","type":"Humidity","status":"ACTIVE","currentValue":55.0,"roomId":"ENG-102"}' | json_pp
```

### 7. Attempt to register a sensor with a non-existent roomId (expect 422)
```bash
curl -s -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"HUM-002","type":"Humidity","status":"ACTIVE","currentValue":60.0,"roomId":"GHOST-999"}' | json_pp
```

### 8. Filter sensors by type
```bash
curl -s "http://localhost:8080/api/v1/sensors?type=CO2" | json_pp
```

### 9. Post a reading to an active sensor (TEMP-001)
```bash
curl -s -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":23.7}' | json_pp
```

### 10. Attempt to post a reading to a MAINTENANCE sensor (expect 403)
```bash
curl -s -X POST http://localhost:8080/api/v1/sensors/OCC-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":5.0}' | json_pp
```

### 11. Get reading history for a sensor
```bash
curl -s http://localhost:8080/api/v1/sensors/TEMP-001/readings | json_pp
```

### 12. Delete a sensor then delete the now-empty room
```bash
# First remove sensors from LIB-301
curl -s -X DELETE http://localhost:8080/api/v1/sensors/TEMP-001
curl -s -X DELETE http://localhost:8080/api/v1/sensors/CO2-001
# Now delete the empty room
curl -s -X DELETE http://localhost:8080/api/v1/rooms/LIB-301
```

---

## Conceptual Report – Question Answers

---

### Part 1.1 – JAX-RS Resource Lifecycle

By default, JAX-RS creates a **new instance** of each resource class for every incoming HTTP request (request-scoped lifecycle). This is by design: it avoids shared mutable state within resource objects, reducing the risk of race conditions.

**Impact on in-memory data management:** Because resource instances are transient, any state stored as instance fields would be lost after each request. To persist data across requests, this project uses a **`DataStore` singleton** — a single object that lives for the lifetime of the application. All resource classes call `DataStore.getInstance()` to access the shared `HashMap`s.

**Thread-safety consideration:** Multiple concurrent requests can call `DataStore` simultaneously. For production, `ConcurrentHashMap` and `Collections.synchronizedList()` should be used to prevent race conditions. For this coursework the specification permits plain `HashMap` and `ArrayList`.

---

### Part 1.2 – HATEOAS

**Hypermedia As The Engine Of Application State (HATEOAS)** means the API embeds navigational links within responses, so clients can discover functionality at runtime rather than depending on static documentation.

**Benefits over static docs:** A client that receives the discovery response from `GET /api/v1` immediately knows every primary resource URL. If the API is versioned or restructured, the client re-reads the discovery endpoint rather than updating hardcoded URLs. This decouples client and server evolution, reduces onboarding friction, and makes the API self-documenting.

---

### Part 2.1 – IDs-only vs. Full Objects

Returning **full objects** in a list means one HTTP round-trip delivers all data the client needs, at the cost of larger payloads. Returning **IDs only** produces a minimal list response but forces the client to issue N follow-up `GET /rooms/{id}` calls — expensive on mobile networks or when listing hundreds of rooms. For a campus management API where room counts are manageable, returning full objects is the correct trade-off.

---

### Part 2.2 – Idempotency of DELETE

`DELETE` is **idempotent**: the server state after N identical delete calls is the same as after the first. In this implementation:
- **First call:** room exists → removed → `204 No Content`.
- **Subsequent calls:** room is already gone → `404 Not Found`.

The resource (absence of the room) is identical. Only the response code differs, which RFC 7231 explicitly permits. Idempotency is valuable because it allows clients and proxies to safely retry DELETE requests without fear of unintended side effects.

---

### Part 3.1 – @Consumes and Content-Type Mismatches

The `@Consumes(MediaType.APPLICATION_JSON)` annotation declares the only media type the method accepts. If a client sends `Content-Type: text/plain` or `application/xml`, JAX-RS rejects the request **before** the method body executes and returns **HTTP 415 Unsupported Media Type**. This is handled automatically by the Jersey runtime — no code inside the resource method is needed.

---

### Part 3.2 – @QueryParam vs. Path Param for Filtering

| Approach | Example | Verdict |
|---|---|---|
| Query parameter | `GET /sensors?type=CO2` | ✅ Preferred |
| Path parameter | `GET /sensors/type/CO2` | ❌ Not ideal |

Query parameters are **optional and composable** — `/sensors?type=CO2&status=ACTIVE` adds a second filter without changing the resource path. Path parameters encode **identity** (which specific resource), not search criteria. Using a path segment for filtering couples the URI structure to filter logic, making the API harder to extend.

---

### Part 4.1 – Sub-Resource Locator Benefits

The sub-resource locator pattern (`@Path("/{sensorId}/readings")` returning a `SensorReadingResource` instance) distributes responsibility across focused classes:

- **Single Responsibility:** `SensorResource` handles sensor CRUD; `SensorReadingResource` handles reading history. Each class is simpler and easier to test independently.
- **Scalability:** Adding new nested resources (e.g., `/sensors/{id}/alerts`) requires a new class and locator — not modifications to existing classes (Open/Closed Principle).
- **Readability:** A monolithic resource class managing every nested path would grow unwieldy in large APIs; delegation keeps each class under ~150 lines.

---

### Part 5.2 – HTTP 422 vs. 404 for Payload Reference Errors

`404 Not Found` means **the requested URI does not exist** — a routing-level failure. When a client POSTs a valid JSON body to a valid URI (e.g., `/api/v1/sensors`) but the body references a `roomId` that does not exist, the route was found successfully — the problem is inside the payload itself.

`422 Unprocessable Entity` conveys that the request was syntactically correct but **semantically invalid** — the server understands the content but cannot process it due to a business-rule violation. This is the precise scenario: the roomId reference is a semantic constraint, not a missing route. Using 422 gives API consumers a clearer, more actionable signal.

---

### Part 5.4 – Security Risks of Exposing Stack Traces

A raw Java stack trace reveals:
- **Internal class names and package structure** — attackers map the application's architecture.
- **Library names and versions** (e.g., `jersey-server-2.35`) — cross-referenced against CVE databases to find known vulnerabilities in that exact version.
- **File paths on the server** — reveals OS, deployment directory, and framework layout.
- **Logic flow** — the sequence of method calls shows what the application was doing, pointing to potential injection or logic-bypass targets.

The `GlobalExceptionMapper` eliminates this risk by logging full detail server-side (where only administrators can see it) and returning only a generic `500` message to the client.

---

### Part 5.5 – Why Filters for Cross-Cutting Concerns

Embedding `Logger.info()` in every resource method violates the **Single Responsibility Principle** — each method must both implement business logic and handle logging. JAX-RS filters solve this via the **Decorator/Chain of Responsibility** pattern:

- **Consistency:** Every request and response is logged without any risk of a developer forgetting to add a log line to a new endpoint.
- **Separation of concerns:** Resource classes contain only business logic; logging, authentication, and CORS are separate filter classes.
- **Centralised control:** Enabling or disabling logging means changing one class, not touching every resource method.
- **Testability:** Resource methods can be unit-tested in isolation without a logging framework present.

