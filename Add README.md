# Smart Campus API

## Overview
A RESTful API built with JAX-RS (Jersey) for managing university campus rooms and IoT sensors. 
The system allows facilities managers to track rooms, sensors, and sensor readings across 
the Smart Campus initiative. All data is stored in-memory using ConcurrentHashMap — no database is used.

## Technology Stack
- Java 11
- JAX-RS with Jersey 2.41
- Jackson for JSON serialization
- Maven build tool
- Apache Tomcat embedded server
- In-memory storage using ConcurrentHashMap

## Project Structure
src/main/java/com/smartcampus/
├── SmartCampusApplication.java
├── model/
│   ├── Room.java
│   ├── Sensor.java
│   └── SensorReading.java
├── store/
│   └── DataStore.java
├── resource/
│   ├── DiscoveryResource.java
│   ├── RoomResource.java
│   ├── SensorResource.java
│   └── SensorReadingResource.java
├── exception/
│   ├── RoomNotEmptyException.java
│   ├── RoomNotEmptyExceptionMapper.java
│   ├── LinkedResourceNotFoundException.java
│   ├── LinkedResourceNotFoundExceptionMapper.java
│   ├── SensorUnavailableException.java
│   ├── SensorUnavailableExceptionMapper.java
│   └── GlobalExceptionMapper.java
└── filter/
└── LoggingFilter.java
## How to Build and Run

### Prerequisites
- Java JDK 11 or higher
- Apache Maven 3.6+
- NetBeans IDE or any Maven compatible IDE

### Steps to Run

1. Clone the repository:
git clone https://github.com/AlishaOvini7/smart_Campus.git

2. Open the project in NetBeans:
   - File → Open Project → select the cloned folder

3. Run the project:
   - Right-click project → Run

4. The API will be available at:
http://localhost:8080/smartCampusApi/api/v1/

5. Open your browser and go to:
 http://localhost:8080/smartCampusApi/

You will see the Smart Campus API landing page showing all available endpoints.

## API Endpoints

| Method | URL | Description |
|--------|-----|-------------|
| GET | /api/v1/ | Discovery endpoint |
| GET | /api/v1/rooms | List all rooms |
| POST | /api/v1/rooms | Create a room |
| GET | /api/v1/rooms/{roomId} | Get a specific room |
| DELETE | /api/v1/rooms/{roomId} | Delete a room |
| GET | /api/v1/sensors | List all sensors |
| GET | /api/v1/sensors?type=CO2 | Filter sensors by type |
| POST | /api/v1/sensors | Register a sensor |
| GET | /api/v1/sensors/{sensorId} | Get a specific sensor |
| DELETE | /api/v1/sensors/{sensorId} | Delete a sensor |
| GET | /api/v1/sensors/{sensorId}/readings | Get reading history |
| POST | /api/v1/sensors/{sensorId}/readings | Add a new reading |

## Sample curl Commands

### 1. Discovery
```bash
curl http://localhost:8080/smartCampusApi/api/v1/
```

### 2. Get all rooms
```bash
curl http://localhost:8080/smartCampusApi/api/v1/rooms
```

### 3. Create a room
```bash
curl -X POST http://localhost:8080/smartCampusApi/api/v1/rooms -H "Content-Type: application/json" -d "{\"id\":\"SCI-201\",\"name\":\"Science Lab\",\"capacity\":40}"
```

### 4. Get a specific room
```bash
curl http://localhost:8080/smartCampusApi/api/v1/rooms/LIB-301
```

### 5. Delete a room with sensors (returns 409)
```bash
curl -X DELETE http://localhost:8080/smartCampusApi/api/v1/rooms/LIB-301
```

### 6. Get all sensors
```bash
curl http://localhost:8080/smartCampusApi/api/v1/sensors
```

### 7. Filter sensors by type
```bash
curl "http://localhost:8080/smartCampusApi/api/v1/sensors?type=CO2"
```

### 8. Register a new sensor
```bash
curl -X POST http://localhost:8080/smartCampusApi/api/v1/sensors -H "Content-Type: application/json" -d "{\"id\":\"CO2-002\",\"type\":\"CO2\",\"status\":\"ACTIVE\",\"currentValue\":400.0,\"roomId\":\"LIB-301\"}"
```

### 9. Register sensor with invalid roomId (returns 422)
```bash
curl -X POST http://localhost:8080/smartCampusApi/api/v1/sensors -H "Content-Type: application/json" -d "{\"id\":\"BAD-001\",\"type\":\"CO2\",\"status\":\"ACTIVE\",\"currentValue\":0.0,\"roomId\":\"FAKE-999\"}"
```

### 10. Get readings for a sensor
```bash
curl http://localhost:8080/smartCampusApi/api/v1/sensors/TEMP-001/readings
```

### 11. Post a new reading
```bash
curl -X POST http://localhost:8080/smartCampusApi/api/v1/sensors/TEMP-001/readings -H "Content-Type: application/json" -d "{\"value\":25.3}"
```

### 12. Post reading to MAINTENANCE sensor (returns 403)
```bash
curl -X POST http://localhost:8080/smartCampusApi/api/v1/sensors/OCC-001/readings -H "Content-Type: application/json" -d "{\"value\":15.0}"
```

---

## Report — Answers to Questions
1.
JAX-RS, by default, uses a new instance of each resource class when a new HTTP request is received. This is referred to as request-scoped lifecycle. This implies that each time a client makes a request to /api/v1/rooms, Jersey will create an entirely new RoomResource object, process the request, and delete it.
This architectural choice directly affects the way that we handle in-memory data. Due to the fact that each request is assigned a new resource instance, we cannot store data in resource classes, it will be lost after each request. Rather, we should store all the data in a joint singleton such as our DataStore class, which is instantiated once and used throughout all of our requests.
We use ConcurrentHashMap rather than a regular HashMap to avoid the possibility of race conditions, i.e. two requests are accessing the same data at the same time. A normal HashMap is not thread-safe, in other words, two requests attempting to add a room simultaneously may cause corruption of data. ConcurrentHashMap is a safe concurrent access implementation that enables multiple threads to read at once and only lock the affected part when writing. This does not compromise on the performance by compromising data integrity.


1.2
HATEOAS is an acronym that means Hypermedia as The Engine Of Application State. It is regarded as a feature of high-quality RESTful design since it renders an API self-descriptive - the server informs the client where to proceed next, instead of the client knowing all the URLs beforehand.
In our Discovery endpoint at GET /api/v1/, we return links to all primary resources:
```json
 { 
"resources": {
 "rooms": "/api/v1/rooms",
 "sensors": "/api/v1/sensors" 
    }
 }
 ```
This has various advantages to client developers over and above to the use of a static documentation. To start with, clients are able to learn about API dynamically - all they have to know is the root URL and can navigate off of it. Second, in the event that URLs are modified in a subsequent version, this will not break those clients that follow links instead of hardcoding URLs. Third, it minimizes the necessity of developers having to look into outside documentation since the API itself directs them. A HATEOAS-based API will remain self-consistent and up to date, whereas a static documentation goes out of date very soon.

2.1
In the case of a client requesting GET /api/v1/rooms, we can choose to design in two ways, either to get the room identities only, or to get the room objects as well.
Sending back IDs only limits the size of response payload, conserving bandwidth in the network. Nevertheless, it requires the client to issue a second request to get all the details of an ID. In a case of 500 rooms, the client would have to request 501 requests in total, which is very inefficient and is referred to as the N+1 problem.
Sending full objects implies more information is sent with a single response but the client obtains all data it requires in a single request. This is much more viable in most applications, particularly when the client requires a list of rooms and their names and capacities.
In our implementation full room objects are returned in the list. This will be the more appropriate solution to our Smart Campus API since facilities managers will be able to view room details in real time without submitting hundreds of follow-up requests.

2.2
In our installation, the DELETE operation is not absolutely but is mostly idempotent in the strict sense of HTTP.
According to the HTTP specification, an idempotent operation does not affect the server state in any way regardless of the number of times it is invoked. In our case:
•	First DELETE on /api/v1/rooms/SCI-201 — room exists, gets deleted, returns 200 OK
•	Second DELETE on /api/v1/rooms/SCI-201 — room no longer exists, returns 404 Not Found
The state of the server is the same, upon both calls, the room is lost in both cases. But the response code varies in the first and second call. Strictly speaking, a perfectly idempotent DELETE would have the same response every time.
This is a real-life implementation, which is acceptable and common. What is significant is that making several calls to DELETE can never result in extra side effects:
 the room will never exist after the first successful call and subsequent calls would always confirm the absence of it.








3.1
The @Consumes(MediaType.APPLICATION_JSON) annotation informs JAX-RS that our POST endpoint only takes in requests with Content-Type being application/json.
In case a client transmits data in a different format such as text/plain or application/xml, JAX-RS will refuse the request even before reaching our method. The framework will automatically send out an HTTP 415 Unsupported Media Type response. This occurs at the framework-level, not in our code, i.e. our method is never called and we never get invalid data.
This is an effective protective system. It guarantees that Jackson, our JSON deserializer, is only presented with valid JSON by which to deserialize into our Sensor POJO. When we left the text/plain data reach Jackson, it would result in uncontrolled 500 error, and a parsing exception. We impose a contract on the API boundary by stating that it consumes we expect our clients to communicate in the format we desire, and they will be given a clear and informative error message.

3.2
There are two ways to allow clients to filter sensors by type:
•	Query parameter: GET /api/v1/sensors?type=CO2 — our approach 
•	Path parameter: GET /api/v1/sensors/type/CO2 — alternative
The query parameter method is the best method of filtering due to a number of significant reasons.
To start with, query parameters are optional. In our implementation, the client may leave out the parameter?type=CO2, in which case we will just send all the sensors. Using a path parameter method, the type is a compulsory component of the URL and one cannot access all the sensors without establishing a new endpoint.
Second, query parameters modify the retrieval of a collection whereas path parameters identify a particular resource. The URL/api/v1/sensors/CO2 suggests that CO2 is the ID of a given sensor, semantically wrong. The URL /api/v1/sensors?type=CO2 is appropriate to mean: provide me with the sensors collection, but should be filtered by type CO2.
Third, query parameters can be written with ease- it is possible to combine several filters in the query like?type=CO2&status=ACTIVE and the URL structure remains the same. In the case of path parameters, it would mean complex and nested URLs to add numerous filters.








4.1
Sub-Resource Locator pattern enables us to outsource a request of a nested resource to an entirely different class. In the implementation, a request to /api/v1/sensors/{sensorId)/readings will not go directly to the SensorResource class, but will find the sensor and then create an instance of SensorReadingResource, and give it the reins. This architectural style can offer some important advantages in big APIs.
First, it enhances separation of concerns. Every class has a single responsibility SensorResource is in charge of sensors, SensorReadingResource in charge of readings. When we lump all reasoning in a single class, we would expand to hundreds of methods and get highly irretrievable.
Second, it allows passing of contexts. The locator technique finds the particular Sensor object and gives it straight to SensorReadingResource. This is to ensure that the reading resource will always be able to look at its parent sensor without having to complete another search, resulting in a clean and efficient code.
Third, it improves testability. The smaller and focused classes are much easier to isolate and unit test. A large controller class, containing all endpoints, would be difficult and delicate to test.

5.2
In the case of POST request made by a client to create a new sensor with a roomId that is already occupied, we could return two possible status codes, 404 Not Found and 422 Unprocessable Entity.
In this case, 404 Not Found is semantically incorrect since it suggests that the URL which the client asked does not exist. The route /api/v1/sensors does exist , it is a valid and working URL. The issue is not at the endpoint, it is the information contained in the request body.
The appropriate option is 422 Unprocessable Entity since it conveys what exactly failed to work, the server knew the shape of the request, it got the correct JSON, but the content of said JSON is semantically incorrect as it points to a resource that is not there. This is not because an absent URL prevents the request from being processed but because one of the references in the payload is broken.
This difference is of great importance to client developers. A 404 would send them to believe that they are on the wrong URL. A 422 explains to them precisely where to find it, within their request body, and they can instantly realize they must supply a legit roomID.

5.4
The fact that raw Java stack traces are exposed to external API consumers is a critical cybersecurity issue. We have our GlobalExceptionMapper to avoid this, A stack trace is a highly sensitive technical data that can be used by an attacker in a variety of ways.
Internal file paths are revealed in stack traces, such as com.smartcampus.resource.RoomResource.deleteRoom(RoomResource.java:67). This informs an attacker of the specific package structure, names of classes and line numbers of our application and so it is very easy to execute targeted attacks.
The name of a library and its version can be exposed via stack traces of third-party dependencies. Known CVEs of that specific library version can be looked up and exploit unpatched security holes by an attacker.
Stack traces can be reverse-engineered to application logic. A graph of method calls shows how our application handles data internally, which may end up showing business logic which is not meant to be exposed.
Using a catch-all ExceptionMapper Throwable, we handle all unforeseen exceptions and send only a safe and generic response to the customer. The actual error details are stored in a server side only and accessible to only the authorised developers.

5.5
Handling cross-cutting concerns such as logging with JAX-RS filters is much better than adding Logger.info() lines at the end of each resource method because of a number of significant reasons.
Originally, filters apply the DRY principle (Don't Repeat Yourself). Using a filter, we implement the logging logic just once in LoggingFilter.java and it automatically applies to all the individual endpoints of the API. When we do so manually, we would have to replicate the same boilerplate code in each of the methods in RoomResource, SensorResource and SensorReadingResource.
Second, there is no exception regarding the use of filters. The manual addition of logs may also result in a developer not adding logs to an endpoint, leaving gaps in observability. There are no requests that a filter will miss since the filter is at the framework level and it intercepts all the requests prior to them reaching any resource method.
Third, filters maintain business logic clean. The logic to resource should only consist of logic in the purpose of the resource methods. Logging code should not be mixed in with business logic because it breaks the Single Responsibility Principle and complicates the methods to read and maintain.
Fourth, when we ever have to switch our logging format or even change logging frameworks, we simply have to make a single change, to the filter, instead of dozens of methods throughout the codebase.


