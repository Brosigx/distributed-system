# Distributed Systems

This repository contains the code and infrastructure for a distributed system developed which includes back & front end.

## Table of Contents

- [General Description](#general-description)
- [Project Structure](#project-structure)
- [Technologies Used](#technologies-used)
  - [Backend](#backend)
  - [Frontend](#frontend)
  - [Databases](#databases)
  - [Containers and Orchestration](#containers-and-orchestration)
  - [CI/CD](#cicd)
- [How to Build and Run](#how-to-build-and-run)
- [License](#license)

---

## General Description

The system implements a distributed architecture composed of several backend services (REST, gRPC, DAO), a web frontend, and database services (MongoDB and MySQL), all orchestrated using Docker and Docker Compose. The goal is to provide a chat platform (LlamaChat) with user, conversation, and dialogue management, supporting different storage and communication backends.

---

## Project Structure

```
proyecto/
│
├── backend/                # Data Access Logic (DAO)
├── backend-grpc/           # gRPC services (definition and implementation)
├── backend-rest/           # Main REST service
├── backend-rest-externo/   # External REST service
├── db-mongo/               # Dockerfile and initialization scripts for MongoDB
├── db-mysql/               # Dockerfile and initialization scripts for MySQL
├── frontend/               # Web application (Flask)
├── llamachat/              # LLM service (language model)
├── docker-compose-devel-mongo.yml # Service orchestration for development
├── Makefile                # Automation tasks
├── mongo.env / sql.env     # Environment variables for databases
└── README.md               # This file
```

---

## Technologies Used

### Backend

- **Java 17**  
  All backend services are written in Java 17, using Maven as the build and dependency management system.

- **Maven**  
  Used to compile, test, and package Java services. Includes plugins for Protobuf/gRPC integration and dependency management.

- **gRPC**  
  Efficient communication between services via RPC.
  - Service definitions in Protobuf.
  - Automatic generation of client/server code.
  - Implementation in `GrpcServiceImpl`.

- **REST (JAX-RS/Servlet)**  
  RESTful services to expose business logic and data access.

- **DAO Pattern**  
  Abstract data access, supporting both MongoDB and MySQL.

### Frontend

- **Python 3.12 + Flask**  
  Lightweight web application for the user interface, authentication, and session management.
  - Uses `flask-login` for authentication.
  - Forms with `flask-WTF`.
  - HTML templates with Jinja2.
  - Custom CSS styles.

- **Bootstrap**  
  CSS framework for responsive design and UI components.

### Databases

- **MongoDB**  
  NoSQL database for storing users and conversations.
  - Automatic initialization with scripts.
  - Accessed from Java using the official driver.

- **MySQL**  
  Relational database for alternative storage.
  - Initialization with SQL scripts.
  - Accessed from Java using the official connector.

### Containers and Orchestration

- **Docker**  
  Each main component has its own Dockerfile to facilitate building and deployment.

- **Docker Compose**  
  Orchestrates all services for local development and testing.
  - `docker-compose-devel-mongo.yml`: Starts all necessary services for the development environment with MongoDB.

### CI/CD

- **GitHub Actions**  
  Continuous integration defined in `.github/workflows/makefile.yml`.
  - Compiles and builds the project automatically on every push or pull request to main branches.

---

## How to Build and Run

### Requirements

- Docker and Docker Compose installed.
- (Optional) Java 17 and Maven for local development without containers.
- Python 3.12 for frontend development outside Docker.

### Development Execution

1. **Clone the repository**
   ```sh
   git clone <repo-url>
   cd proyecto
   ```

2. **Start all services with Docker Compose**
   ```sh
   docker-compose -f docker-compose-devel-mongo.yml up --build
   ```

   This will start:
   - Flask frontend at [http://localhost:5010](http://localhost:5010)
   - REST backend at [http://localhost:8080](http://localhost:8080)
   - External REST backend at [http://localhost:8081](http://localhost:8081)
   - gRPC service on port 50051
   - MongoDB on port 27017
   - LlamaChat (LLM service) on port 5020

3. **Stop the services**
   ```sh
   docker-compose -f docker-compose-devel-mongo.yml down
   ```

### Manual Build

Each subdirectory contains a Makefile to build individual images:
```sh
cd backend
make
cd ../backend-grpc
make
cd ../frontend
make
# etc.
```

---

## License

This project is under the CC0 1.0 Universal license, which means you can use, modify, and distribute the code without restrictions.

---

## Credits

Developed by Ambrosio Ramón Guardiola and Aurélio Sánchez Soriano.
