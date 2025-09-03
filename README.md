# 💬 Chat App

> A **real-time chat** application built with **Java Spring Boot** and **WebSocket**, featuring a simple frontend with HTML/CSS/JavaScript.  
> The project simulates a modern messaging system that is scalable and ready for real-world deployment.

---

## 🚀 Features

✅ Real-time messaging with WebSocket  
✅ Simple and user-friendly interface (HTML/CSS/JS)  
✅ Multi-user support  
✅ Decoupled backend architecture for easy API/Frontend extension  
✅ Easy deployment with **Docker Compose**

---

## 🛠️ Technologies

- **Backend:** [Spring Boot](https://spring.io/projects/spring-boot) (Java), WebSocket, REST API
- **Frontend:** HTML5, CSS3, JavaScript
- **Build Tool:** Maven
- **Deployment:** Docker & Docker Compose

---

## 📂 Project Structure

```
chat-app/
│
├── .mvn/                 # Maven wrapper
├── src/                  # Source code (Java + frontend)
│   ├── main/
│   │   ├── java/...       # Spring Boot backend
│   │   └── resources/...  # Configurations & templates
│   └── test/...           # Unit tests
│
├── docker-compose.yml    # Run with Docker
├── pom.xml               # Dependency management
├── mvnw / mvnw.cmd       # Maven wrapper for Linux/Mac & Windows
└── README.md             # Documentation
```

---

## ⚡ Installation & Run

### Requirements
- **Java 17+**
- **Maven 3.8+**
- **Docker (optional)**

### Run steps

1. Clone the project:
   ```bash
   git clone https://github.com/hungtd412/chat-app.git
   cd chat-app
   ```

2. Start Redis with Docker:
   ```bash
   docker-compose up
   ```

3. Run with Maven:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

👉 The app will be available at: **http://localhost:9000**

---

## 🔮 Roadmap

- 🌟 Send images, files, emojis
- 🌟 Online/offline status
- 🌟 Audio and video calls
- 🌟 Production deployment
- ...

---

## 🤝 Contribution

Contributions are welcome!  
You can:
- Open an **Issue** for bugs or feature requests
- Create a **Pull Request** with improvements

---

## 👨‍💻 Author

- **HungTD** – [GitHub Profile](https://github.com/hungtd412)

If you find this project useful, please ⭐ the repo to support it!

---

## 🏆 Conclusion

`Chat App` is not just a basic chat application but also a **proof of clean backend/frontend system design that is scalable and practical**.  
A solid foundation for learning, research, or building into a production-ready product.
