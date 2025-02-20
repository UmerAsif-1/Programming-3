# Secure Data Storage and Retrieval Server

This project implements a robust and secure server application designed for managing structured data records. It provides a secure HTTPS endpoint for clients to store, retrieve, and manage data, emphasizing data integrity and user authentication.

## Project Overview

The core purpose of this application is to offer a reliable backend service for applications requiring persistent data storage and secure access. It is built using Java and leverages an SQLite database for its persistence layer, making it suitable for applications needing local file-based data storage.

Key features include:

* **Data Integrity:** Data records are stored in a structured format, ensuring consistency and reliability.
* **Secure Access Control:** User authentication is implemented to protect sensitive data from unauthorized access.
* **Data Persistence:** An SQLite database ensures that data is stored persistently across server restarts.
* **Efficient Data Handling:** The server efficiently handles data storage and retrieval requests, optimizing for performance.
* **Secure Communication:** HTTPS ensures that all communication between the server and clients is encrypted and secure.
* **Standardized Data Exchange:** JSON is used for data exchange, enabling easy integration with various client applications.
* **Timestamp Management:** Timestamps are managed to ensure accurate tracking of record creation and modification times.
* **Modular Design:** The application is designed with modularity in mind, separating concerns into distinct components for maintainability and scalability.

## Architecture and Design

The application follows a layered architecture, separating concerns into distinct components:

* **Data Model Layer:**
    * Defines the structure of data records and user accounts using Java classes (e.g., `ObservationRecord`).
* **Data Access Layer (DAL):**
    * Encapsulates all database interactions, providing a clean interface for data storage and retrieval (`MessageDatabase`).
* **Authentication Layer:**
    * Handles user authentication and registration, ensuring secure access to data (`UserAuthenticator`, `RegistrationHandler`).
* **Server Layer:**
    * Manages HTTP(S) requests and responses, coordinating the interactions between other layers (`Server`).

This design promotes maintainability, testability, and scalability.

## Functionality

The server provides the following functionalities:

* **Data Record Management:**
    * Clients can submit new data records via POST requests to the `/datarecord` endpoint.
    * Clients can retrieve data records via GET requests to the `/datarecord` endpoint.
* **User Management:**
    * New users can register via POST requests to the `/registration` endpoint.
    * User authentication is required to access data record management endpoints.
* **Data Exchange:**
    * All data exchange is performed using JSON, ensuring compatibility with various client applications.
* **Security:**
    * HTTPS ensures secure communication between clients and the server.
    * User authentication protects data from unauthorized access.
* **Database Operations:**
    * The Database is initialized when the server is first run, and the tables are created at that time.
    * All database operations are handled via JDBC.

## Use Cases

This server application is suitable for a wide range of use cases, including:

* Backend for mobile or web applications requiring persistent data storage.
* Data management systems for small to medium-sized projects.
* Applications requiring secure data storage and user authentication.

## Future Enhancements

* Implement more advanced authentication mechanisms (e.g., OAuth 2.0).
* Add support for other database systems (e.g., PostgreSQL, MySQL).
* Implement data validation and input sanitization.
* Add logging and monitoring capabilities.

## Technologies

* **Java:** The primary language for the server application.
* **SQLite:** A lightweight, file-based database.
* **`com.sun.net.httpserver`:** Java's built-in HTTP(S) server.
* **`org.json`:** A Java library for JSON processing.
* **Maven:** A build automation tool.
* **JDBC:** Java Database Connectivity.
* **SSL/TLS:** Secure Sockets Layer/Transport Layer Security.
