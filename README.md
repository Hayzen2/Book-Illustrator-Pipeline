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
3. **Give Permissions to Scripts**:
Before running the application, ensure Docker is running. Then execute:
```bash
chmod +x start.sh test.sh
```
4. **Start the Application**:
```bash
./start.sh
```
5. **Access the Application**:
   - Open your web browser and navigate to `http://localhost:5173` to access the frontend.
   - The backend API will be available at `http://localhost:8080`.
6. **Run Tests**:
```bash
./test.sh
```
7. **Stop the Application**:
   - To stop the application, press `Ctrl+C` in the terminal where `start.sh` is running. This will gracefully shut down all services.
8. **Clean Up**:
   - To remove all Docker containers and volumes created by this application, run:
```bash
docker-compose down -v
```
