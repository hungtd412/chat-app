# Chat App — Architecture & Tech Stack Summary

## 1) Project Overview
This project is a **real-time chat application** built as a monolithic Spring Boot service that combines:
- REST APIs for authentication, users, friends, conversations, and message history.
- WebSocket (STOMP over SockJS) for real-time message delivery.
- Server-hosted static frontend pages (HTML/CSS/JavaScript).

The app runs on port `9000` and uses MySQL for persistent data, Redis for JWT blacklist/session invalidation support, and Cloudinary for media storage.

---

## 2) High-Level Architecture

### Backend (Spring Boot)
Layered structure under `src/main/java/com/hungtd/chatapp`:
- **Controller layer**: Exposes REST endpoints and WebSocket message mapping.
- **Service layer**: Contains business logic (auth, users, friends, conversations, messaging, websocket dispatch, cloudinary).
- **Repository layer**: Spring Data JPA repositories with custom JPQL/native queries.
- **Domain layer**: JPA entities + enums.
- **Infrastructure/config layer**: Security, JWT decoder, WebSocket broker/interceptor, Redis, Cloudinary, startup seed data.

### Frontend (Static resources)
Located in `src/main/resources/static`:
- `signin`, `signup`, `mainscreen`, `chat`, `friend-management`, `setting`, shared `utils`, and `paginator` modules.
- Uses `localStorage` for `access_token` and `refresh_token`.
- Calls REST APIs via AJAX/fetch and receives real-time messages through STOMP subscriptions.

### Data & External Services
- **MySQL**: users, friendships, friend requests, conversations, participants, messages.
- **Redis**: blacklisted JWT storage with TTL.
- **Cloudinary**: avatar/group image upload and deletion.
- **Eureka**: service registration/discovery enabled (client configured).

---

## 3) Main Functional Domains

### Authentication & Authorization
- JWT-based auth with access + refresh token model.
- Login (`/auth/log-in`) issues both tokens.
- Refresh (`/auth/refresh`) validates refresh token and rotates access token.
- Logout (`/auth/log-out`) blacklists access/refresh tokens in Redis.
- Security configured as OAuth2 resource server with custom JWT decoding.

### User Management
- Register user (`POST /users`), get profile, get by ID, list users (admin), update avatar/profile.
- Avatars stored on Cloudinary; previous non-default avatar is removed when replaced.

### Friend Management
- Send/accept/reject friend requests.
- On friend request acceptance:
  - friendship record is created,
  - a private conversation is auto-created.
- Friend listing supports pagination and returns friend identity + associated private conversation.

### Conversation & Messaging
- Retrieve current user conversations sorted by newest message.
- Update group title and group image.
- Message history API supports cursor-like pagination (`offset` message id + limit).
- Real-time messaging flow:
  1. Client sends STOMP message to `/app/chat.send`.
  2. Server validates JWT from STOMP header.
  3. Server validates sender belongs to conversation.
  4. Message saved in DB.
  5. Server pushes to per-user queue destinations.

---

## 4) Package/Module Structure (Backend)

- `configuration`: `SecurityConfig`, `WebSocketConfig`, `WebSocketAuthInterceptor`, `RedisConfig`, `CloudinaryConfig`, `CustomJwtDecoder`, init seeding.
- `controller`: `AuthenticationController`, `UserController`, `FriendController`, `ConversationController`, `MessageController`.
- `service`: domain services and implementations:
  - `auth`, `user`, `friend`, `conversation`, `chat`, `websocket`, `cloudinary`.
- `repository`: JPA interfaces for all major entities.
- `entity`: `User`, `Conversation`, `Participant`, `Message`, `Friend`, `FriendRequest`, etc.
- `dto`: request/response models.
- `mapper`: MapStruct mappers.
- `exception`: centralized app exception + global handler.
- `util`: JWT and file upload helpers.
- `audit`: JPA auditing support (`createdAt`, `updatedAt` via base entity strategy).

---

## 5) Data Model Snapshot
Core tables/entities:
- **users**: account/profile/roles/avatar.
- **friend_requests**: sender, receiver, status (PENDING/ACCEPTED/...).
- **friends**: symmetric friendship by composite key (`user_id_1`, `user_id_2`).
- **conversations**: type (`PRIVATE`/`GROUP`), title, image.
- **participants**: composite key (`conversation_id`, `user_id`), role (`ADMIN`/`MEMBER`).
- **messages**: conversation, sender id, type (`TEXT`/`THUMB`/`FILE`), content.

---

## 6) Request/Message Flow Summary

### REST Request Flow
Frontend → Controller → Service → Repository → MySQL → Response DTO

### WebSocket Message Flow
Frontend STOMP client → `/app/chat.send` (`@MessageMapping`) → `MessageService` → save message → `SimpMessagingTemplate.convertAndSendToUser(...)` → receivers subscribe on `/user11/{userId}/queue/messages`

---

## 7) Tech Stack

### Core
- **Java 17**
- **Spring Boot 3.4.5**
- **Maven**

### Spring Modules
- Spring Web
- Spring Data JPA
- Spring Security
- OAuth2 Resource Server
- Spring Validation
- Spring WebSocket + STOMP
- Spring Data Redis
- Spring Cloud Netflix Eureka Client

### Supporting Libraries
- **MapStruct** (DTO/entity mapping)
- **Lombok**
- **Nimbus JOSE JWT** (JWT creation/verification)
- **Cloudinary Java SDK**
- **Apache HttpClient5**
- **Commons IO**
- **WebJars STOMP WebSocket**

### Data & Infra
- **MySQL**
- **Redis** (docker-compose provided)
- **Cloudinary**
- **Eureka Server** (external, expected at `localhost:8761`)

### Frontend
- **HTML/CSS/JavaScript**
- **jQuery**
- **SockJS + STOMP client**

---

## 8) Configuration & Runtime Notes
- Main configuration: `src/main/resources/application.yaml`.
- Sensitive values come from `env.properties` and placeholders:
  - DB credentials
  - JWT signer key
  - Cloudinary credentials
- Static resources are served from `classpath:/static/`.
- Redis runs via `docker-compose.yml`.
- App includes bootstrap sample data generation in `AppInitConfig`.

---

## 9) Architectural Characteristics

### Strengths
- Clear layered architecture and domain separation.
- Real-time + REST hybrid communication model.
- Token blacklist with Redis improves logout/session control.
- Cloudinary integration externalizes media storage.

### Current Constraints / Improvement Opportunities
- Frontend currently static and tightly coupled to fixed backend URL (`localhost:9000`) in JS files.
- WebSocket user destination prefix is custom (`/user11`), which is unconventional and requires strict client/server consistency.
- Some profile update methods are scaffolded but not implemented (`updatePassword`, `updateEmail`).
- Monolithic deployment (service discovery enabled, but still a single app codebase).

---

## 10) Conclusion
The project is a **Spring Boot monolith for real-time messaging** with a practical architecture: JWT-secured REST APIs, STOMP WebSocket messaging, JPA/MySQL persistence, Redis-backed token invalidation, and Cloudinary media storage. It is a solid foundation for evolving toward production-grade chat features and/or future service decomposition.
