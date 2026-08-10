# 📚 AUCA Library Management System

A robust, high-performance Library Management System built with **Java**, **Hibernate/JPA**, **PostgreSQL**, and **JUnit**. This system handles complex library operations—including multi-level location hierarchies, membership-tier validation, smart inventory allocation, and automatic late fee calculations—all verified by a comprehensive test suite.

---

## 🚀 Key Features & What We Did

We implemented and fully verified the core business logic of the library system:

*   **📍 Hierarchical Location Management:** Full geographic tracking from Province ➔ District ➔ Sector ➔ Cell ➔ Village to pinpoint user registration locations.
*   **👥 Tiered Membership & Borrow Limits:** Enforced strict borrowing limits based on membership tiers (**GOLD**: 5 books, **SILVER**: 3 books, **STRIVER**: 2 books) with manual approval flows.
*   **📦 Smart Inventory & Category Validation:** Room, shelf, and book management. Enforces strict validation ensuring books are only placed on shelves matching their specific category (e.g., *SCIENCE*, *FICTION*).
*   **📊 Room Optimization & Book Counting:** Real-time aggregation of book counts across multiple shelves in a room, with built-in optimization to locate the room with the fewest books.
*   **💰 Dynamic Late Fee Calculation:** Automated fee generation based on membership type (Gold: 50 RWF/day, Silver: 30 RWF/day, Striver: 10 RWF/day).
*   **⚡ Robust Session Management:** Solved complex Hibernate detached-entity and connection lifecycle issues (`LogicalConnectionManagedImpl is closed`) to ensure clean, transactional service layers.

---

## 📸 System Screenshots

### 🧪 Test Suite Execution
*Below is the execution of the full test suite showing all 33 test cases passing successfully:*

![Test Suite Execution](docs/screenshots/test-suite-pass.png)

### 🗄️ Database Schema & Tables
*The generated PostgreSQL database schema reflecting the entities and relationships:*

![Database Schema](docs/screenshots/database-schema.png)

*(Note: To display your screenshots, create a folder named `docs/screenshots/` in your project, save your images there as `test-suite-pass.png` and `database-schema.png`, and commit them to Git!)*

---

## 🛠️ Technology Stack

| Component | Technology | Version / Details |
| :--- | :--- | :--- |
| **Language** | Java | JDK 17+ |
| **ORM Framework** | Hibernate / JPA | v6.5.2.Final |
| **Database** | PostgreSQL | Local / Production instance |
| **Connection Pool** | HikariCP | High-performance pooling |
| **Testing Framework**| JUnit | v4.13.2 |
| **Build Tool** | Maven | Project lifecycle management |

---

## ⚙️ Configuration & Setup

### 1. Database Configuration
Ensure your PostgreSQL database is running and update your connection settings in `src/main/resources/hibernate.cfg.xml`:

```xml
<property name="connection.url">jdbc:postgresql://localhost:5432/auca_library</property>
<property name="connection.username">your_username</property>
<property name="connection.password">your_password</property>
