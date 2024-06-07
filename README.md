# Movie Descriptions Server

## Overview

The **Movie Descriptions Server** is a Spring Boot application that provides a RESTful API for managing movies and their categories. The server allows users to create, update, delete, and fetch movie details and their associated categories.

## Table of Contents

- [API Endpoints](#api-endpoints)
- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Getting Started](#getting-started)


### API Endpoints
Run your server and then you can find the swagger documentation at the following url:

http://localhost:8080/swagger-ui.html 

(replace localhost and port with your server's host and port)


## Features

- **Create Movie**: Add a new movie with specific details and associated categories.
- **Update Movie**: Modify the details of an existing movie.
- **Delete Movie**: Remove a movie from the database.
- **Fetch Movies**: Retrieve details of movies with optional filtering and pagination.
- **Fetch Categories**: Get a list of all available categories.

## Technology Stack

- **Java**
- **Spring Boot**
- **Spring Data JPA** (For data persistence and repository management)
- **QueryDSL** (For type-safe queries)
- **JUnit** (Testing framework for unit tests)
- **Mockito** (Mocking framework for unit tests)
- **Gradle** (Build automation tool)

## Getting Started

### Prerequisites

- **Java 17**
- **Gradle** (or use the Gradle wrapper included in the project)

### Installation

1. **Clone the repository**:
   ```sh
   git clone https://github.com/your-username/movie-descriptions-server.git
   cd movie-descriptions-server
2. **Build the project**:
   ```sh
   ./gradlew build
3. **Run the application:t**:
   ```sh
   ./gradlew bootRun
   
### Data
The project uses Postgresql as the database. 
The database configuration can be found in the `application.properties` file.
- You want to create a postgresql database
- You want to update the `application.properties` file with the correct database url, username, and password.
  ```sh
  spring.application.name=movie-descriptions-server
  spring.datasource.url=jdbc:postgresql://localhost:5432/snowhound
  spring.datasource.username=devuser
  spring.datasource.password=devpass
  spring.datasource.driver-class-name=org.postgresql.Driver
  ```
- You want to run the migration script to create the database schema and populate the database with some sample data.
- You can find the sql script in the `src/main/resources/db/migration` folder.
- Database migration for e2e tests is done automatically
  ```sh
  ...
  @Sql(scripts = {"/migration/setup-test-schema.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(scripts = {"/migration/teardown-test-schema.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public class MovieE2eTest {
  ...
  }
  ```

