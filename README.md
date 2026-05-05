# Network-Based Student Management System

A multi-threaded Client-Server application built with Java Swing and MySQL. This system allows a user to perform CRUD (Create, Read, Update, Delete) operations on student records over a network connection.

## Features
- **Client-Server Architecture:** Centralized server handling multiple client connections concurrently.
- **Java Swing GUI:** A responsive desktop interface with modern styling.
- **Advanced Filtering & Sorting:** Search by Name/Email, Sort by CGPA/Name, and Filter by minimum CGPA or Course.
- **MySQL Database:** Persistent storage for all student records.

## Prerequisites
Before running this application, you must have the following installed on your machine:
- Java Development Kit (JDK 8 or higher)
- MySQL Server

## Setup Instructions

### 1. Database Setup
The application requires a MySQL database to function. Run the following SQL script in your MySQL client (like MySQL Workbench) to set it up:

```sql
CREATE DATABASE IF NOT EXISTS student_db;
USE student_db;

CREATE TABLE IF NOT EXISTS students (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    course VARCHAR(100) NOT NULL,
    cgpa DOUBLE NOT NULL
);
```

### 2. Configure Credentials
Open `src/db/DBConnection.java` and update the database credentials to match your local MySQL configuration:
```java
private static final String URL      = "jdbc:mysql://localhost:3306/student_db";
private static final String USER     = "root";           // your username
private static final String PASSWORD = "#tanyasql123#";  // your password
```

## How to Run
1. Make sure your MySQL Server is running in the background.
2. Compile the project (making sure to include the `mysql-connector-java` library in your classpath).
3. Start the Server: Run `server.Server`. It will listen for incoming connections on port 5000.
4. Start the Client: Run `gui.StudentManagementGUI`.

*Note: The client is currently configured to connect to `localhost`. If the server is on a different machine, update `HOST` in `client.ClientConnection`.*
