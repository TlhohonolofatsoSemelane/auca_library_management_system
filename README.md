# AUCA Library Management System

A Java-based Library Management System built with **Hibernate/JPA**, **PostgreSQL**, **Maven**, and **JUnit**.  
The system manages users, memberships, books, shelves, rooms, borrowing records, locations, and late fee calculations.

This project focuses on clean entity relationships, service-layer business logic, database persistence, and automated testing.

---

## What We Did

We built and tested a complete backend library management system with the following features:

- User registration and membership management
- Province and location-based user tracking
- Book, shelf, and room inventory management
- Shelf-to-room assignment
- Book-to-shelf assignment with category validation
- Borrowing records management
- Borrow limit validation based on membership type
- Room-based book counting
- Finding the room with the fewest books
- Late fee calculation based on membership type
- Hibernate session and transaction handling
- Full automated testing using JUnit

---

## Technologies Used

| **Technology** | **Purpose** |
|:---|:---|
| Java | Main programming language |
| Hibernate / JPA | ORM and database persistence |
| PostgreSQL | Relational database |
| Maven | Dependency management and build tool |
| JUnit | Unit and integration testing |
| pgAdmin | Database visualization and management |
| HikariCP | Database connection pooling |

---

## Project Screenshots

### Database Schema and Tables

The screenshot below shows the generated PostgreSQL database tables in pgAdmin.

<img width="958" height="539" alt="image" src="https://github.com/user-attachments/assets/ff09e8cf-d1be-41d1-9c64-078b9f7f4f80" />


---

### Test Suite Results

The screenshot below shows the successful execution of the automated test suite.

<img width="958" height="539" alt="image" src="https://github.com/user-attachments/assets/f6e96fd6-f18c-4018-a154-03451d0fc840" />


---

### Test Coverage

The automated test suite verifies the main business logic of the system, including:

User registration
Membership validation
Province and location lookups
Book creation
Shelf creation
Room creation
Shelf assignment to room
Book assignment to shelf
Book category validation
Borrowing record creation
Borrow limit checking
Room-based book counting
Room inventory optimization
Late fee calculation
Hibernate session and transaction handling

---




