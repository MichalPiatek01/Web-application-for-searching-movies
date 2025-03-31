# Web application for searching movies

This project is a web application for searching movies using data from The Open Movie Database (OMDb). It is developed in Java, with PostgreSQL as the database and Vaadin as the front-end framework. The application allows users to search for movies, rate them, and add them to their list of watched films.

# Features

- Create, update and delete own reviews
- Delete revies (admin)
- Add movies to your watched films

# Prerequisites

- JDK 22
- PostrgeSQL
- PostgeSQL server

# Getting Started

1. Clone the repository.
2. Configure the PostgreSQL database settings in the application properties file.
- You can set spring.jpa.hibernate.ddl-auto=create to have the schema created automatically. This setting results in data deletion upon each creation, but you can switch to 'update' or 'create-only' if you intend to persist data across multiple application executions.
3. Make sure PostgreSQL server is running.
4. Run the application.
5. Access the application through the provided URL.

Getting stared with docker (docker required)

1. Clone the repository.
2. Use "docker compose up" in project directory to start multi-container application.
3. Access the application through the provided URL.

## PostgreSQL Database Diagram 

![baza](https://github.com/user-attachments/assets/c67d6dc4-31ba-49a1-9fd1-519f51cc01dd)

## Technologies Used

- Java
- **Spring**
    - security
    - data-jpa
- Project Lombok
- Maven
- Vaadin
- PostgreSQL
- Git

# Screenshots

![main](https://github.com/user-attachments/assets/dc793f19-9f8b-4ebe-8a61-57fe9e84dd67)

![movie](https://github.com/user-attachments/assets/edf4716c-77f5-410c-84e3-4654020a66ae)
