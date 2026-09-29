# Real-Time Chat Application

A full-stack real-time chat application built with **Spring Boot**, **Angular**, **PostgreSQL**, **Redis**, and **WebSockets**. Supports instant messaging, chat rooms, user authentication, and online presence tracking -- all containerized with Docker for effortless deployment.

---

## Architecture

```
                        +-------------------+
                        |     Browser       |
                        +--------+----------+
                                 |
                    HTTP / WebSocket (port 80)
                                 |
                        +--------v----------+
                        |      Nginx        |
                        |  (Reverse Proxy)  |
                        +---+----------+----+
                            |          |
              /api/* & /ws  |          |  Static files
                            |          |
                   +--------v---+  +---v-----------------+
                   | Spring Boot|  | Angular SPA         |
                   |   (8080)   |  | (index.html + JS)   |
                   +--+-----+---+  +---------------------+
                      |     |
             +--------+     +--------+
             |                       |
     +-------v--------+    +--------v--------+
     |   PostgreSQL    |    |     Redis       |
     |    (5432)       |    |    (6379)       |
     |  User data,     |    |  Sessions,      |
     |  Messages,      |    |  Online status, |
     |  Chat rooms     |    |  Pub/Sub        |
     +-----------------+    +-----------------+
```

---

## Tech Stack

| Layer      | Technology                          |
|------------|-------------------------------------|
| Frontend   | Angular 17, TypeScript, RxJS, STOMP |
| Backend    | Spring Boot 3, Java 21, Spring Security, Spring WebSocket |
| Database   | PostgreSQL 16                       |
| Cache      | Redis 7                             |
| Auth       | JWT (JSON Web Tokens)               |
| Proxy      | Nginx                               |
| Containers | Docker, Docker Compose              |

---

## Features

- **Real-time messaging** via WebSockets (STOMP over SockJS)
- **JWT authentication** with secure token refresh
- **Chat rooms** -- create, join, and manage group conversations
- **Private messaging** between users
- **Online presence** tracking powered by Redis
- **Message history** persisted in PostgreSQL
- **Typing indicators** broadcast in real time
- **Responsive UI** that works on desktop and mobile
- **Dockerized** for one-command deployment

---

## Prerequisites

### Docker (recommended)

- [Docker](https://docs.docker.com/get-docker/) >= 24.0
- [Docker Compose](https://docs.docker.com/compose/install/) >= 2.20

### Local development

- Java 21 (e.g., Eclipse Temurin)
- Maven 3.9+
- Node.js 20+
- npm 10+
- PostgreSQL 16
- Redis 7

---

## Quick Start

```bash
# Clone the repository
git clone https://github.com/your-username/realtime-chat-app.git
cd realtime-chat-app

# Copy the example environment file and fill in your secrets
cp .env.example .env

# Start all services
docker compose up -d

# View logs
docker compose logs -f

# Stop all services
docker compose down
```

The application will be available at **http://localhost**.

---

## Local Development

### Backend

```bash
cd backend

# Run with Maven (uses application-dev.yml profile)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Or build and run the jar
./mvnw clean package -DskipTests
java -jar target/*.jar --spring.profiles.active=dev
```

The API server starts at **http://localhost:8080**.

### Frontend

```bash
cd frontend

# Install dependencies
npm install

# Start the dev server with hot reload
ng serve
# or
npm start
```

The dev server starts at **http://localhost:4200** and proxies API calls to port 8080.

---

## API Endpoints

### Authentication

| Method | Endpoint             | Description              | Auth Required |
|--------|----------------------|--------------------------|---------------|
| POST   | `/api/auth/register` | Register a new user      | No            |
| POST   | `/api/auth/login`    | Login and receive JWT    | No            |
| POST   | `/api/auth/refresh`  | Refresh an expired token | Yes           |

### Users

| Method | Endpoint             | Description               | Auth Required |
|--------|----------------------|---------------------------|---------------|
| GET    | `/api/users/me`      | Get current user profile  | Yes           |
| PUT    | `/api/users/me`      | Update current user       | Yes           |
| GET    | `/api/users/online`  | List online users         | Yes           |
| GET    | `/api/users/search`  | Search users by username  | Yes           |

### Chat Rooms

| Method | Endpoint                      | Description                  | Auth Required |
|--------|-------------------------------|------------------------------|---------------|
| GET    | `/api/rooms`                  | List all rooms for the user  | Yes           |
| POST   | `/api/rooms`                  | Create a new chat room       | Yes           |
| GET    | `/api/rooms/{id}`             | Get room details             | Yes           |
| POST   | `/api/rooms/{id}/join`        | Join a chat room             | Yes           |
| POST   | `/api/rooms/{id}/leave`       | Leave a chat room            | Yes           |
| GET    | `/api/rooms/{id}/members`     | List room members            | Yes           |

### Messages

| Method | Endpoint                      | Description                  | Auth Required |
|--------|-------------------------------|------------------------------|---------------|
| GET    | `/api/rooms/{id}/messages`    | Get message history (paged)  | Yes           |
| POST   | `/api/rooms/{id}/messages`    | Send a message (REST)        | Yes           |

---

## WebSocket Events

Connect to `ws://localhost/ws` (or `ws://localhost:8080/ws` for local dev).

### Client -> Server (SEND)

| Destination                   | Payload                         | Description            |
|-------------------------------|---------------------------------|------------------------|
| `/app/chat.send/{roomId}`    | `{ content, type }`             | Send a message         |
| `/app/chat.typing/{roomId}`  | `{ typing: true/false }`        | Typing indicator       |
| `/app/chat.join/{roomId}`    | `{}`                            | Join room channel      |
| `/app/chat.leave/{roomId}`   | `{}`                            | Leave room channel     |

### Server -> Client (SUBSCRIBE)

| Topic                                | Payload                         | Description            |
|--------------------------------------|---------------------------------|------------------------|
| `/topic/room/{roomId}`              | `{ id, sender, content, timestamp }` | New message in room |
| `/topic/room/{roomId}/typing`       | `{ username, typing }`          | Typing status update   |
| `/topic/presence`                    | `{ username, status }`          | User online/offline    |
| `/user/queue/notifications`          | `{ type, data }`                | Private notifications  |

---

## Project Structure

```
realtime-chat-app/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/portfolio/chat/
│   │   │   │   ├── config/          # Security, WebSocket, Redis, CORS config
│   │   │   │   ├── controller/      # REST and WebSocket controllers
│   │   │   │   ├── dto/             # Data Transfer Objects (Java records)
│   │   │   │   ├── entity/          # JPA entities
│   │   │   │   ├── exception/       # Custom exceptions and global handler
│   │   │   │   ├── repository/      # Spring Data JPA repositories
│   │   │   │   ├── security/        # UserDetailsService implementation
│   │   │   │   └── service/         # Business logic
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       └── application-dev.yml
│   │   └── test/
│   ├── Dockerfile
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── app/
│   │   │   ├── components/          # Angular components
│   │   │   ├── guards/              # Route guards
│   │   │   ├── interceptors/        # HTTP interceptors
│   │   │   ├── models/              # TypeScript interfaces
│   │   │   └── services/            # Angular services
│   │   ├── assets/
│   │   ├── environments/
│   │   └── styles.scss
│   ├── Dockerfile
│   ├── nginx.conf
│   ├── angular.json
│   └── package.json
├── docker-compose.yml
├── .env.example
├── .gitignore
└── README.md
```

---

## Screenshots

> Coming soon -- screenshots of the login page, chat room list, and real-time messaging interface will be added here.

---

## License

This project is licensed under the [MIT License](LICENSE).
