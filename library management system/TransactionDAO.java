package dao;

import model.Transaction;
import model.Transaction.Status;
import util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Transactions.
 *
 * Borrow and return operations use explicit JDBC transactions
 * (setAutoCommit(false) / commit / rollback) to guarantee
 * atomicity across the Transactions and Books tables.
 */
public class TransactionDAO {

    private final BookDAO bookDAO = new BookDAO();

    // ── BORROW ───────────────────────────────────────────────────────────────────

    /**
     * Records a new borrow transaction.
     * Atomically:
     *   1. Inserts a row in Transactions
     *   2. Decrements available_copies in Books
     *
     * @param userId      the borrowing user
     * @param bookId      the book being borrowed
     * @param loanDays    number of days before due
     * @return the new Transaction object
     * @throws SQLException      if a DB error occurs
     * @throws IllegalStateException if no copies are available
     */
    public Transaction borrowBook(int userId, int bookId, int loanDays) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();

        // ── Disable auto-commit to start a JDBC transaction ──────────────────
        conn.setAutoCommit(false);
        try {
            // 1. Check availability (within the same transaction / locking row)
            String checkSql = "SELECT available_copies FROM Books WHERE book_id = ? FOR UPDATE";
            int available;
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, bookId);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) throw new IllegalArgumentException("Book ID " + bookId + " not found.");
                available = rs.getInt("available_copies");
            }
            if (available <= 0) {
                throw new IllegalStateException("No copies available for book ID " + bookId + ".");
            }

            // 2. Insert transaction record
            LocalDateTime now     = LocalDateTime.now();
            LocalDateTime dueDate = now.plusDays(loanDays);

            String insertSql =
                "INSERT INTO Transactions (user_id, book_id, borrow_date, due_date, status) " +
                "VALUES (?, ?, ?, ?, 'BORROWED')";
            int transactionId;
            try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, userId);
                ps.setInt(2, bookId);
                ps.setTimestamp(3, Timestamp.valueOf(now));
                ps.setTimestamp(4, Timestamp.valueOf(dueDate));
                ps.executeUpdate();

                ResultSet keys = ps.getGeneratedKeys();
                if (!keys.next()) throw new SQLException("Failed to retrieve transaction ID.");
                transactionId = keys.getInt(1);
            }

            // 3. Decrement available_copies
            bookDAO.adjustAvailableCopies(bookId, -1, conn);

            // ── Commit both changes atomically ────────────────────────────────
            conn.commit();
            System.out.println("[Txn] BORROW committed — Transaction ID: " + transactionId);

            Transaction txn = new Transaction(userId, bookId, dueDate);
            txn.setTransactionId(transactionId);
            txn.setBorrowDate(now);
            return txn;

        } catch (Exception e) {
            conn.rollback();
            System.err.println("[Txn] BORROW rolled back: " + e.getMessage());
            throw e instanceof SQLException ? (SQLException) e : new SQLException(e);
        } finally {
            conn.setAutoCommit(true); // restore default
        }
    }

    // ── RETURN ───────────────────────────────────────────────────────────────────

    /**
     * Records a book return.
     * Atomically:
     *   1. Updates the transaction status to RETURNED and sets return_date
     *   2. Increments available_copies in Books
     *
     * @param transactionId the ID of the active borrow transaction
     * @return the updated Transaction object
     */
    public Transaction returnBook(int transactionId) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();

        conn.setAutoCommit(false);
        try {
            // 1. Fetch the open transaction
            String fetchSql =
                "SELECT * FROM Transactions WHERE transaction_id = ? AND status = 'BORROWED' FOR UPDATE";
            int bookId;
            try (PreparedStatement ps = conn.prepareStatement(fetchSql)) {
                ps.setInt(1, transactionId);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    throw new IllegalArgumentException(
                        "No active borrow found for transaction ID " + transactionId + ".");
                }
                bookId = rs.getInt("book_id");
            }

            // 2. Mark transaction as returned
            LocalDateTime returnedAt = LocalDateTime.now();
            String updateSql =
                "UPDATE Transactions SET return_date = ?, status = 'RETURNED' " +
                "WHERE transaction_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setTimestamp(1, Timestamp.valueOf(returnedAt));
                ps.setInt(2, transactionId);
                ps.executeUpdate();
            }

            // 3. Increment available copies
            bookDAO.adjustAvailableCopies(bookId, +1, conn);

            conn.commit();
            System.out.println("[Txn] RETURN committed — Transaction ID: " + transactionId);

            return getTransactionById(transactionId).orElseThrow();

        } catch (Exception e) {
            conn.rollback();
            System.err.println("[Txn] RETURN rolled back: " + e.getMessage());
            throw e instanceof SQLException ? (SQLException) e : new SQLException(e);
        } finally {
            conn.setAutoCommit(true);
        }
    }

    // ── READ ─────────────────────────────────────────────────────────────────────

    /** All transactions (newest first). */
    public List<Transaction> getAllTransactions() throws SQLException {
        String sql = "SELECT * FROM Transactions ORDER BY borrow_date DESC";
        return fetchList(sql);
    }

    /** All active (not yet returned) borrows for a user. */
    public List<Transaction> getActiveBorrowsByUser(int userId) throws SQLException {
        String sql = "SELECT * FROM Transactions WHERE user_id = ? AND status = 'BORROWED'";
        return fetchList(sql, userId);
    }

    /** Full history for a user. */
    public List<Transaction> getTransactionsByUser(int userId) throws SQLException {
        String sql = "SELECT * FROM Transactions WHERE user_id = ? ORDER BY borrow_date DESC";
        return fetchList(sql, userId);
    }

    /** All overdue borrows (past due date, not returned). */
    public List<Transaction> getOverdueTransactions() throws SQLException {
        String sql = "SELECT * FROM Transactions " +
                     "WHERE status = 'BORROWED' AND due_date < NOW()";
        return fetchList(sql);
    }

    /** Find a single transaction by ID. */
    public Optional<Transaction> getTransactionById(int transactionId) throws SQLException {
        String sql = "SELECT * FROM Transactions WHERE transaction_id = ?";
        List<Transaction> result = fetchList(sql, transactionId);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    /** Marks all overdue transactions in the DB. Useful for batch jobs. */
    public int markOverdueTransactions() throws SQLException {
        String sql = "UPDATE Transactions SET status = 'OVERDUE' " +
                     "WHERE status = 'BORROWED' AND due_date < NOW()";
        Connection conn = DatabaseConnection.getConnection();
        try (Statement st = conn.createStatement()) {
            int rows = st.executeUpdate(sql);
            System.out.println("[Txn] Marked " + rows + " transaction(s) as OVERDUE.");
            return rows;
        }
    }

    // ── Private helpers ──────────────────────────────────────────────────────────

    private List<Transaction> fetchList(String sql, Object... params) throws SQLException {
        List<Transaction> list = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    private Transaction mapRow(ResultSet rs) throws SQLException {
        Transaction t = new Transaction();
        t.setTransactionId(rs.getInt("transaction_id"));
        t.setUserId(rs.getInt("user_id"));
        t.setBookId(rs.getInt("book_id"));

        Timestamp borrow = rs.getTimestamp("borrow_date");
        if (borrow != null) t.setBorrowDate(borrow.toLocalDateTime());

        Timestamp due = rs.getTimestamp("due_date");
        if (due != null) t.setDueDate(due.toLocalDateTime());

        Timestamp ret = rs.getTimestamp("return_date");
        if (ret != null) t.setReturnDate(ret.toLocalDateTime());

        t.setStatus(Status.valueOf(rs.getString("status")));
        return t;
    }
}
