# GRADION TECHNICAL ASSESSMENT (Software Engineer Intern): Book Illustrator Pipeline
This is a fullstack web application that transforms a book's text into character portraits and illustrations using Gemini API
## Prerequisites
Before you begin, ensure you have met the following requirements:
* **Java**: JDK 21 (or newer)
* **Gemini API Key**: You need a valid Gemini API key to access the image generation services. You can obtain it from [Google AI Studio] (https://aistudio.google.com/)
* **Node.js & npm**: Node.js 18.16.0 (or newer) and npm 9.6.7 (or newer)
* **Docker**: Docker 24.0.5 (or newer) and Docker Compose 2.20.2 (or newer)

## Environment Variables
Create a `.env` file in the root directory based on the provided `.env.example`:

```env
GEMINI_API_KEY=your_actual_gemini_api_key_here

# Database Configuration (Used by Docker Compose & Spring Boot)
DB_PORT=3306
DB_ROOT_PASSWORD=rootpassword
DB_NAME=book_illustrator
DB_USER=devuser
DB_PASSWORD=devpassword
```

## Quick Start
1. **Clone the Repository**:
```bash
git clone <repository-url>
cd book-illustrator
```
2. **Set Up Environment Variables**:
   - Copy `.env.example` to `.env` and fill in your Gemini API key and database credentials.
3. **Run Docker Compose**:
   - Ensure Docker is running, then execute:
```bash
docker-compose up -d
```
4. **Give Permissions to Scripts**:
Before running the application, ensure Docker is running. Then execute:
```bash
chmod +x start.sh test.sh
```
5. **Start the Application**:
```bash
./start.sh
```
6. **Access the Application**:
   - Open your web browser and navigate to `http://localhost:5173` to access the frontend.
   - The backend API will be available at `http://localhost:8080`.
   - Database UI (phpMyAdmin): Access http://localhost:8081 to view and inspect MySQL tables, entities, and state transitions in real-time (Log in using the credentials defined in your .env file).
7. **Run Tests**:
```bash
./test.sh
```
8. **Stop the Application**:
   - To stop the application, press `Ctrl+C` in the terminal where `start.sh` is running. This will gracefully shut down all services.
9. **Clean Up**:
   - To remove all Docker containers and volumes created by this application, run:
```bash
docker-compose down -v
```
## Project Structure

The repository is divided into two distinct applications following a "Right-sized" approach to avoid over-engineering.

### Backend (Spring Boot 3.x - Java 21)
Follows a standard Layered Architecture to cleanly separate web routing, business logic, and database interactions.

```text
backend/src/main/java/com/pipeline/
├── client/         # External Gemini API RestClient implementation (targets v1beta/interactions)
├── config/         # Spring configurations (Async thread pool, CORS, RestClient setup)
├── controller/     # REST API endpoints (Includes the unified '/execute' endpoint and "Fat GET")
├── dto/            # Data Transfer Objects to separate internal Entities from JSON responses
├── entity/         # JPA Entities mapping directly to the MySQL database schema
├── repository/     # Spring Data JPA interfaces (Houses the custom @Modifying Atomic Claim-Check query)
└── service/        # Core business logic, @Async background task orchestration, and DB transactions
```

### Frontend (React + Vite)
Follows a Component-Based Architecture, heavily utilizing Custom Hooks to extract side-effects and polling logic away from UI components.

```text
frontend/src/
├── api/            # Centralized API fetch logic (domain configuration, headers)
├── components/     # Reusable, stateless UI components (Buttons, Spinners, CharacterCards)
├── context/        # React Context for lightweight global state (e.g., Auth/User Session)
├── hooks/          # Complex state & side-effects (Contains `usePipelinePolling` for the 2s interval)
├── pages/          # Smart components routing to specific views (Dashboard, PipelineView)
└── utils/          # Pure helper functions (error parsing, date formatters)
```
