# Purchase & Warranty Tracker

A Semester 3 Java web project for recording purchased products, their receipts, warranty details, return periods, and service history.

## Technology stack

- Frontend: HTML, CSS, and Vanilla JavaScript
- Backend: Java 21 and Spring Boot
- Database: MySQL (prepared through a Spring JDBC configuration profile)
- Build tool: Maven

## Project structure

```text
backend/src/main/resources/static/  Frontend files, served by Spring Boot
backend/                             Spring Boot REST API
database/                            Database notes and SQL scripts
docs/                                Project documentation
```

## Run locally

1. Start the backend:

    ```powershell
    cd backend
    mvn spring-boot:run
    ```

2. Open `http://localhost:8080/login.html` in a browser.

Do NOT open the HTML files via Live Server or `file://` anymore, because the frontend
is now served by the backend itself on the same origin as the API. Opening the pages
from a separate origin breaks session cookies and authentication.

## MySQL setup

The default application profile does not require a database so the foundation can be run immediately. When database development begins, copy `backend/src/main/resources/application-mysql.properties.example` to `application-mysql.properties`, set the `DB_*` environment variables, remove the temporary JDBC auto-configuration exclusion in `application.properties`, and run with the `mysql` Spring profile.

Never commit database passwords or local configuration files.
