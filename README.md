# Pet Platform Backend (Monolith)

Offline monolithic Spring Boot backend for pet supplies trading and cooking practice management.

## 1) Start Command (How to Run)

```bash
docker compose up --build
```

Run in detached mode if preferred:

```bash
docker compose up --build -d
```

Stop and remove containers:

```bash
docker compose down
```

Stop and remove containers + volume data:

```bash
docker compose down -v
```

## 2) Service Address (Services List)

- Application API: `http://localhost:8080`
- Health endpoint: `http://localhost:8080/actuator/health`
- MySQL: `localhost:3306`

Docker services in `docker-compose.yml`:
- `app`
- `mysql`

Explicit ports exposed:
- `8080:8080` (app)
- `3306:3306` (mysql)

## 3) Verification Method

### 3.1 Container-level verification

```bash
docker compose ps
```

Expected: both `app` and `mysql` are `Up` and healthy.

### 3.2 API health verification

```bash
curl http://localhost:8080/actuator/health
```

Expected response contains `"status":"UP"`.

### 3.3 Auth flow verification

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin1234"}'
```

Expected: returns `sessionToken` and user roles including `ADMIN`.

### 3.4 Schema migration verification

- Check app logs and confirm Flyway migration success:

```bash
docker compose logs app
```

Expected: Flyway migration `V1__init.sql` applied successfully.

### 3.5 Full test run (unit + API)

```bash
./run_tests.sh
```

Expected final line includes:

`run_tests.sh output showing unit tests + API tests passing`

## Additional Notes

- Zero private/local runtime dependencies are required other than Docker/Compose.
- Database and app runtime dependencies are fully declared in `docker-compose.yml`.
- Seed user: `admin / admin1234`.
