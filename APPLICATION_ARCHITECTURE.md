# Chat Application - Architecture Overview

## **High-Level Architecture**

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           CLIENT LAYER (Frontend)                            │
│                                                                               │
│  ┌────────────────────────────────────────────────────────────────────────┐ │
│  │                         HTML/JavaScript UI                             │ │
│  │  • chat.html (Thymeleaf Template)                                      │ │
│  │  • SockJS Client Library                                               │ │
│  │  • STOMP Protocol                                                      │ │
│  └────────────────────────────────────────────────────────────────────────┘ │
│                                    ↓                                          │
│                      WebSocket Connection (ws://)                            │
└─────────────────────────────────────────────────────────────────────────────┘
                                    ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│                    SPRING BOOT APPLICATION SERVER                            │
│                                                                               │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │                      CONFIG LAYER                                    │  │
│  │  ┌──────────────────┬──────────────────┬──────────────────┐         │  │
│  │  │  SecurityConfig  │ WebsocketConfig  │ ModelMapperConfig│         │  │
│  │  │  (Auth & CORS)   │ (STOMP Broker)   │ (Entity Mapping) │         │  │
│  │  └──────────────────┴──────────────────┴──────────────────┘         │  │
│  └──────────────────────────────────────────────────────────────────────┘  │
│                                    ↓                                          │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │                     CONTROLLER LAYER                                 │  │
│  │                   (Request Handlers)                                 │  │
│  │  ┌─────────────────┬──────────────────┬──────────────────┐          │  │
│  │  │ AuthController  │ConversationCtrlr │ MessageController│          │  │
│  │  │ /api/signup     │/api/conversations│ @MessageMapping  │          │  │
│  │  │ /api/auth/**    │/api/newChat      │ /chat.sendMessage│          │  │
│  │  │                 │/api/newGroup     │                  │          │  │
│  │  └─────────────────┴──────────────────┴──────────────────┘          │  │
│  │                                                                       │  │
│  │  ┌──────────────────────────────────────────────────────────┐       │  │
│  │  │                 UserController                           │       │  │
│  │  │         /api/users/{userid}/conversations               │       │  │
│  │  │         GET /chatHistory/{conversationid}/messages      │       │  │
│  │  └──────────────────────────────────────────────────────────┘       │  │
│  └──────────────────────────────────────────────────────────────────────┘  │
│                                    ↓                                          │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │                      SERVICE LAYER                                   │  │
│  │              (Business Logic & Operations)                           │  │
│  │  ┌──────────────────┬──────────────────┬──────────────────┐         │  │
│  │  │ UserService      │ConversationService│ MessageService   │         │  │
│  │  │ UserServiceImpl   │ConversationSrvcImpl│ MessageSrvcImpl   │         │  │
│  │  │                  │                  │                  │         │  │
│  │  │ • createUser()   │• getAllConvers() │• saveMessage()   │         │  │
│  │  │ • findUser()     │• createConvers() │• getMessages()   │         │  │
│  │  │ • encoding       │• createGroup()   │• STOMP Broadcast │         │  │
│  │  │   password       │• addParticipant()│  (/topic/...)    │         │  │
│  │  └──────────────────┴──────────────────┴──────────────────┘         │  │
│  └──────────────────────────────────────────────────────────────────────┘  │
│                                    ↓                                          │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │                    REPOSITORY LAYER                                  │  │
│  │              (Data Access & Persistence)                             │  │
│  │  ┌──────────────┬──────────────────┬──────────────┐                 │  │
│  │  │ UserRepository│ConversationRepo  │MessageRepo   │                 │  │
│  │  │ ParticipantRepo │ChatRepo        │              │                 │  │
│  │  │ (JPA)        │ (JPA)            │ (JPA)        │                 │  │
│  │  └──────────────┴──────────────────┴──────────────┘                 │  │
│  └──────────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────┘
                                    ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│                         DATABASE LAYER (MySQL)                               │
│                                                                               │
│  ┌──────────────┬──────────────────┬─────────────────────────┐             │
│  │   user       │  conversation    │  conversation_participant│             │
│  │  • user_id   │  • conversation_id│  • conversation_partic..│             │
│  │  • userName  │  • conversationType│ • conversation_id (FK) │             │
│  │  • phNo      │  • groupName     │  • user_id (FK)        │             │
│  │  • password  │  • createdAt     │  • joinedAt            │             │
│  │  • createdAt │                  │                         │             │
│  └──────────────┴──────────────────┴─────────────────────────┘             │
│                                                                               │
│  ┌──────────────┬──────────────────────────┐                               │
│  │   message    │    chat_message          │                               │
│  │  • message_id│  • id                    │                               │
│  │  • conv._id  │  • sender                │                               │
│  │  • senderId  │  • content               │                               │
│  │  • sender    │  (Legacy/Unused)        │                               │
│  │  • content   │                          │                               │
│  │  • createdAt │                          │                               │
│  └──────────────┴──────────────────────────┘                               │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## **Layer Breakdown**

### **1. PRESENTATION LAYER (Frontend)**
- **Technology**: HTML5, JavaScript, Bootstrap
- **Libraries**: SockJS, STOMP
- **Location**: `src/main/resources/templates/chat.html`
- **Responsibilities**:
  - User interface for chat
  - WebSocket connection management
  - Real-time message display
  - Message input & send

---

### **2. API/CONTROLLER LAYER**
- **Technology**: Spring MVC, Spring WebSocket
- **Location**: `src/main/java/.../controller/`

#### **Controllers**:
1. **AuthController** (`/api`)
   - `POST /signup` - User registration

2. **ConversationController** (`/api`)
   - `GET /users/{userid}/conversations` - Get all conversations for user
   - `POST /conversations/newChat` - Create 1:1 conversation
   - `POST /conversations/newGroup` - Create group conversation
   - `POST /conversations/{conversationid}/addParticipant` - Add participant

3. **MessageController** (`/api`)
   - `@MessageMapping /chat.sendMessage` - WebSocket message handler
   - `GET /chatHistory/{conversationid}/messages` - Get message history

4. **UserController** 
   - User management endpoints

---

### **3. SERVICE LAYER**
- **Technology**: Spring Service, Business Logic
- **Location**: `src/main/java/.../service/`

#### **Services**:
1. **UserService & UserServiceImpl**
   - `createUser()` - Register new user with password encoding
   - User lookup & management

2. **ConversationService & ConversationServiceImpl**
   - `getAllConversations()` - Fetch user's conversations
   - `createConversation()` - Create 1:1 chat
   - `createGroup()` - Create group chat
   - `addParticipant()` - Add members to group

3. **MessageService & MessageServiceImpl**
   - `saveMessage()` - Persist & broadcast message
   - `getMessages()` - Retrieve conversation history
   - Uses `SimpMessagingTemplate` for STOMP broadcast

---

### **4. DATA ACCESS LAYER (Repository)**
- **Technology**: Spring Data JPA
- **Location**: `src/main/java/.../repository/`

#### **Repositories**:
- `UserRepository` - User CRUD
- `ConversationRepository` - Conversation CRUD
- `MessageRepository` - Message CRUD
- `ParticipantRepository` - Participant CRUD
- `ChatRepo` - Legacy chat operations

---

### **5. DOMAIN/MODEL LAYER**
- **Technology**: JPA Entities, Lombok
- **Location**: `src/main/java/.../model/`

#### **Entities**:
```
User (1-to-Many) ←→ ConversationParticipant (Many-to-1)
                ↓
            Conversation (1-to-Many) ←→ Message (Many-to-1)
```

#### **Entity Details**:
1. **User**
   - Fields: id, userName, phNo, password, createdAt
   - Relations: OneToMany with ConversationParticipant

2. **Conversation**
   - Fields: id, conversationType (ONE_TO_ONE/GROUP), groupName, createdAt
   - Relations: OneToMany with ConversationParticipant, Message

3. **ConversationParticipant** (Junction Table)
   - Fields: id, joinedAt
   - Relations: ManyToOne with User & Conversation

4. **Message**
   - Fields: id, senderId, sender, content, createdAt
   - Relations: ManyToOne with Conversation

5. **ChatMessage** (Legacy)
   - Simple message model (unused in main flow)

---

### **6. PAYLOAD LAYER (DTOs)**
- **Location**: `src/main/java/.../payload/`
- **Purpose**: API request/response objects

#### **DTOs**:
- `UserRequest` / `UserResponse`
- `ConversationRequest` / `ConversationResponse`
- `GroupCreateRequest` / `GroupCreateResponse`
- `MessageRequest` / `MessageResponse`
- `RegisterRequest`

---

### **7. CONFIGURATION LAYER**
- **Location**: `src/main/java/.../config/`

#### **Configs**:
1. **WebsocketConfig**
   - Registers STOMP endpoint at `/ws`
   - Configures message broker
   - Application destination prefixes: `/app`
   - Message broker topics: `/topic`

2. **SecurityConfig**
   - Disables CSRF (for API)
   - CORS enabled for all origins
   - Permits public endpoints: `/api/auth/**`, `/api/signup`
   - Role-based access control

3. **ModelMapperConfig**
   - Maps entities to DTOs (automatic conversion)

4. **WebConfig**
   - Additional web configurations

---

### **8. DATABASE LAYER**
- **Technology**: MySQL 8.0+
- **Connection**: HikariCP connection pool
- **ORM**: Hibernate (JPA)

#### **Tables**:
- `user` - User accounts
- `conversation` - Chat conversations
- `conversation_participant` - Conversation members (junction)
- `message` - Chat messages
- `chat_message` - Legacy messages (unused)

---

## **Data Flow**

### **User Registration Flow**
```
Client (POST /api/signup) 
  → AuthController 
  → UserServiceImpl.createUser() 
  → PasswordEncoder (BCrypt) 
  → UserRepository.save() 
  → MySQL: user table
```

### **1-to-1 Chat Creation Flow**
```
Client (POST /api/conversations/newChat)
  → ConversationController
  → ConversationServiceImpl.createConversation()
  → Create Conversation entity
  → Create 2 ConversationParticipant records
  → Save to MySQL: conversation & conversation_participant tables
```

### **Real-Time Message Flow (WebSocket)**
```
Client Browser (SockJS/STOMP)
  → /ws endpoint (WebsocketConfig)
  → MessageController @MessageMapping(/chat.sendMessage)
  → MessageServiceImpl.saveMessage()
    ├─ Fetch User & Conversation from DB
    ├─ Create Message entity
    ├─ MessageRepository.save() → MySQL
    └─ SimpMessagingTemplate.convertAndSend(/topic/conversations/{convId})
  → All connected clients on that topic
  → Display message on UI
```

### **Message History Retrieval Flow**
```
Client (GET /api/chatHistory/{conversationid}/messages)
  → MessageController.getMessages()
  → MessageServiceImpl.getMessages()
  → ConversationRepository.findById()
  → Return conversation.messages
```

---

## **Key Technologies & Dependencies**

| Component | Technology |
|-----------|-----------|
| **Framework** | Spring Boot 4.0.2 |
| **Java Version** | 20+ |
| **Web** | Spring MVC, Thymeleaf |
| **WebSocket** | Spring WebSocket, SockJS, STOMP |
| **ORM** | Spring Data JPA, Hibernate |
| **Database** | MySQL 8.0+ |
| **Security** | Spring Security, BCrypt |
| **Mapping** | ModelMapper 3.2.5 |
| **Utilities** | Lombok |
| **Connection Pool** | HikariCP |

---

## **Communication Protocols**

1. **REST API** - Traditional HTTP endpoints for CRUD operations
2. **WebSocket** - Real-time bidirectional communication for messages
   - Endpoint: `ws://localhost:8080/ws`
   - Message Broker: Simple in-memory broker
   - Topics: `/topic/conversations/{conversationId}`

---

## **Security Features**

- CSRF protection disabled for API
- CORS enabled for all origins
- BCrypt password encryption
- Role-based access control (ADMIN, SELLER)
- Public & authenticated endpoints separation

---

## **Summary**

This is a **multi-layered Spring Boot Chat Application** with:
- ✅ Layered architecture (Controller → Service → Repository → Entity)
- ✅ Real-time WebSocket messaging via STOMP
- ✅ User authentication & management
- ✅ 1-to-1 and group conversations
- ✅ Message persistence in MySQL
- ✅ RESTful API design
- ✅ Security & password encoding

