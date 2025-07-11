# 💬 Phase 3: Real-time Chat Application

## 🎯 **Learning Objectives**

This phase focuses on **Publisher-Subscriber patterns**, **real-time communication**, and **message-driven architectures** through building a comprehensive chat platform. You'll master advanced structural patterns, real-time data synchronization, and scalable messaging systems.

---

## 🏗️ **System Overview**

### **What We're Building**
A production-level **Real-time Chat Application** similar to WhatsApp, Slack, or Discord that handles:
- 💬 **Real-time Messaging**: Instant message delivery and receipt
- 👥 **Group Chats**: Multi-user conversations and channels
- 🔒 **User Management**: Authentication, authorization, and presence
- 📁 **File Sharing**: Media upload and download capabilities
- 🔔 **Notifications**: Push notifications and message alerts
- 🏢 **Organizations**: Workspace management and team collaboration
- 🔍 **Message Search**: Full-text search across conversations
- ⚡ **Offline Support**: Message queuing and synchronization

### **Architecture Highlights**
- **Publisher-Subscriber Pattern**: Message broadcasting and event handling
- **Mediator Pattern**: Chat room coordination and message routing
- **Composite Pattern**: Hierarchical user groups and organizations
- **WebSocket Integration**: Real-time bidirectional communication
- **Message Queue Systems**: Reliable message delivery
- **Microservices Architecture**: Scalable service design

---

## 🎨 **Design Patterns to Master**

### **1. Publisher-Subscriber Pattern** 📡
**Purpose**: Decouple message senders from receivers for scalable communication

**Implementation**:
```java
public interface MessagePublisher {
    void publish(Message message, String topic);
    void subscribe(String topic, MessageSubscriber subscriber);
    void unsubscribe(String topic, MessageSubscriber subscriber);
}

public interface MessageSubscriber {
    void onMessageReceived(Message message);
    void onUserStatusChanged(UserStatusEvent event);
    void onChannelUpdated(ChannelUpdateEvent event);
}

@Service
public class ChatMessageBroker implements MessagePublisher {
    private final Map<String, Set<MessageSubscriber>> topicSubscribers;
    
    @Override
    public void publish(Message message, String topic) {
        Set<MessageSubscriber> subscribers = topicSubscribers.get(topic);
        subscribers.forEach(subscriber -> 
            subscriber.onMessageReceived(message)
        );
    }
}
```

**Real-world Usage**:
- Message broadcasting to multiple users
- Real-time status updates
- Channel notifications
- System-wide announcements

---

### **2. Mediator Pattern** 🎭
**Purpose**: Centralize complex communications and control logic

**Implementation**:
```java
public interface ChatMediator {
    void sendMessage(Message message, User sender);
    void addUserToChannel(User user, Channel channel);
    void removeUserFromChannel(User user, Channel channel);
    void notifyTyping(User user, Channel channel);
}

@Service
public class ChatRoomMediator implements ChatMediator {
    private final MessagePublisher messagePublisher;
    private final UserPresenceService presenceService;
    private final ChannelService channelService;
    
    @Override
    public void sendMessage(Message message, User sender) {
        // Validate permissions
        // Process message content
        // Route to appropriate channels
        // Update message history
        // Send notifications
    }
}
```

**Real-world Usage**:
- Chat room management
- Message routing logic
- User interaction coordination
- Permission management

---

### **3. Composite Pattern** 🌳
**Purpose**: Handle hierarchical structures of users, groups, and organizations

**Implementation**:
```java
public abstract class ChatEntity {
    protected String id;
    protected String name;
    protected Set<Permission> permissions;
    
    public abstract void addMember(ChatEntity entity);
    public abstract void removeMember(ChatEntity entity);
    public abstract Set<User> getAllUsers();
    public abstract boolean hasPermission(Permission permission);
}

public class Organization extends ChatEntity {
    private Set<Team> teams;
    private Set<Channel> channels;
    
    @Override
    public Set<User> getAllUsers() {
        return teams.stream()
            .flatMap(team -> team.getAllUsers().stream())
            .collect(Collectors.toSet());
    }
}

public class Team extends ChatEntity {
    private Set<User> members;
    private Set<Channel> channels;
}

public class User extends ChatEntity {
    private UserProfile profile;
    private UserStatus status;
}
```

**Real-world Usage**:
- Organizational hierarchies
- Permission inheritance
- Bulk operations on groups
- Nested team structures

---

### **4. Flyweight Pattern** 🪶
**Purpose**: Efficiently handle large numbers of similar message objects

**Implementation**:
```java
public class MessageFlyweight {
    private final MessageType type;
    private final String template;
    private final MessageFormat format;
    
    // Intrinsic state (shared)
    public MessageFlyweight(MessageType type, String template, MessageFormat format) {
        this.type = type;
        this.template = template;
        this.format = format;
    }
    
    public RenderedMessage render(MessageContext context) {
        // Use extrinsic state (context) to render message
        return new RenderedMessage(
            format.apply(template, context),
            context.getTimestamp(),
            context.getSender(),
            context.getChannel()
        );
    }
}

@Service
public class MessageFlyweightFactory {
    private final Map<String, MessageFlyweight> flyweights = new ConcurrentHashMap<>();
    
    public MessageFlyweight getFlyweight(MessageType type, String template, MessageFormat format) {
        String key = type + ":" + template + ":" + format;
        return flyweights.computeIfAbsent(key, 
            k -> new MessageFlyweight(type, template, format)
        );
    }
}
```

**Real-world Usage**:
- Memory-efficient message storage
- Template-based messages
- System message optimization
- Large-scale message handling

---

### **5. Proxy Pattern** 🛡️
**Purpose**: Control access, add caching, and provide additional functionality

**Implementation**:
```java
public interface MessageService {
    Message sendMessage(SendMessageRequest request);
    List<Message> getMessages(GetMessagesRequest request);
    void deleteMessage(String messageId, String userId);
}

@Service
public class CachingMessageServiceProxy implements MessageService {
    private final MessageService realService;
    private final CacheManager cacheManager;
    private final SecurityService securityService;
    
    @Override
    public List<Message> getMessages(GetMessagesRequest request) {
        // Security check
        securityService.validateAccess(request.getUserId(), request.getChannelId());
        
        // Cache check
        String cacheKey = generateCacheKey(request);
        List<Message> cached = cacheManager.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        // Delegate to real service
        List<Message> messages = realService.getMessages(request);
        cacheManager.put(cacheKey, messages);
        return messages;
    }
}
```

**Real-world Usage**:
- Access control and security
- Performance optimization through caching
- Rate limiting and throttling
- Logging and monitoring

---

### **6. Adapter Pattern** 🔌
**Purpose**: Support multiple client types and external integrations

**Implementation**:
```java
// External notification service interface
public interface ExternalNotificationService {
    void sendNotification(ExternalNotification notification);
}

// Our internal notification interface
public interface InternalNotificationService {
    void sendChatNotification(ChatNotification notification);
}

@Service
public class NotificationServiceAdapter implements InternalNotificationService {
    private final ExternalNotificationService externalService;
    
    @Override
    public void sendChatNotification(ChatNotification chatNotification) {
        // Adapt internal format to external format
        ExternalNotification externalNotification = new ExternalNotification(
            chatNotification.getRecipientId(),
            adaptMessage(chatNotification.getMessage()),
            adaptPriority(chatNotification.getPriority())
        );
        
        externalService.sendNotification(externalNotification);
    }
}
```

**Real-world Usage**:
- Multiple client applications (web, mobile, desktop)
- Third-party service integration
- Legacy system compatibility
- Protocol translation

---

## 🏛️ **System Architecture**

### **Domain Model**
```
Chat Application
├── User Management
│   ├── User
│   ├── UserProfile
│   ├── UserStatus
│   └── Authentication
├── Messaging
│   ├── Message
│   ├── DirectMessage
│   ├── ChannelMessage
│   └── MessageThread
├── Channels & Groups
│   ├── Channel
│   ├── PrivateChannel
│   ├── PublicChannel
│   └── Group
├── Organizations
│   ├── Organization
│   ├── Team
│   ├── Role
│   └── Permission
└── Notifications
    ├── PushNotification
    ├── EmailNotification
    └── InAppNotification
```

### **Microservices Architecture**
```
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│  User Service   │  │ Message Service │  │Channel Service  │
├─────────────────┤  ├─────────────────┤  ├─────────────────┤
│ • Authentication│  │ • Message CRUD  │  │ • Channel CRUD  │
│ • User Profiles │  │ • Message Queue │  │ • Membership    │
│ • Presence      │  │ • Message Search│  │ • Permissions   │
└─────────────────┘  └─────────────────┘  └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│Notification Svc │  │ Gateway Service │  │ File Service    │
├─────────────────┤  ├─────────────────┤  ├─────────────────┤
│ • Push Notifs   │  │ • Load Balancing│  │ • File Upload   │
│ • Email Alerts  │  │ • Rate Limiting │  │ • File Download │
│ • Real-time     │  │ • Authentication│  │ • File Storage  │
└─────────────────┘  └─────────────────┘  └─────────────────┘
```

### **Real-time Communication Stack**
```
┌─────────────────────────────────────┐
│           Client Layer              │
│    (Web, Mobile, Desktop)           │
├─────────────────────────────────────┤
│         WebSocket Layer             │
│    (Socket.IO, Native WebSocket)    │
├─────────────────────────────────────┤
│        Message Broker Layer         │
│     (Redis Pub/Sub, RabbitMQ)       │
├─────────────────────────────────────┤
│        Application Layer            │
│      (Spring Boot Services)         │
├─────────────────────────────────────┤
│         Persistence Layer           │
│    (PostgreSQL, MongoDB, Redis)     │
└─────────────────────────────────────┘
```

---

## 🚀 **Key Features to Implement**

### **💬 Real-time Messaging**
- WebSocket-based instant messaging
- Message delivery confirmation
- Read receipts and typing indicators
- Message editing and deletion
- Rich text and emoji support

### **👥 User & Presence Management**
- User authentication and authorization
- Online/offline status tracking
- User profiles and settings
- Friend/contact management
- Presence broadcasting

### **🏢 Organization & Teams**
- Multi-tenant organization support
- Team-based access control
- Role and permission management
- Hierarchical organizational structure
- Workspace administration

### **📁 File & Media Sharing**
- Image and video upload/download
- File preview and thumbnails
- Large file handling with chunking
- CDN integration for performance
- File access control

### **🔍 Search & Discovery**
- Full-text message search
- User and channel discovery
- Message filtering and sorting
- Search result highlighting
- Advanced search queries

### **🔔 Notification System**
- Real-time push notifications
- Email notification integration
- Customizable notification preferences
- Mobile push notification support
- Notification history and management

---

## 🧪 **Advanced Concepts**

### **WebSocket Integration**
```java
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {
    
    private final ChatMediator chatMediator;
    private final SessionManager sessionManager;
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String userId = extractUserId(session);
        sessionManager.addSession(userId, session);
        chatMediator.notifyUserOnline(userId);
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        ChatMessage chatMessage = parseMessage(message.getPayload());
        chatMediator.sendMessage(chatMessage, extractUser(session));
    }
}
```

### **Message Queue Integration**
```java
@Service
public class MessageQueueService {
    
    @RabbitListener(queues = "chat.messages")
    public void handleMessage(MessageEvent event) {
        // Process incoming messages
        chatMediator.routeMessage(event.getMessage());
    }
    
    @EventListener
    public void handleMessageSent(MessageSentEvent event) {
        // Publish to message queue for persistence and delivery
        rabbitTemplate.convertAndSend("chat.messages", event);
    }
}
```

### **Caching Strategy**
```java
@Service
public class MessageCacheService {
    
    @Cacheable(value = "recent-messages", key = "#channelId")
    public List<Message> getRecentMessages(String channelId, int limit) {
        return messageRepository.findRecentByChannelId(channelId, limit);
    }
    
    @CacheEvict(value = "recent-messages", key = "#message.channelId")
    public void invalidateRecentMessages(Message message) {
        // Cache invalidation on new messages
    }
}
```

### **Security & Authorization**
```java
@Service
public class ChatSecurityService {
    
    public boolean canUserAccessChannel(String userId, String channelId) {
        User user = userService.getUser(userId);
        Channel channel = channelService.getChannel(channelId);
        
        return permissionEvaluator.hasPermission(
            user, channel, ChannelPermission.READ
        );
    }
    
    public boolean canUserSendMessage(String userId, String channelId) {
        // Complex permission logic
        return hasChannelAccess(userId, channelId) && 
               !isUserMuted(userId, channelId) &&
               hasWritePermission(userId, channelId);
    }
}
```

---

## 📚 **Learning Progression**

### **Week 1: Foundation & Basic Messaging**
- [ ] Project setup with WebSocket support
- [ ] Basic domain model implementation
- [ ] Simple one-to-one messaging
- [ ] User authentication and session management

### **Week 2: Advanced Patterns & Group Chat**
- [ ] Publisher-Subscriber pattern implementation
- [ ] Mediator pattern for chat rooms
- [ ] Group chat functionality
- [ ] Message persistence and retrieval

### **Week 3: Scalability & Performance**
- [ ] Composite pattern for organizations
- [ ] Flyweight pattern for message optimization
- [ ] Caching and performance optimization
- [ ] Message queue integration

### **Week 4: Advanced Features & Integration**
- [ ] File sharing and media support
- [ ] Search functionality
- [ ] Notification system
- [ ] Mobile client support

---

## 🎯 **Success Criteria**

After completing Phase 3, you should be able to:

### **Technical Mastery**
- ✅ Implement complex structural and behavioral patterns
- ✅ Design real-time, event-driven systems
- ✅ Handle WebSocket communication effectively
- ✅ Create scalable messaging architectures
- ✅ Optimize for performance and memory usage

### **Real-world Skills**
- ✅ Build production-ready chat applications
- ✅ Implement microservices communication patterns
- ✅ Handle real-time data synchronization
- ✅ Design for high availability and scalability
- ✅ Integrate with external services and APIs

### **Interview Preparation**
- ✅ Explain Publisher-Subscriber pattern benefits
- ✅ Design scalable chat system architectures
- ✅ Discuss real-time communication challenges
- ✅ Handle system reliability and fault tolerance
- ✅ Optimize for performance at scale

---

## 🚀 **Getting Started**

### **Prerequisites**
- Completion of Phase 1 & 2
- Understanding of WebSocket technology
- Basic knowledge of message queues
- Familiarity with real-time systems

### **Setup Instructions**
```bash
# Switch to Phase 3 branch
git checkout phase/3/chat-application

# Install dependencies
mvn clean install

# Start Redis (for message broker)
docker run -d -p 6379:6379 redis:alpine

# Run the application
mvn spring-boot:run

# Run tests
mvn test

# Access WebSocket endpoint
# ws://localhost:8080/chat
```

### **Technology Stack**
- **Backend**: Spring Boot, Spring WebSocket, Spring Security
- **Message Broker**: Redis Pub/Sub or RabbitMQ
- **Database**: PostgreSQL (main data), Redis (caching)
- **Real-time**: WebSocket, Server-Sent Events
- **File Storage**: AWS S3 or local file system
- **Search**: Elasticsearch (optional)

---

## 🎉 **Ready to Chat?**

This phase will teach you how to build scalable, real-time systems that can handle thousands of concurrent users. The patterns and techniques learned here are fundamental to many modern applications beyond just chat systems.

**Let's create something that connects people! 💬🚀**

---

## 📞 **Support**

- **Documentation**: Real-time system design guides
- **Code Examples**: WebSocket and messaging patterns
- **Test Cases**: Comprehensive testing strategies
- **Performance**: Optimization techniques and benchmarks

**Happy Real-time Coding! 🎓**
