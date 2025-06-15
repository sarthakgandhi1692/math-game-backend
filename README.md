# 🧮 One Minute Math Challenge

A real-time multiplayer math game backend service that enables users to compete in one-minute math challenges. Players can join rooms, solve math problems, and compete against each other in real-time.

## 🚀 Tech Stack

### Backend Framework and Language
- **Kotlin** - Primary programming language
- **Spring Boot 3.2.3** - Backend framework
- **WebSocket** - Real-time communication
- **JVM 17** - Runtime environment

### Authentication & Database
- **Supabase** - Authentication and database service
  - User management
  - PostgreSQL database
  - JWT-based authentication

### Build Tools & Dependencies
- **Gradle** - Build automation tool
- **Docker** - Containerization

### Key Libraries
- **Spring Security** - Authentication and authorization
- **Spring WebSocket** - Real-time communication
- **Spring Data JPA** - Database operations
- **Kotlin Coroutines** - Asynchronous programming
- **java-jwt** - JWT token handling
- **PostgreSQL Driver** - Database connectivity

## 🏗️ Project Structure

```
src/
├── main/
│   ├── kotlin/
│   │   ├── com.mathGame.app/
│   │   │   ├── config/            # Configuration classes
│   │   │   ├── constants/         # Modular constants
│   │   │   ├── controller/        # REST & WebSocket endpoints
│   │   │   ├── model/            # Data models
│   │   │   ├── repository/       # Database repositories
│   │   │   ├── security/         # JWT & auth handling
│   │   │   ├── service/          # Business logic
│   │   │   └── websocket/        # WebSocket handlers
│   └── resources/
│       └── application.yml       # Application config
```

## 🌟 Features

- Real-time multiplayer math challenges
- User authentication via Supabase
- Player matching and game room creation
- Live scoring and result calculation
- Leaderboard system
- WebSocket-based real-time communication
- Secure JWT authentication
- Containerized deployment support

## 🚀 Getting Started

### Prerequisites
- JDK 17 or higher
- Docker (optional, for containerized deployment)
- Supabase account and project setup

### Environment Setup
1. Create a `.env` file in the root directory with the following variables:
```env
SUPABASE_URL=your_supabase_url
SUPABASE_KEY=your_supabase_key
DATABASE_URL=your_database_url
```

### Building and Running
```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun

# Run with Docker
docker build -t math-game-backend .
docker run -p 8080:8080 math-game-backend
```

## 📝 API Documentation

### WebSocket Endpoints
- `ws://localhost:8080/game` - Main game WebSocket endpoint

### WebSocket Message Types
#### From Client:
- `JOIN_WAITING_ROOM` - Request to join waiting room
- `ANSWER_SUBMISSION` - Submit answer for current question
- `PING` - Keep-alive message

#### From Server:
- `CONNECTED` - Connection confirmation
- `GAME_STARTED` - Game room creation notification
- `QUESTION` - New question delivery
- `SCORE_UPDATE` - Real-time score updates
- `GAME_ENDED` - Game completion notification
- `ERROR` - Error messages

## 🔐 Security
- JWT-based authentication
- Secure WebSocket connections
- Role-based access control
- Session management

## 📦 Deployment
The application can be deployed using Docker. A Dockerfile is provided for containerization.

## 🤝 Contributing
Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License
This project is licensed under the MIT License - see the LICENSE file for details. 