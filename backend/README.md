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
| `SALT`                  | bytes    | —               | Salt for hashing (auto-generated from `SECRET_KEY`)       |
| `POSTGRES_USER`         | str      | —               | PostgreSQL username                                       |
| `POSTGRES_PASSWORD`     | str      | —               | PostgreSQL password                                       |
| `POSTGRES_HOST`         | str      | —               | PostgreSQL host (e.g., `localhost` or service name)       |
| `POSTGRES_PORT`         | int      | —               | PostgreSQL port                                           |
| `POSTGRES_DB`           | str      | —               | PostgreSQL database name                                  |

> **Note:**  
> `SALT` is automatically generated from `SECRET_KEY` and does not need to be set manually.

```env
# Example .env.dev
ENVIRONMENT="development"
ENABLE_CONSOLE=true
ENABLE_JSON=false
PORT=5000
SECRET_KEY="your_secret_key"
POSTGRES_HOST="DATABASE"
POSTGRES_USER="user"
POSTGRES_PASSWORD="password"
POSTGRES_PORT=9999
POSTGRES_DB="netschooltoken_backend"
```

---

## 🚀 Quick Start (Development)

1. **Clone the repository:**
   ```sh
   git clone <repo-url>
   cd NetSchoolToken/backend
   ```

2. **Configure environment:**
   - Copy `.env.dev` and adjust values as needed.

3. **Build and run with Docker Compose:**
   ```sh
   docker compose -f docker-compose.dev.yml up --build
   ```

4. **API Docs:**
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

## 🧪 Testing

- Tests are located in the `tests/` directory.
- Use `pytest` for running tests.
- For test environment, create a separate `.env.test` and Docker Compose file if needed.

```sh
PYTHONPATH=src pytest
```

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

- **Run migrations (if using Alembic):**  
  _Add instructions here if migrations are set up._

- **Start development server locally:**  
  ```sh
  uvicorn src.main:app --reload --host 0.0.0.0 --port 5000
  ```

---

## 📚 API Example

**Create Token:**
```http
POST /v1/tokens/
Content-Type: application/json

{
  "refresh_token": "string",
  "time_to_refresh": "2025-01-01T00:00:00Z",
  "users": []
}
```

**Get Token:**
```http
GET /v1/tokens/{token_id}
```

---

## 🤝 Contributing

Pull requests and issues are welcome!  
Please follow the code style and add tests for new features.

---

## 📝 License

MIT License

---

**Made with ❤️ using FastAPI and modern Python.**