# CLAUDE.md

## Project Overview
`movie-service` serves the Netflux movie catalog: search, filter, and retrieve movie data. Part of the Netflux distributed system.

## Root Package
`com.vinsguru.netflux.movie`

## Active Tech Stack
- **Backend:** Java 25, Spring Boot 4.1.0, Maven
- **Database:** PostgreSQL (via Testcontainers for local dev and testing)
- **Testing:** JUnit 5, Testcontainers

## Core Operational Commands

### Development & Build Lifecycle
- Build project:
```bash
./mvnw clean compile
```
- Package production artifact:
```bash
./mvnw clean package
```
- Run locally (via TestApplication + Testcontainers):
```bash
./mvnw spring-boot:test-run
```

### Testing Lifecycle
- Run all tests:
```bash
./mvnw test
```
- Run a single test class:
```bash
./mvnw test -Dtest=ClassName
```
