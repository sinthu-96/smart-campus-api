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


# Part 1: Service Architecture & Setup
## Q1: In your report, explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions.
The classes of the resource in JAX-RS follow the per-request life cycle because each request to the server will create an instance of the resource class. The per-request life cycle satisfies the statelessness principle in REST and guarantees that no data related to any individual client request will be exposed to other clients. Nevertheless, although the resources classes have the per-request life cycle, application-wide data is kept in memory through the use of java data structures(HashMap and ArrayList), which are always available for any request and are therefore singleton by nature.
This design utilizes a DataStore class, which serves as  a centralized way of managing the shared data across all requests despite the stateless nature of the resource classes. As shared data might be accessed by multiple requests  at the same time, there is a specific way of guaranteeing data consistency.

## Q2: Why is the provision of “Hypermedia” (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation
HATEOAS is a principle in RESTful API architecture where the response to API calls contains hyperlinks that explain how clients can move around the application. Here, there is a discovery endpoint (GET/api/v1), and the API metadata is returned, together with links to the major entities, such as /rooms and /sensors. The purpose of the discovery operation is to enable clients to find out what other endpoints are available.
The advantages that it provides include:
•	Client and server independence
•	Flexible API development
•	Response interpretation
Conclusion:HATEOS supports agility and scalability in RESTful systems.





# Part 2: Room Management
## Q1: When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client side processing.
Sending IDs of the resource objects keeps the resources small and reduces network bandwidth usage. On the contrary, sending the complete resource objects gives us everything in one shot without needing any further requests to the API. In this case, we will be sending the complete resource objects gives us everything in one shot without needing any further requests to the API. In this case, we will be sending the complete object of the room, making it easier for the user.
Therefore, using only the ID is efficient because it reduces response size and avoids duplicate data, but it may not be sufficient because the client must make extra requests to get full details. Returning the complete object is more practical because it provides all required information in one response and makes client-side processing easier. This makes our application user-friendly

## Q2: Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.
DELETE operation is an idempotent operation since the result of multiple calls to it will end up with the same state at the endpoint.
In this case:
•	If there is any room, it can be deleted without issues.
•	Calling the DELETE operation multiple times will not locate any room.
The fact that different response codes (e.g., 404 Not Found) could be returned, the state of the system remains the same. Additionally, only rooms without any sensors are deletable, which means a conflict oocurs for rooms with sensors, and 409 status code will be returned.








# Part 3: Sensor Operations & Linking
## Q1: We explicitly use the @Consumes (MediaType.APPLICATION_JSON) annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?
“@Consumes(MediaType.APPLICATION_JSON)” ensures that the API only receives requests with a JSON request body. When the user submits data in some other format (such as text/plain or application/xml), the JAX-RS framework discards the request and sends back a 415 HTTP error message to the client.
The benefits include the following:
•	Format validation is ensured 
•	Consistency is maintained
•	Request validation is enforced
## Q2: You implemented this filtering using @QueryParam. Contrast this with an alternative design where the type is part of the URL path (e.g., /api/vl/sensors/type/CO2). Why is the query parameter approach generally considered superior for filtering and searching collections?
Using @QueryParam for filtering, such as /sensors?type=CO2, is more appropriate than using a path-based design such as /sensors/type/CO2.
This is because path parameters are mainly used to identify a specific resource, while query parameters are used to filter or search within a collection of resources. Query parameters are also optional and flexible, allowing multiple filters to be added easily, such as /sensors?type=CO2&status=ACTIVE.
Therefore, the query parameter approach is more suitable for filtering sensor collections and makes the API more scalable and maintainable.










# Part 4: Deep Nesting with Sub-Resources
## Q1: Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path (e.g., sensors/{id}/readings/{rid}) in one massive controller class?
The Sub-Resource Locator design pattern allows different resources to be encapsulated in their respective classes.
In this scenario, we have:
•	SensorResource handles sensors
•	SensorReadingResource handles readings using /sensors/{id}/readings
This has the advantages of:
•	Concern separation
•	Cleaner code structure
•	More maintainable and extendable
Otherwise, the resource will become too complex.
Therefore, this results in a better code structure and maintenance.

# Part 5: Advanced Error Handling, Exception Mapping & Logging
## Q1: Why is HTTP422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?
The HTTP code 422 Unprocessable Entity is applicable when the request is semantically wrong but syntax-wise right.
From the example presented, it means that:
•	The Endpoint exists
•	The JSON request format is correct
•	but, the room Id used does not exist
This means that even though the server understands the request, there is an issue processing it since it is semantically wrong.
As compared to HTTP 404, this case shows that the endpoint does not exist.
Therefore, the usage of the 422 HTTP makes the request:
•	Semantically right
•	A bit more accurate
•	RESTful

## Q2: From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?
Displaying stack traces of the Java application in its raw form poses a major security threat to the API consumers.
Stack trace will reveal:
•	Structure of classes and package
•	Frameworks being used
•	Path and line numbers
•	Logic behind the application
This can be exploited by bad actors in order to exploit any vulnerabilities within the system.
In this case, all the unanticipated exceptions are mapped through an exception mapper, and 500 error code is displayed.
It makes sure that the API works safely and efficiently in a production environment.

## Q3: Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting Logger.info() statements inside every single resource method?

This field is broad and incorporates various elements, whereby in this scenario, logging can be achieved using JAX-RS filters.
In particular, a customized filter is used to achieve these objectives:
•	ContainerRequestFilter – log request HTTP method and URI
•	ContainerResponseFilter – log response status
Advantages of using filters are as follows:
•	Allows for centralization of logging operations
•	Consistent functionality for all API methods
•	Reduces redundancy of code
•	Ensures segregation of business logic



