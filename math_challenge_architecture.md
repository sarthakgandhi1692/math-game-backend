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
* `ANSWER_SUBMISSION` - Submit answer for current question
* `PING` - Keep-alive message

### From Server:
* `CONNECTED` - Initial connection confirmation
* `GAME_STARTED` - Game room created and started
* `QUESTION` - New question for player
* `SCORE_UPDATE` - Real-time score updates
* `GAME_ENDED` - Game completion with results
* `ERROR` - Error messages and notifications

## 🔒 Security

### WebSocket Authentication
* JWT token required in WebSocket connection URL
* `WebSocketAuthHandshakeInterceptor` validates tokens
* Token verification using Supabase JWKS
* User information stored in WebSocket session attributes

### REST API Security
* JWT verification for all REST endpoints
* Role-based access control
* Secure session management

## 🎮 Game Flow

1. **Initial Connection**
   * Client connects to WebSocket with JWT
   * Server validates token and sends `CONNECTED` message
   * Client receives user information and connection status

2. **Login & Authentication**
   * Android → Supabase → Get JWT
   * WebSocket connects with JWT
   * Server verifies token and establishes secure connection

3. **Pairing via WebSocket**
   * Client sends `JOIN_WAITING_ROOM`
   * Server pairs two users, creates room
   * Players are notified of room creation

4. **Game Start**
   * Server sends `GAME_STARTED` with opponent details
   * Server sends first `QUESTION`
   * Backend timer starts (60 seconds)
   * Question index tracking begins

5. **Answer Submission**
   * Clients send `ANSWER_SUBMISSION` messages
   * Backend validates and scores in real-time
   * Server sends `SCORE_UPDATE` to both players
   * Next question is sent automatically

6. **Game End**
   * After 60s, backend sends `GAME_ENDED`
   * Updates Supabase DB with results
   * Updates leaderboard
   * Cleans up game resources

7. **Results & Leaderboard**
   * Leaderboard updates via REST or WebSocket push
   * Players can view their performance statistics

## 🧪 Testing

### WebSocket Test Client
* HTML-based test client (`websocket-test.html`)
* Supports all WebSocket message types
* Real-time game simulation
* Debug logging and connection status

### Test Endpoints
* `/api/test/game/start` - Start test game
* `/api/test/game/answer` - Submit test answer
* `/api/test/game/leaderboard` - Test leaderboard

---

## 🔌 REST API Endpoints (Non-Realtime)

| Method | Endpoint        | Description              |
| ------ | --------------- | ------------------------ |
| POST   | `/auth/verify`  | Verifies Supabase JWT    |
| GET    | `/session/{id}` | Returns session details  |
| GET    | `/leaderboard`  | Fetch global leaderboard |

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

## ✅ Next Steps

1. Design WebSocket protocol contract
2. Build game room lifecycle and pairing logic
3. Implement real-time question dispatch & answer tracking
4. Add game session scoring engine
5. Connect Android to WS endpoint using OkHttp
6. Polish UI/UX with real-time feedback
7. Monitor performance and scalability

---
