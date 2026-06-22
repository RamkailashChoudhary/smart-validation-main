# Smart AI Semantic Validation API

A production-grade Spring Boot 3 REST API that validates field values
semantically using AI — not just by format.

Instead of asking *"is this the right format?"*, it asks
*"does this value actually make sense?"*

---

## The Problem

Classical validation passes these values without complaint:

```
fullName:            "123abc$$"
jobTitle:            "I like eating pizza"
professionalSummary: "aaaaaaaaaaaaaaaaaaa"
```

This API rejects them — because AI understands meaning, not just pattern.

---

## How It Works

Two validation layers run in sequence:

- **Layer 1 — Bean Validation** (`@NotBlank`, `@Email`, `@Size`): free, instant, format checks. Runs first. If it fails → `400 Bad Request`. AI is never called.
- **Layer 2 — AI Semantic Validation**: the entire DTO is sent to Groq (Llama 3.3 70B) in **one single API call**. If any field fails → `422 Unprocessable Entity`.

---

## Tech Stack

| Technology | Version | Purpose |
|---|---|---|
| Java | 21 | Language |
| Spring Boot | 3.3.6 | Framework |
| Spring AI | 1.0.0 | AI integration |
| Groq API | Llama 3.3 70B | Free AI model |
| Maven | 3.8+ | Build tool |

---

## Project Structure

```
src/main/java/blog/bouguern/smartvalidation/
├── SmartValidationApplication.java
├── config/
│   └── SpringAiConfig.java
├── validation/
│   ├── AiValidator.java
│   ├── AiValidatorImpl.java
│   └── AiValidationResponse.java
├── dto/
│   └── JobApplicationDTO.java
├── service/
│   └── JobApplicationService.java
├── controller/
│   └── JobApplicationController.java
└── exception/
    ├── AiValidationException.java
    ├── ValidationErrorResponse.java
    └── GlobalExceptionHandler.java
```

---

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.8+
- Free Groq API key → [https://console.groq.com](https://console.groq.com)

### Setup

Clone the repository:

```bash
git clone https://github.com/bouguern/smart-validation.git
cd smart-validation
```

Set your Groq API key as an environment variable:

```bash
# Windows
setx GROQ_API_KEY "gsk_your-key-here"

# Linux / Mac
export GROQ_API_KEY=gsk_your-key-here
```

Run the application:

```bash
mvn spring-boot:run
```

API available at: `http://localhost:8981`

Run tests:

```bash
mvn test
```

---

## API Endpoint

### `POST /api/v1/applications`

**Headers:**
```
Content-Type: application/json
```

---

### Case 1 — Valid request → `201 Created`

```json
{
  "fullName": "Mohamed Bouguern",
  "jobTitle": "Senior Software Engineer",
  "email": "mohamed.bg@example.com",
  "professionalSummary": "Experienced Java developer with 5 years building Spring Boot microservices and REST APIs in enterprise environments.",
  "linkedInUrl": "https://www.linkedin.com/in/mohamed-bouguern/"
}
```

Response:

```json
{
  "status": "success",
  "applicationId": "550e8400-e29b-41d4-a716-446655440000",
  "message": "Your application has been received"
}
```

---

### Case 2 — Semantic failure → `422 Unprocessable Entity`

```json
{
  "fullName": "aaaaabbbb1234$$",
  "jobTitle": "I like eating pizza",
  "email": "test@example.com",
  "professionalSummary": "Lorem ipsum dolor sit amet consectetur adipiscing elit sed do eiusmod tempor incididunt ut labore."
}
```

Response:

```json
{
  "timestamp": "2026-05-07T10:00:00Z",
  "status": 422,
  "error": "Semantic validation failed",
  "path": "/api/v1/applications",
  "fieldErrors": [
    { "field": "fullName",            "message": "Not a realistic human name" },
    { "field": "jobTitle",            "message": "Not a valid professional job title" },
    { "field": "professionalSummary", "message": "Looks like placeholder text" }
  ]
}
```

---

### Case 3 — Missing required fields → `400 Bad Request`

```json
{}
```

---

## All Test Cases

| # | Scenario | Body | Expected Status |
|---|---|---|---|
| 1 | Valid with LinkedIn | All valid fields + LinkedIn URL | `201 Created` |
| 2 | Valid without LinkedIn | All valid fields, no LinkedIn | `201 Created` |
| 3 | Gibberish name and title | Random characters | `422 Unprocessable` |
| 4 | Lorem ipsum summary | Placeholder text | `422 Unprocessable` |
| 5 | Invalid LinkedIn URL | Random non-LinkedIn URL | `422 Unprocessable` |
| 6 | All fields gibberish | All values nonsense | `422 Unprocessable` |
| 7 | Empty body | `{}` | `400 Bad Request` |
| 8 | Invalid email format | `"email": "not-an-email"` | `400 Bad Request` |
| 9 | Summary too short | Less than 50 characters | `400 Bad Request` |

---

## Why One AI Call for the Entire DTO

Most implementations call the AI once per field — N fields = N calls, N × latency, N × cost.

This project sends the full DTO in **one single call**. The AI sees all fields together, enabling cross-field coherence detection — something impossible when fields are validated in isolation.

| Approach | Latency | Cost |
|---|---|---|
| Per-field (naive) | N × ~400ms | N × API cost |
| **Full DTO (this project)** | **~500ms always** | **1 × API cost** |

---

## Scalable to Any DTO

The validator requires **zero changes** for new DTOs. Just pass any object:

```java
aiValidator.validate(anyDto);
```

No annotations. No new validator classes. No configuration.
The AI infers validation rules from field names and values automatically.

---

## Architecture

```
HTTP Request
     │
     ▼
JobApplicationController  (@Valid → Bean Validation)
     │
     │  @Valid fails → 400 Bad Request
     │
     ▼
JobApplicationService
     │
     ▼
AiValidatorImpl
     │  ── serialize DTO to JSON ──►  Groq API (Llama 3.3 70B)
     │  ◄─ Map<fieldName, result> ──
     │
     │  invalid fields → 422 Unprocessable Entity
     │
     ▼
201 Created
```

All exceptions are handled centrally by `GlobalExceptionHandler` (`@RestControllerAdvice`).

---


Read the full article on Medium: [Full Article](https://medium.com/@bouguern.mohamed/smart-rest-api-with-ai-semantic-validation-using-java-21-spring-boot-and-spring-ai-starter-model-25b504172396)

---

## Author

**Mohamed Bouguern** — Full-Stack Java/Angular & AI Integration Developer

[Medium](https://medium.com/@bouguern.mohamed) · [LinkedIn](https://www.linkedin.com/in/mohamed-bouguern/) · [GitHub](https://github.com/bouguern)

---

## License

MIT License — feel free to use, modify, and distribute.
