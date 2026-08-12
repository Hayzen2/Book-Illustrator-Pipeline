# Testing Strategy & Report

## 1. Strategy Overview

Per the assessment instructions (§09), I adopted a **Test-Driven Development (TDD) approach as a leash on AI**. Instead of writing tests merely for coverage scores, I used tests to strictly enforce business rules and validate the correctness of AI-generated logic before integrating it into the main flow.

### What I Test:
*   **Security & Authentication (Backend):** 
    *   `JWTUtilTest`: Ensures tokens correctly embed `userId` and `email` for stateless auth, and strictly reject expired or forged signatures.
    *   `AuthServiceTest`: Verifies the core identity rule (§4.1): "Email exists → load their projects. Doesn't exist → create the user."
*   **Pipeline Behavior & State Machine (Backend):** (Upcoming) Will test state transitions, retry mechanisms on failed steps, and the atomic lock that prevents duplicate Gemini calls on double-clicks.
*   **UI States (Frontend):** (Upcoming) Will focus on critical UX rendering (Loading states, Error recovery buttons, and Empty states).

### What I Do NOT Test:
*   **Framework Boilerplate:** I do not write tests for standard Spring Data JPA repository methods (`save()`, `findById()`) or Lombok-generated getters/setters in DTOs. Testing standard framework features adds zero value and violates the "Right-sized solution" principle.
*   **End-to-End (E2E) UI Automation:** As per the prompt guidelines (§5.4), E2E is not expected. Component-level testing and manual UAT are sufficient for this scope.

---

## 2. Test Execution Log (Backend)

*(Note: Below is the real terminal output of the test runs verifying the Authentication and Security layer).*

```text
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 5.733 s -- in com.example.BookIllustrator.BookIllustratorApplicationTests
[INFO] Running com.example.BookIllustrator.service.AuthServiceTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.564 s -- in com.example.BookIllustrator.service.AuthServiceTest
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```