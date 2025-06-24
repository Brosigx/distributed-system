# Sistemas Distribuidos - Proyecto 2024-2025

Este repositorio contiene el código y la infraestructura para un sistema distribuido desarrollado donde se ha implementado tanto el backend como el frontend.

## Tabla de Contenidos

- [Descripción General](#descripción-general)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Tecnologías Utilizadas](#tecnologías-utilizadas)
  - [Backend](#backend)
  - [Frontend](#frontend)
  - [Bases de Datos](#bases-de-datos)
  - [Contenedores y Orquestación](#contenedores-y-orquestación)
  - [CI/CD](#cicd)
- [Cómo Construir y Ejecutar](#cómo-construir-y-ejecutar)
- [Licencia](#licencia)

---

## Descripción General

El sistema implementa una arquitectura distribuida compuesta por varios servicios backend (REST, gRPC, DAO), un frontend web, y servicios de bases de datos (MongoDB y MySQL), todo orquestado mediante Docker y Docker Compose. El objetivo es proporcionar una plataforma de chat (LlamaChat) con gestión de usuarios, conversaciones y diálogos, soportando distintos backends de almacenamiento y comunicación.

---

## Estructura del Proyecto

```
proyecto/
│
├── backend/                # Lógica de acceso a datos (DAO)
├── backend-grpc/           # Servicios gRPC (definición e implementación)
├── backend-rest/           # Servicio REST principal
├── backend-rest-externo/   # Servicio REST externo
├── db-mongo/               # Dockerfile y scripts de inicialización para MongoDB
├── db-mysql/               # Dockerfile y scripts de inicialización para MySQL
├── frontend/               # Aplicación web (Flask)
├── llamachat/              # Servicio LLM (modelo de lenguaje)
├── docker-compose-devel-mongo.yml # Orquestación de servicios en desarrollo
├── Makefile                # Tareas de automatización
├── mongo.env / sql.env     # Variables de entorno para bases de datos
└── README.md               # Este archivo
```

---

## Tecnologías Utilizadas

### Backend

- **Java 17**  
  Todos los servicios backend están escritos en Java 17, usando Maven como sistema de construcción y gestión de dependencias.

- **Maven**  
  Usado para compilar, testear y empaquetar los servicios Java. Incluye plugins para integración con Protobuf/gRPC y gestión de dependencias.

- **gRPC**  
  Comunicación eficiente entre servicios mediante RPC.
  - Definición de servicios en Protobuf.
  - Generación automática de código cliente/servidor.
  - Implementación en `GrpcServiceImpl`.

- **REST (JAX-RS/Servlet)**  
  Servicios RESTful para exponer la lógica de negocio y acceso a datos.

- **DAO Pattern**  
  Acceso abstracto a datos, soportando tanto MongoDB como MySQL.

### Frontend

- **Python 3.12 + Flask**  
  Aplicación web ligera para la interfaz de usuario, autenticación y gestión de sesiones.
  - Uso de `flask-login` para autenticación.
  - Formularios con `flask-WTF`.
  - Plantillas HTML con Jinja2.
  - Estilos personalizados en CSS.

- **Bootstrap**  
  Framework CSS para diseño responsivo y componentes UI.

### Bases de Datos

- **MongoDB**  
  Base de datos NoSQL para almacenamiento de usuarios y conversaciones.
  - Inicialización automática con scripts.
  - Acceso desde Java usando el driver oficial.

- **MySQL**  
  Base de datos relacional para almacenamiento alternativo.
  - Inicialización con scripts SQL.
  - Acceso desde Java usando el conector oficial.

### Contenedores y Orquestación

- **Docker**  
  Cada componente principal tiene su propio Dockerfile para facilitar la construcción y despliegue.

- **Docker Compose**  
  Orquestación de todos los servicios para desarrollo y pruebas locales.
  - `docker-compose-devel-mongo.yml`: Levanta todos los servicios necesarios para el entorno de desarrollo con MongoDB.

### CI/CD

- **GitHub Actions**  
  Integración continua definida en `.github/workflows/makefile.yml`.
  - Compila y construye el proyecto automáticamente en cada push o pull request a ramas principales.

---

## Cómo Construir y Ejecutar

### Requisitos

- Docker y Docker Compose instalados.
- (Opcional) Java 17 y Maven para desarrollo local sin contenedores.
- Python 3.12 para desarrollo del frontend fuera de Docker.

### Ejecución en Desarrollo

1. **Clonar el repositorio**
   ```sh
   git clone <url-del-repo>
   cd proyecto
   ```

2. **Levantar todos los servicios con Docker Compose**
   ```sh
   docker-compose -f docker-compose-devel-mongo.yml up --build
   ```

   Esto iniciará:
   - Frontend Flask en [http://localhost:5010](http://localhost:5010)
   - Backend REST en [http://localhost:8080](http://localhost:8080)
   - Backend REST externo en [http://localhost:8081](http://localhost:8081)
   - Servicio gRPC en el puerto 50051
   - MongoDB en el puerto 27017
   - LlamaChat (servicio LLM) en el puerto 5020

3. **Parar los servicios**
   ```sh
   docker-compose -f docker-compose-devel-mongo.yml down
   ```

### Construcción Manual

Cada subdirectorio contiene un Makefile para construir imágenes individuales:
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

## Licencia

Este proyecto está bajo la licencia CC0 1.0 Universal, lo que significa que puedes usar, modificar y distribuir el código sin restricciones.

---

## Créditos

Desarrollado por Ambrosio Ramón Guardiola, y Aurélio Sánchez Soriano.