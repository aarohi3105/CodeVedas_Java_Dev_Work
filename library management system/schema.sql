-- ============================================
-- Library Management System - Database Schema
-- ============================================

CREATE DATABASE IF NOT EXISTS library_db;
USE library_db;

-- ============================================
-- TABLE: Books
-- ============================================
CREATE TABLE IF NOT EXISTS Books (
    book_id       INT AUTO_INCREMENT PRIMARY KEY,
    title         VARCHAR(255) NOT NULL,
    author        VARCHAR(255) NOT NULL,
    isbn          VARCHAR(20)  UNIQUE NOT NULL,
    genre         VARCHAR(100),
    total_copies  INT          NOT NULL DEFAULT 1,
    available_copies INT       NOT NULL DEFAULT 1,
    added_date    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_copies CHECK (available_copies >= 0 AND available_copies <= total_copies)
);

-- ============================================
-- TABLE: Users
-- ============================================
CREATE TABLE IF NOT EXISTS Users (
    user_id       INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    email         VARCHAR(255) UNIQUE NOT NULL,
    phone         VARCHAR(15),
    membership_date TIMESTAMP  DEFAULT CURRENT_TIMESTAMP,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE
);

-- ============================================
-- TABLE: Transactions
-- ============================================
CREATE TABLE IF NOT EXISTS Transactions (
    transaction_id   INT AUTO_INCREMENT PRIMARY KEY,
    user_id          INT         NOT NULL,
    book_id          INT         NOT NULL,
    borrow_date      TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    due_date         TIMESTAMP   NOT NULL,
    return_date      TIMESTAMP   NULL,
    status           ENUM('BORROWED', 'RETURNED', 'OVERDUE') DEFAULT 'BORROWED',
    CONSTRAINT fk_user  FOREIGN KEY (user_id)  REFERENCES Users(user_id)  ON DELETE CASCADE,
    CONSTRAINT fk_book  FOREIGN KEY (book_id)  REFERENCES Books(book_id)  ON DELETE CASCADE
);

-- ============================================
-- INDEXES for Performance
-- ============================================
CREATE INDEX idx_transactions_user   ON Transactions(user_id);
CREATE INDEX idx_transactions_book   ON Transactions(book_id);
CREATE INDEX idx_transactions_status ON Transactions(status);
CREATE INDEX idx_books_isbn          ON Books(isbn);
CREATE INDEX idx_users_email         ON Users(email);

-- ============================================
-- SAMPLE DATA
-- ============================================
INSERT INTO Books (title, author, isbn, genre, total_copies, available_copies) VALUES
('The Great Gatsby',        'F. Scott Fitzgerald', '978-0743273565', 'Fiction',         3, 3),
('To Kill a Mockingbird',   'Harper Lee',          '978-0061935466', 'Fiction',         2, 2),
('Clean Code',              'Robert C. Martin',    '978-0132350884', 'Technology',      4, 4),
('Design Patterns',         'Gang of Four',        '978-0201633610', 'Technology',      2, 2),
('Sapiens',                 'Yuval Noah Harari',   '978-0062316097', 'Non-Fiction',     3, 3),
('1984',                    'George Orwell',       '978-0451524935', 'Dystopian',       5, 5),
('The Pragmatic Programmer','David Thomas',        '978-0135957059', 'Technology',      3, 3),
('Educated',                'Tara Westover',       '978-0399590504', 'Memoir',          2, 2);

INSERT INTO Users (name, email, phone) VALUES
('Alice Johnson',  'alice@example.com',  '555-1001'),
('Bob Smith',      'bob@example.com',    '555-1002'),
('Carol Williams', 'carol@example.com',  '555-1003'),
('David Brown',    'david@example.com',  '555-1004');
