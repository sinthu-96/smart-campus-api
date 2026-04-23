# Smart Campus REST API

A RESTful API built using **JAX-RS (Jersey)** for managing Rooms, Sensors, and Sensor Readings in a Smart Campus environment.

---

## API Design Overview

Base URL:
http://localhost:8080/api/v1

### Resources

- /rooms → Room management
- /sensors → Sensor management
- /sensors/{id}/readings → Sensor readings (sub-resource)

### Features

- RESTful architecture (stateless, resource-based)
- In-memory data storage (HashMap, ArrayList)
- Input validation
- Exception handling with proper HTTP codes
- Sub-resource locator pattern
- Request & response logging using filters

---

## Project Structure

- config → Application configuration
- models → Data models (Room, Sensor, SensorReading)
- resources → API endpoints
- storage → In-memory datastore
- exceptions → Custom exceptions + mappers
- filters → Logging filter

---

## How to Run

1. Clone the repository:
   git clone https://github.com/sinthu-96/smart-campus-api.git

2. Open the project in NetBeans or IntelliJ

3. Build the project:
   mvn clean install

4. Run the application

5. Access API at:
   http://localhost:8080/api/v1

---

## Sample cURL Commands

### Get API info
curl http://localhost:8080/api/v1

### Create Room
curl -X POST http://localhost:8080/api/v1/rooms -H "Content-Type: application/json" -d "{\"id\":\"LIB-301\",\"name\":\"Library\",\"capacity\":100}"

### Get Rooms
curl http://localhost:8080/api/v1/rooms

### Create Sensor
curl -X POST http://localhost:8080/api/v1/sensors -H "Content-Type: application/json" -d "{\"id\":\"TEMP-001\",\"type\":\"Temperature\",\"status\":\"ACTIVE\",\"roomId\":\"LIB-301\"}"

### Filter Sensors
curl "http://localhost:8080/api/v1/sensors?type=Temperature"

### Add Reading
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings -H "Content-Type: application/json" -d "{\"value\":25.5}"

---






## Report Answers

📘 FINAL REPORT — SMART CAMPUS REST API
---
### Part 1 — Service Architecture & Setup
### 1. JAX-RS Resource Lifecycle
In JAX-RS, resource classes follow a per-request lifecycle, meaning a new instance of the resource is created for each incoming HTTP request. This supports the stateless constraint of REST architecture, ensuring that request-specific data is not shared across different clients.
However, although resource instances are created per request, the system uses in-memory data structures (such as HashMap and ArrayList) to store application data. These structures are shared across all requests and therefore behave like a singleton application state.
In this implementation, a centralized DataStore class is used to manage shared data. This ensures consistency across requests while keeping resource classes stateless.
Because multiple requests may access or modify shared data simultaneously, careful handling is required to avoid race conditions and inconsistent updates. This design ensures data integrity, thread safety, and reliable API behaviour.
---
### 2. HATEOAS (Hypermedia as the Engine of Application State)
HATEOAS is a REST principle where API responses include hypermedia links that guide clients on how to navigate the system dynamically.
In this implementation, a discovery endpoint (GET /api/v1) returns API metadata along with links to primary resources such as /rooms and /sensors. This allows clients to discover available endpoints without relying entirely on static documentation.
This approach provides:
•	Loose coupling between client and server 
•	Improved flexibility, allowing APIs to evolve without breaking clients 
•	Self-descriptive responses, enhancing usability 
Therefore, HATEOAS improves adaptability and scalability in RESTful systems.
---
### Part 2 — Room Management
#### 3. Returning IDs vs Full Objects
Returning only resource IDs reduces response size and network bandwidth usage, making it efficient for large datasets.
However, returning full resource objects provides complete information in a single response, reducing the need for additional API calls and simplifying client-side logic.
In this implementation, full room objects are returned, as it improves usability and reduces round-trip requests.
Therefore:
•	ID-only → efficient but limited 
•	Full objects → more practical and user-friendly 
This design improves the overall client experience.
---
#### 4. DELETE Idempotency
The DELETE operation is an idempotent HTTP method, meaning that multiple identical requests produce the same final state.
In this implementation:
•	If a room exists, it is deleted successfully 
•	If the same DELETE request is sent again, the room no longer exists 
Although the response may differ (e.g., 404 Not Found), the system state remains unchanged.
Additionally, deletion is prevented if the room still contains sensors. In that case, a 409 Conflict is returned.
This behaviour ensures predictable, safe, and REST-compliant API operations.
---
### Part 3 — Sensor Operations & Linking
#### 5. Behaviour of @Consumes(MediaType.APPLICATION_JSON)
The @Consumes(MediaType.APPLICATION_JSON) annotation ensures that the API method accepts only JSON-formatted request bodies.
If a client sends data in a different format (such as text/plain or application/xml), the JAX-RS runtime rejects the request and returns an HTTP 415 Unsupported Media Type response.
This enforces a strict content negotiation contract, ensuring:
•	Only valid formats are processed 
•	Input consistency is maintained 
•	Invalid requests are rejected early 
This improves the robustness and reliability of the API.
---
#### 6. QueryParam vs PathParam
Using @QueryParam for filtering (e.g., /sensors?type=CO2) is more appropriate than using path parameters (e.g., /sensors/type/CO2).
This is because:
•	Path parameters identify specific resources 
•	Query parameters are used for filtering collections 
Query parameters are:
•	Optional and flexible 
•	Easily extendable (e.g., multiple filters) 
•	More aligned with RESTful design 
For example:
/sensors?type=CO2&status=ACTIVE
This approach improves scalability, readability, and maintainability.
---
### Part 4 — Deep Nesting with Sub-Resources
#### 7. Benefits of the Sub-Resource Locator Pattern
The Sub-Resource Locator pattern allows nested resources to be handled by separate dedicated classes.
In this implementation:
•	SensorResource handles sensors 
•	SensorReadingResource handles readings via /sensors/{id}/readings 
This provides:
•	Separation of concerns 
•	Cleaner and more modular code structure 
•	Improved maintainability and extensibility 
•	Better scalability for complex APIs 
Without this pattern, a single resource class would become overly complex.
Therefore, this design improves modularity and long-term maintainability.
---
### Part 5 — Advanced Error Handling, Exception Mapping & Logging
#### 8. Why HTTP 422 is More Accurate than 404
HTTP 422 Unprocessable Entity is used when a request is syntactically correct but contains invalid data.
In this implementation:
•	The endpoint exists 
•	The JSON request is valid 
•	But the provided roomId does not exist 
This means the server understands the request but cannot process it due to a semantic validation error.
In contrast, HTTP 404 indicates that the endpoint itself does not exist.
Therefore, using 422 improves:
•	Semantic accuracy 
•	Error clarity 
•	Alignment with RESTful validation principles 
---
#### 9. Security Risk of Exposing Stack Traces
Exposing raw Java stack traces to API clients is a serious security risk.
Stack traces may reveal:
•	Internal class and package structures 
•	Framework and library details 
•	File paths and line numbers 
•	Application logic 
This information can be exploited by attackers to identify vulnerabilities.
In this implementation, all unexpected errors are handled using a global exception mapper, which returns a generic HTTP 500 Internal Server Error response without exposing internal details.
This ensures secure and production-ready API behaviour.
---
#### 10. Why Use Filters for Logging
Logging is a cross-cutting concern, and in this implementation it is handled using JAX-RS filters.
A custom filter implements both:
•	ContainerRequestFilter → logs HTTP method and URI 
•	ContainerResponseFilter → logs response status 
Using filters provides:
•	Centralized logging logic 
•	Consistent behaviour across all endpoints 
•	Reduced code duplication 
•	Clean separation from business logic 
This approach improves maintainability, scalability, and system observability.

