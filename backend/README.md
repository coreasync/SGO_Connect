# NetSchoolToken Backend

A modern, robust backend service for token management, built with **FastAPI**, **SQLAlchemy**, and **PostgreSQL**.  
This project is designed for scalability, security, and productivity.

---

## 📁 Project Structure

```
backend/
├── src/
│   ├── core/           # Core configuration, database, logging
│   ├── models/         # SQLAlchemy ORM models
│   ├── routers/        # FastAPI routers (API endpoints)
│   ├── schemas/        # Pydantic schemas (request/response validation)
│   ├── services/       # Business logic and CRUD services
│   └── utils/          # Utilities, exceptions, middlewares, etc.
├── Dockerfile          # Docker build instructions
├── docker-compose.dev.yml  # Docker Compose for development
├── pyproject.toml      # Python dependencies and tool configs
└── .env.dev            # Example environment variables for development
```

---

## ⚙️ Configuration

All configuration is managed via environment variables and the [`src/core/config.py`](src/core/config.py) file.

## 🌱 Environment Variables

| Variable                | Type     | Default         | Description                                               |
|-------------------------|----------|-----------------|-----------------------------------------------------------|
| `ENVIRONMENT`           | str      | development     | Application environment: `development`, `testing`, `production` |
| `LOG_LEVEL`             | str      | TRACE           | Logging level: `TRACE`, `DEBUG`, `INFO`, `SUCCESS`, `WARNING`, `ERROR`, `CRITICAL` |
| `ENABLE_CONSOLE`        | bool     | true            | Enable console logging                                    |
| `ENABLE_JSON`           | bool     | false           | Enable JSON logging                                       |
| `PORT`                  | int      | —               | Port for FastAPI server                                   |
| `TOKEN_EXPIRES_SECONDS` | int      | 300             | Token expiration time in seconds (default: 5 minutes)     |
| `SECRET_KEY`            | str      | —               | Secret key for cryptographic operations                   |
| `SALT`                  | bytes    | —               | Salt for hashing                                          |
| `POSTGRES_USER`         | str      | —               | PostgreSQL username                                       |
| `POSTGRES_PASSWORD`     | str      | —               | PostgreSQL password                                       |
| `POSTGRES_HOST`         | str      | —               | PostgreSQL host                                           |
| `POSTGRES_PORT`         | int      | —               | PostgreSQL port                                           |
| `POSTGRES_DB`           | str      | —               | PostgreSQL database name                                  |


---

## 🚀 Quick Start (Development)

1. **Clone the repository:**
   ```sh
   git clone https://github.com/coreasync/NetSchoolToken/
   cd NetSchoolToken/backend
   uv sync
   ```

2. **Build and run with Docker Compose:**
   ```sh
   docker compose -f docker-compose.dev.yml up --build
   ```

3. **API Docs:**
   - Swagger UI: [http://localhost:5000/docsdev](http://localhost:5000/docsdev)
   - ReDoc: [http://localhost:5000/v1/docs](http://localhost:5000/v1/docs)
   - OpenAPI JSON: [http://localhost:5000/v1/openapi.json](http://localhost:5000/v1/openapi.json)

---

## 🧩 Main Components

- **FastAPI**: High-performance web framework for building APIs.
- **SQLAlchemy**: Powerful ORM for database access.
- **Pydantic**: Data validation and settings management.
- **Loguru**: Elegant logging.
- **PostgreSQL**: Reliable, production-grade database.

---

## 🏗️ Key Directories

- [`src/core/`](src/core/):  
  - `config.py`: Settings, environment, and constants.
  - `database.py`: Async database engine and session management.
  - `log.py`: Logging configuration and utilities.

- [`src/models/`](src/models/):  
  - SQLAlchemy models for all entities.

- [`src/schemas/`](src/schemas/):  
  - Pydantic schemas for API requests and responses.

- [`src/routers/`](src/routers/):  
  - API endpoints grouped by resource.

- [`src/services/`](src/services/):  
  - Business logic, CRUD operations, and service classes.

- [`src/utils/`](src/utils/):  
  - Exception handling, middlewares, utility functions.

---

## 📝 Code Quality

- **Ruff** is used for linting and code style enforcement.
- Configuration is in [`pyproject.toml`](pyproject.toml).
- Run linting:
  ```sh
  ruff check src
  ```

---

## 🛠️ Useful Commands

- **Build Docker image:**  
  `docker build -t netschooltoken-backend .`

- **Start development server locally:**  
  ```sh
  uvicorn src.main:app --reload --host 0.0.0.0 --port 5000
  ```

---

## 🤝 Contributing

Pull requests and issues are welcome!  
Please follow the code style and add tests for new features.

---

## 📝 License

MIT License

---

**Made with ❤️**