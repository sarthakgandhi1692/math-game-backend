# 🧐 One Minute Math Challenge - High Level Architecture

## 🌟 Project Objective

A real-time multiplayer math challenge game where Android users can:

* Log in using credentials
* Get paired with another player
* Play a 1-minute timed quiz
* Receive scores
* View a leaderboard

---

## 🧱 Architecture Overview

```
+-------------------+            +-----------------------------+            +------------------+
|    Android App    |  <--->     | Spring Boot Backend (API)  |  <--->     |  Supabase (Auth, |
| (Jetpack Compose) |            |    - Kotlin + Gradle       |            |   DB, Storage)   |
+-------------------+            +-----------------------------+            +------------------+
        |                                     |                                       |
        | --------- Login via Supabase ------>|                                       |
        |<------ JWT & Profile Details -------|                                       |
        |                                     |<----- Uses Supabase Admin API ------>|
        |                                     |     (user data, DB access, etc.)     |
        |                                     |                                       |
        |<------ WebSocket Channel -----------|                                       |
        |------ Realtime Game Events -------->|                                       |
        |                                     |                                       |
```

### Backend Structure
```
src/
├── main/
│   ├── kotlin/
│   │   ├── com.mathGame.app/
│   │   │   ├── config/            # Configuration classes
│   │   │   ├── constants/         # Modular constants
│   │   │   ├── controller/        # REST & WebSocket endpoints
│   │   │   ├── model/            # Data models
│   │   │   │   ├── database/     # Database entities
│   │   │   │   ├── game/         # Game logic models
│   │   │   │   └── websocket/    # WebSocket messages
│   │   │   ├── repository/       # Database repositories
│   │   │   ├── security/         # JWT & auth handling
│   │   │   ├── service/          # Business logic
│   │   │   └── websocket/        # WebSocket handlers
│   └── resources/
│       ├── application.yml       # Application config
│       └── static/              # Static web resources
```

---

## 🧹 Key Components

### 🎮 Android App (Jetpack Compose)

* **Auth**: Supabase email/password login
* **WebSocket**: Realtime communication with backend during gameplay
* **Screens**:

  * Login/Register
  * Lobby/Pairing
  * Game screen (with timer, math Qs)
  * Result screen
  * Leaderboard

### 🔧 Spring Boot Backend (Kotlin + Gradle)

Handles:

* Supabase JWT verification
* Game room generation and player pairing
* WebSocket endpoint for real-time communication
* Game session management, scoring, result calculation
* Leaderboard aggregation

#### Constants Organization
The backend uses a modular constants organization:
* `ApiConstants`: API-related configurations (ports, timeouts)
* `DatabaseConstants`: Database connection and pool settings
* `PlayerConstants`: Player-specific defaults and limits
* `QuestionConstants`: Question generation parameters and operators
* `GameConstants`: Game rules and timing parameters

### 📄 Supabase

* **Auth**: User login/signup via Supabase
* **DB**: PostgreSQL for storing:

  * Users
  * Sessions
  * Questions
  * Answers
  * Scores

---

## 📋 Core Features

### ✅ 1. Two Students Logging In

* Via Supabase Auth API
* JWT used in WebSocket handshake and REST requests
* Backend verifies token using Supabase's JWKS

### ✅ 2. Joining a Game Room (via WebSocket)

* On WebSocket connect, client joins "waiting room"
* Backend pairs players and creates a game room (UUID-based)
* Players are notified of room start via WebSocket

### ✅ 3. Playing the Game (1-minute Timer & Scoring)

* Backend sends questions over WebSocket
* Timer starts on both clients
* Clients send answers via WebSocket messages
* Backend validates and scores in real-time

### ✅ 4. Final Score & Leaderboard

* Backend calculates final results after timer ends
* Sends result via WebSocket
* Updates leaderboard in Supabase
* Leaderboard fetched via REST `GET /leaderboard`

---

## 📂 Supabase DB Schema

### `users`

* `id`: UUID (PK)
* `email`: text
* `name`: text

### `game_sessions`

* `id`: UUID (PK)
* `room_id`: UUID
* `player1_id`: FK → users
* `player2_id`: FK → users
* `start_time`: timestamp
* `end_time`: timestamp
* `status`: enum \[WAITING, ACTIVE, COMPLETED]

### `questions`

* `id`: UUID (PK)
* `expression`: text (e.g., "5 + 3 \* 2")
* `correct_answer`: int

### `player_answers`

* `id`: UUID (PK)
* `session_id`: FK → game\_sessions
* `player_id`: FK → users
* `question_id`: FK → questions
* `answer`: int
* `is_correct`: boolean
* `timestamp`: timestamp

### `leaderboard`

* `user_id`: FK → users
* `total_score`: int
* `total_games`: int

---

## 🔌 WebSocket Message Types

### From Client:
* `JOIN_WAITING_ROOM` - Request to join the waiting room
* `ANSWER_SUBMISSION` - Submit answer for current question with questionId and answer
* `PING` - Keep-alive message with timestamp for latency tracking

### From Server:
* `CONNECTED` - Initial connection confirmation with userId and welcome message
* `GAME_STARTED` - Game room created with roomId, opponentId, opponentName, and startTime
* `QUESTION` - New question containing questionId, expression, questionNumber, and totalQuestions
* `SCORE_UPDATE` - Real-time score updates with yourScore and opponentScore
* `GAME_ENDED` - Game completion with final results
* `ERROR` - Error messages and notifications

## 🔒 Security

### WebSocket Authentication
* JWT token required in WebSocket connection URL
* Token verification using Supabase JWT service
* User information (userId, email, name) stored in WebSocket session attributes
* Duplicate connection handling - closes old session if user connects again

### REST API Security
* JWT verification for all protected endpoints
* Public endpoints available at `/api/public/*`
* Role-based access control via JWT claims
* Secure session management

## 🎮 Game Flow

1. **Initial Connection**
   * Client connects to WebSocket with JWT
   * Server validates token and stores user session
   * Server sends `CONNECTED` message with userId

2. **Login & Authentication**
   * Client obtains JWT via Supabase Auth
   * Token verification through `/api/auth/verify` endpoint
   * WebSocket connection established with valid JWT

3. **Pairing via WebSocket**
   * Client sends `JOIN_WAITING_ROOM`
   * Server creates/finds game room
   * When two players match:
     * Game room created with unique ID
     * Both players notified via `GAME_STARTED`
     * Initial question sent to both players

4. **Game Progress**
   * Server tracks question index per user
   * Questions sent sequentially via `QUESTION` messages
   * Each question includes:
     * Question number and total questions count
     * Math expression to solve
     * Unique questionId for answer tracking

5. **Answer Submission**
   * Client sends `ANSWER_SUBMISSION` with questionId and answer
   * Server validates answer and updates scores
   * Both players receive `SCORE_UPDATE`
   * Next question sent automatically
   * Process continues until all questions answered or time expires

6. **Game End**
   * Game can end in multiple ways:
     * All questions answered
     * Time limit reached
     * Player disconnection
   * Final scores saved to database
   * Leaderboard updated
   * Players notified via `GAME_ENDED`

7. **Error Handling**
   * Connection errors handled gracefully
   * Player disconnection triggers game end
   * Opponent notified of disconnection
   * Invalid messages return error responses
   * Duplicate connections managed automatically

## 🧪 Testing

### Test Endpoints
* `/api/test/game/start` - Creates two test players and starts a game session
* `/api/test/game/answer` - Submit test answer with userId, questionId, and answer
* `/api/test/game/leaderboard` - Retrieve current leaderboard data
* `/api/test/game/player/{userId}/answers` - Get all answers for a specific player
* `/api/test/db` - Test database connectivity and version

### Authentication Testing
* Public endpoints available for basic testing
* Protected endpoints for testing with valid JWT
* Token verification and role-based access testing

### Game Logic Testing
* Test game creation and player matching
* Answer submission and scoring verification
* Leaderboard updates and calculations
* Player disconnection handling

### WebSocket Testing
* Connection establishment with JWT
* Message type handling and validation
* Game state management
* Error scenarios and recovery
* Duplicate connection handling
* Player disconnection scenarios

---

## 🔌 REST API Endpoints (Non-Realtime)

| Method | Endpoint                    | Description                           | Access     |
| ------ | --------------------------- | ------------------------------------- | ---------- |
| POST   | `/api/auth/verify`         | Verifies JWT token                    | Public     |
| GET    | `/api/public/test`         | Public test endpoint                  | Public     |
| GET    | `/api/protected`           | Protected endpoint example            | Protected  |
| GET    | `/api/protected/details`   | Protected endpoint with user details  | Protected  |
| GET    | `/api/test/db`             | Test database connection              | Protected  |
| POST   | `/api/test/game/start`     | Start test game with two players     | Protected  |
| POST   | `/api/test/game/answer`    | Submit test answer                    | Protected  |
| GET    | `/api/test/game/leaderboard`| Get leaderboard data                | Protected  |
| GET    | `/api/test/game/player/{userId}/answers` | Get player's answers    | Protected  |

---

## 📲 Game Flow

1. **Login**

   * Android → Supabase → Get JWT
   * WebSocket connects with JWT

2. **Pairing via WebSocket**

   * Client sends `JOIN_WAITING_ROOM`
   * Server pairs two users, creates room

3. **Game Start**

   * Server sends `GAME_STARTED` and `QUESTION`
   * Timer starts client-side (with sync message)

4. **Answer Submission**

   * Clients send `ANSWER_SUBMISSION` messages
   * Backend validates and scores

5. **Game End**

   * After 60s, backend sends `GAME_ENDED`
   * Updates Supabase DB

6. **Results & Leaderboard**

   * Leaderboard updates via REST or WebSocket push

---

## ⚙️ Technologies

| Layer    | Stack                                                 |
| -------- | ----------------------------------------------------- |
| Frontend | Android, Jetpack Compose, Kotlin, Retrofit, OkHttp WS |
| Backend  | Spring Boot, Kotlin, Gradle, Spring WebSocket         |
| Auth     | Supabase Auth (JWT)                                   |
| DB       | Supabase PostgreSQL                                   |
| Realtime | WebSocket (Spring + OkHttp)                           |
| Hosting  | Supabase or custom cloud for backend                  |

---
