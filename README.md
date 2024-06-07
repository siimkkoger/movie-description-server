# Movie Descriptions Server

## Overview

The **Movie Descriptions Server** is a Spring Boot application that provides a RESTful API for managing movies and their categories. The server allows users to create, update, delete, and fetch movie details and their associated categories.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Getting Started](#getting-started)
- [API Endpoints](#api-endpoints)


## Features

- **Create Movie**: Add a new movie with specific details and associated categories.
- **Update Movie**: Modify the details of an existing movie.
- **Delete Movie**: Remove a movie from the database.
- **Fetch Movies**: Retrieve details of movies with optional filtering and pagination.
- **Fetch Categories**: Get a list of all available categories.

## Technology Stack

- **Java**: Programming language used for development.
- **Spring Boot**: Framework for building the application.
- **Spring Data JPA**: For data persistence and repository management.
- **QueryDSL**: For type-safe queries.
- **JUnit**: Testing framework.
- **Mockito**: Mocking framework for unit tests.
- **Gradle**: Build automation tool.

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

### API Endpoints

http://localhost:8080/swagger-ui.html

