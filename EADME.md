# Screen Test 

This assignment project is built using Spring Boot 3.5.5 and Java 17.
It includes two tasks designed to evaluate your ability to work with REST APIs, Spring Web, Spring Data JPA, H2 Database, and testing frameworks.

### Prerequisites

- Maven 3.x
- Java 17
- Any IDE

# Assignment Tasks
## Task 1: Text Replacement API

- Endpoint: GET /replace
- Controller: TextReplaceController.java

Description: Accepts a query parameter text and:

1. If length < 2: return 400 Bad Request
2. If length = 2: return 200 OK with empty body
3. If length > 2: replace first character with * and last character with $

### Example Input/Output:
- elephant => *lephan$
- home => *om$
- abc#20xyz => *bc#20xy$
- abc => *b$
- TetingCodeAssignmentProject -> *estingCodeAssignmentProjec$
- My Test Project => *y Test Projec$

### Validation Rules:

- Works with letters, numbers, and special characters.
- Handles edge cases for length < 2, = 2, and > 2.
- Tests:
  - Unit tests for:
    - Words of varying lengths
    - Edge cases (0–3 characters)
    - Mixed strings (letters, numbers, symbols)

## Task 2: Forecast API

- Endpoint: POST /forcast 
- Controller: ForcastController.java
- Request Body:
  Task 2: Forecast API
```
    {
        "addTemprature": true/false,
        "addHumidity": true/false,
        "addWindSpeed": true/false
    }
```
- Behavior:
  - Calls external API: https://api.open-meteo.com/v1/forecast?latitude=52.52&longitude=13.41&current=temperature_2m,wind_speed_10m&hourly=temperature_2m,relative_humidity_2m,wind_speed_10m
- Extracts:
  1. Maximum temperature
  2. Maximum humidity
  3. Maximum wind speed
- Stores values in the database against the date.
- Validation Rules:
  1. All request body parameters are mandatory (true/false).
  2. Missing parameters → 400 Bad Request.
  3. External API unreachable → 502 Upstream API Unreachable.
  4. Any other exception → 500 Internal Server Error.
- Error Response Format:
```
    {
      "timestamp": "2025-09-11T01:32:01.493523327",
      "status": 502,
      "error": "Upstream API Unreachable",
      "message": "Connection to the upstream is unreachable",
      "path": "/api/v1/forcast"
    }
```
- Tests:
  - Unit tests for each layer (service, repository, controller).
  - Integration tests for end-to-end flow.
  - Positive, negative, and edge test cases.


## 📌 Notes

- Follow clean code practices (naming conventions, comments where necessary).
- Ensure test coverage for both tasks.
- Database schema will be auto-created via JPA/Hibernate.