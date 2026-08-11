# Project Plan: Book Illustrator Pipeline

**Estimated Effort:** ~16 hours
**Objective:** Deliver a resilient, resumable 5-step pipeline integrating the Gemini API, with a strong focus on architecture decisions, concurrency handling, and AI-assisted workflow proof.

---

## Phase 1: Recon & Setup (Hours 1–2)
*Goal: Understand the pipeline constraints and initialize the AI copilot workspace.*
- **Run the Reference Notebook:** Execute Google Colab steps 1–5 manually to understand Gemini JSON inputs/outputs.
- **Map to REST API:** Map notebook calls to Gemini REST API endpoints (structured JSON schema outputs).
- **Set Tech Stack:** Initialize Spring Boot backend, React frontend, and MySQL Docker setup.
- **Initialize AI Workspace:** Create `docs/plan.md`, `docs/architecture.md`, and `.ai/` directories for transcripts.
- **Baseline Commit:** Commit initial setup with environment scripts (`start.sh`, `test.sh`).
- **Architecture Design (AI Driven):** Discuss with AI to finalize the architecture decisions, database schema, pipeline state machine, and API contract.
- **Draft `docs/architecture.md`:** Document the finalized DB schema, state transitions, and file storage strategy.
- **First Entry in `DECISIONS.md`:** Document the trade-off of choosing MySQL over JSON files for concurrency safety.

## Phase 2: Backend & State Management (Hours 3–7)
*Goal: Build a robust, resumable state machine safe from duplicate API calls.*

- **Storage Decision & Schema:** Implement MySQL schema (`users`, `projects`, `pipeline_steps`). Document the MySQL vs. JSON decision in `DECISIONS.md`.
- **Login & Project Management:** Implement backend endpoints for passwordless login (email + name) and project creation (upload `.txt` or paste text). Ensure proper one-to-many relationship mapping.
- **Model Pipeline State:** Implement strict statuses (`PENDING`, `RUNNING (IN PROGRESS)`, `COMPLETED`, `FAILED`) for the 5 steps.
- **Concurrency Guard (Duplicate Guard):** Design and implement a locking/state-check mechanism to prevent double-execution if a user refreshes or double-clicks during an in-flight API call.
- **Gemini Integration:** Implement the REST client to call Gemini models.
- **Cost Discipline (Hard Limits):** Enforce caps strictly on the backend: max 2 characters, max 1 chapter before saving.
- **Context Chaining:** Implement Gemini File API or context reuse so the full book text is NOT re-sent on every step.

## Phase 3: Frontend & UX (Hours 8–11)
*Goal: Build a polished, resilient UI that accurately reflects the backend state.*

- **Identity & Routing:** Build passwordless email/name login. Secure project routes.
- **The Stepper UI:** Map backend state exactly to UI. If `STEP_3_RUNNING`, show loading on Step 3, even on hard refresh.
- **Polished Loading & Errors:** Implement skeleton loaders or progressive updates for 10-30s API calls.
- **Retry Mechanism:** Ensure errors show clearly and the "Retry" button only hits the failed step without resetting progress.
- **Local Asset Serving:** Fetch and render generated portrait and illustration images from the backend local filesystem.

## Phase 4: Testing & Documentation (Hours 12–15)
*Goal: Solidify the submission with rigorous tests and the required decision logs.*

- **DECISIONS.md - Core Decisions:** Document architecture, state management, and concurrency decisions.
- **DECISIONS.md - AI Overrides:** Highlight at least 3 places where AI output was wrong/unsafe and explain the fix.
- **DECISIONS.md - Final Question:** Answer the "If you had one more day..." question.
- **Backend Testing:** Write tests for the state machine and concurrency logic (duplicate execution behavior).
- **Frontend Testing:** Write component tests for empty, loading, and error states.
- **Test Report:** Generate and commit the final test report into `TESTING.md`.

## Phase 5: Final Polish & Audit (Hour 16)
*Goal: Ensure all hard constraints of the assessment are met before submission.*

- **Constraint Check:** Verify 2 character / 1 chapter limits are enforced server-side.
- **Git History Audit:** Ensure small, meaningful commits. Verify AI is credited in commit messages (e.g., `co-authored-by AI`).
- **Environment Audit:** Verify `.env.example` is present and real API keys are NOT committed.
- **Run Scripts:** Test `start.sh` and `test.sh` one final time to ensure they run flawlessly.