# Purchase & Warranty Tracker

A Semester 3 Java web project for recording purchased products, their receipts, warranty details, return periods, and service history.

## Technology stack

- Frontend: HTML, CSS, and Vanilla JavaScript
- Backend: Java 21 and Spring Boot
- Database: MySQL (prepared through a Spring JDBC configuration profile)
- Build tool: Maven

## Project structure

```text
frontend/  Static browser interface
backend/   Spring Boot REST API
database/  Database notes and future SQL scripts
docs/      Project documentation
```

## Run locally

1. Start the backend:

   ```powershell
   cd backend
   mvn spring-boot:run
   ```

2. Open `frontend/index.html` in a browser, or serve the repository root with a static-file extension such as VS Code Live Server.

The frontend checks `http://localhost:8080/api/health`.

## MySQL setup

The default application profile does not require a database so the foundation can be run immediately. When database development begins, copy `backend/src/main/resources/application-mysql.properties.example` to `application-mysql.properties`, set the `DB_*` environment variables, remove the temporary JDBC auto-configuration exclusion in `application.properties`, and run with the `mysql` Spring profile.

Never commit database passwords or local configuration files.
