# Videojuegos REST

## Local PostgreSQL setup

1. Install and start PostgreSQL, then create a database named `videojuegos`.
2. Copy `.env.example` to `.env` and set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` to your local database connection details.
3. Start the application with `.\mvnw.cmd spring-boot:run` on Windows.

The application loads `.env` at startup. The file is excluded from Git; do not commit database credentials.
