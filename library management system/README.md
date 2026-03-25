# Library Management System — Java + JDBC + MySQL

A fully featured Library Management System built with **Java**, **JDBC**, and **MySQL**.
It demonstrates clean layered architecture (Model → DAO → Service → CLI) with
proper **JDBC transactions** for borrow/return operations.

---

## Project Structure

```
LibraryManagement/
├── sql/
│   └── schema.sql            ← Database schema + sample data
└── src/
    ├── LibraryApp.java        ← Entry point (interactive CLI)
    ├── model/
    │   ├── Book.java
    │   ├── User.java
    │   └── Transaction.java
    ├── dao/
    │   ├── BookDAO.java       ← CRUD for Books
    │   ├── UserDAO.java       ← CRUD for Users
    │   └── TransactionDAO.java← Borrow / Return with JDBC transactions
    ├── service/
    │   └── LibraryService.java← Business-rule layer
    └── util/
        └── DatabaseConnection.java ← Singleton JDBC connection
```

---

## Prerequisites

| Tool              | Version |
| ----------------- | ------- |
| JDK               | 17+     |
| MySQL Server      | 8.0+    |
| MySQL Connector/J | 8.x     |

Download [mysql-connector-j-8.x.jar](https://dev.mysql.com/downloads/connector/j/).

---

## Setup

### 1 — Create the database

```bash
mysql -u root -p < sql/schema.sql
```

This creates `library_db` with `Books`, `Users`, and `Transactions` tables,
plus sample data (8 books, 4 users).

### 2 — Update credentials

Edit `src/util/DatabaseConnection.java`:

```java
private static final String USER     = "root";       // ← your MySQL user
private static final String PASSWORD = "password";   // ← your MySQL password
```

### 3 — Compile

```bash
javac -cp ".:mysql-connector-j-8.x.jar" \
    src/util/DatabaseConnection.java \
    src/model/*.java \
    src/dao/*.java \
    src/service/LibraryService.java \
    src/LibraryApp.java \
    -d out/
```

_(On Windows replace `:` with `;` in the classpath.)_

### 4 — Run

```bash
java -cp "out:mysql-connector-j-8.x.jar" LibraryApp
```

---

## Features

### Books (CRUD)

| Operation | Detail                                         |
| --------- | ---------------------------------------------- |
| Add       | Insert with title, author, ISBN, genre, copies |
| View All  | Full catalogue                                 |
| Search    | Case-insensitive partial title match           |
| Available | Books with ≥ 1 copy available                  |
| Update    | Edit title, author, genre                      |
| Delete    | Cascades to transactions                       |

### Users (CRUD)

| Operation  | Detail                    |
| ---------- | ------------------------- |
| Register   | Name, email, phone        |
| View All   | All registered members    |
| Update     | Edit name / email / phone |
| Deactivate | Soft-disable account      |

### Transactions (JDBC Atomic Operations)

| Operation | Detail                                                   |
| --------- | -------------------------------------------------------- |
| Borrow    | Decrements `available_copies`; checks user limit (max 3) |
| Return    | Updates status; increments `available_copies`            |
| History   | Per-user borrow log                                      |
| Overdue   | Lists / marks overdue borrows                            |

---

## JDBC Transaction Flow

```
borrowBook()
  ├── setAutoCommit(false)
  ├── SELECT available_copies … FOR UPDATE   ← pessimistic lock
  ├── INSERT INTO Transactions …
  ├── UPDATE Books SET available_copies - 1
  ├── commit()
  └── (on any error) rollback()

returnBook()
  ├── setAutoCommit(false)
  ├── SELECT … FOR UPDATE
  ├── UPDATE Transactions SET status='RETURNED', return_date=NOW()
  ├── UPDATE Books SET available_copies + 1
  ├── commit()
  └── (on any error) rollback()
```

---

## Business Rules

- Maximum **3 books** borrowed per user simultaneously.
- Deactivated users **cannot** borrow books.
- Default loan period: **14 days** (configurable per borrow call).
- `markOverdueTransactions()` can be run as a scheduled batch job.

---

## Sample CLI Session

```
╔══════════════════════════════════════════╗
║     Library Management System         ║
║   Java + JDBC + MySQL                    ║
╚══════════════════════════════════════════╝

┌────────────────────────────────┐
│       MAIN  MENU               │
├────────────────────────────────┤
│  1 › Book Management           │
│  2 › User Management           │
│  3 › Borrow / Return           │
│  4 › Reports                   │
│  0 › Exit                      │
└────────────────────────────────┘
Enter choice: 3

── BORROW / RETURN ─────────────────
  1 › Borrow a Book
  2 › Return a Book
  0 › Back
────────────────────────────────────
Choice: 1
  User ID : 1
  Book ID : 3
  Loan days (default 14): 14
[Txn] BORROW committed — Transaction ID: 1
  ✔  Borrowed! Transaction ID: 1  |  Due: 2026-04-08T10:30:00
```
