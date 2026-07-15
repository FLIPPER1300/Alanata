# Alanata Library Management

Spring Boot application for managing books and their copies (REST API + simple Thymeleaf UI).

## 1. Requirements

Before running the project, make sure you have:

- **JDK 25**
- Internet connection (Maven Wrapper downloads dependencies)

Verify Java installation:

```bash
java -version
```

## 2. Running the project

### Windows (recommended via Maven Wrapper)

```powershell
.\mvnw.cmd spring-boot:run
```

### macOS / Linux

```bash
./mvnw spring-boot:run
```

The application runs at: `http://localhost:8080`

> If you have Java configuration issues, set `JAVA_HOME` to JDK 25.

## 3. Startup seed data

On first run, sample books are inserted automatically (only if the database is empty).

Configuration in `src/main/resources/application.yml`:

```yaml
app:
  seed-books: true
```

In tests, seeding is disabled (`src/test/resources/application.yml`) so it does not affect unit tests.

## 4. REST API documentation

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

### Pageable (book listing)

The `GET /api/books` endpoint uses query parameters:

- `page` (starting from 0)
- `size`
- `sort` in `field,direction` format (for example `title,asc`)

Example:

```text
/api/books?page=0&size=5&sort=title,asc
```

## 5. Simple web UI (Thymeleaf)

- Book list: `http://localhost:8080/books`
- Create book form: `http://localhost:8080/books/new`

## 6. H2 console

- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:librarydb`
- User: `Filip`
- Password: `Password1`

## 7. Running tests

```powershell
.\mvnw.cmd test
```

Or run full verification including JaCoCo coverage:

```powershell
.\mvnw.cmd clean verify
```
