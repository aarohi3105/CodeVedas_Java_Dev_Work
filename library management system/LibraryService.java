package service;

import dao.BookDAO;
import dao.TransactionDAO;
import dao.UserDAO;
import model.Book;
import model.Transaction;
import model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Service layer that orchestrates DAO calls and enforces business rules:
 *  - A user may have at most 3 books borrowed simultaneously.
 *  - Deactivated users cannot borrow books.
 *  - Default loan period is 14 days.
 */
public class LibraryService {

    private static final int MAX_BORROW_LIMIT = 3;
    private static final int DEFAULT_LOAN_DAYS = 14;

    private final BookDAO        bookDAO        = new BookDAO();
    private final UserDAO        userDAO        = new UserDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    // ══════════════════════════════════════════════════════════════════════════════
    // BOOK OPERATIONS
    // ══════════════════════════════════════════════════════════════════════════════

    public int addBook(Book book) throws SQLException {
        return bookDAO.addBook(book);
    }

    public Optional<Book> getBookById(int id) throws SQLException {
        return bookDAO.getBookById(id);
    }

    public List<Book> getAllBooks() throws SQLException {
        return bookDAO.getAllBooks();
    }

    public List<Book> getAvailableBooks() throws SQLException {
        return bookDAO.getAvailableBooks();
    }

    public List<Book> searchBooks(String keyword) throws SQLException {
        return bookDAO.searchByTitle(keyword);
    }

    public boolean updateBook(Book book) throws SQLException {
        return bookDAO.updateBook(book);
    }

    public boolean deleteBook(int bookId) throws SQLException {
        return bookDAO.deleteBook(bookId);
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // USER OPERATIONS
    // ══════════════════════════════════════════════════════════════════════════════

    public int registerUser(User user) throws SQLException {
        return userDAO.addUser(user);
    }

    public Optional<User> getUserById(int id) throws SQLException {
        return userDAO.getUserById(id);
    }

    public List<User> getAllUsers() throws SQLException {
        return userDAO.getAllUsers();
    }

    public boolean updateUser(User user) throws SQLException {
        return userDAO.updateUser(user);
    }

    public boolean deactivateUser(int userId) throws SQLException {
        return userDAO.deactivateUser(userId);
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // BORROW / RETURN
    // ══════════════════════════════════════════════════════════════════════════════

    /**
     * Lends a book to a user.
     *
     * Business rules checked:
     *  1. User must exist and be active.
     *  2. Book must exist and have available copies.
     *  3. User must not exceed MAX_BORROW_LIMIT.
     */
    public Transaction borrowBook(int userId, int bookId) throws SQLException {
        return borrowBook(userId, bookId, DEFAULT_LOAN_DAYS);
    }

    public Transaction borrowBook(int userId, int bookId, int loanDays) throws SQLException {
        // Rule 1 – user active?
        User user = userDAO.getUserById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User ID " + userId + " not found."));
        if (!user.isActive()) {
            throw new IllegalStateException("User account is deactivated.");
        }

        // Rule 2 – book available?
        Book book = bookDAO.getBookById(bookId)
            .orElseThrow(() -> new IllegalArgumentException("Book ID " + bookId + " not found."));
        if (!book.isAvailable()) {
            throw new IllegalStateException("No copies of '" + book.getTitle() + "' are available.");
        }

        // Rule 3 – borrow limit?
        long current = transactionDAO.getActiveBorrowsByUser(userId).size();
        if (current >= MAX_BORROW_LIMIT) {
            throw new IllegalStateException(
                "User has reached the maximum borrow limit of " + MAX_BORROW_LIMIT + " books.");
        }

        return transactionDAO.borrowBook(userId, bookId, loanDays);
    }

    /**
     * Processes a book return using the transaction ID.
     */
    public Transaction returnBook(int transactionId) throws SQLException {
        return transactionDAO.returnBook(transactionId);
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // REPORTS
    // ══════════════════════════════════════════════════════════════════════════════

    public List<Transaction> getAllTransactions() throws SQLException {
        return transactionDAO.getAllTransactions();
    }

    public List<Transaction> getUserBorrowHistory(int userId) throws SQLException {
        return transactionDAO.getTransactionsByUser(userId);
    }

    public List<Transaction> getActiveBorrows(int userId) throws SQLException {
        return transactionDAO.getActiveBorrowsByUser(userId);
    }

    public List<Transaction> getOverdueTransactions() throws SQLException {
        return transactionDAO.getOverdueTransactions();
    }

    public int markOverdueBooks() throws SQLException {
        return transactionDAO.markOverdueTransactions();
    }
}
