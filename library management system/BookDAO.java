package dao;

import model.Book;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Books.
 * Implements full CRUD operations using JDBC.
 */
public class BookDAO {

    // ── CREATE ───────────────────────────────────────────────────────────────────

    /**
     * Inserts a new book into the database.
     * @return the generated book_id, or -1 on failure.
     */
    public int addBook(Book book) throws SQLException {
        String sql = "INSERT INTO Books (title, author, isbn, genre, total_copies, available_copies) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getAuthor());
            ps.setString(3, book.getIsbn());
            ps.setString(4, book.getGenre());
            ps.setInt(5, book.getTotalCopies());
            ps.setInt(6, book.getAvailableCopies());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                int id = keys.getInt(1);
                book.setBookId(id);
                System.out.println("[BookDAO] Book added with ID: " + id);
                return id;
            }
        }
        return -1;
    }

    // ── READ ─────────────────────────────────────────────────────────────────────

    /** Retrieves all books. */
    public List<Book> getAllBooks() throws SQLException {
        String sql = "SELECT * FROM Books ORDER BY title";
        List<Book> books = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                books.add(mapRow(rs));
            }
        }
        return books;
    }

    /** Finds a book by its primary key. */
    public Optional<Book> getBookById(int bookId) throws SQLException {
        String sql = "SELECT * FROM Books WHERE book_id = ?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        }
        return Optional.empty();
    }

    /** Finds a book by ISBN. */
    public Optional<Book> getBookByIsbn(String isbn) throws SQLException {
        String sql = "SELECT * FROM Books WHERE isbn = ?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, isbn);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        }
        return Optional.empty();
    }

    /** Searches books by title (case-insensitive partial match). */
    public List<Book> searchByTitle(String keyword) throws SQLException {
        String sql = "SELECT * FROM Books WHERE LOWER(title) LIKE ? ORDER BY title";
        List<Book> books = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword.toLowerCase() + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) books.add(mapRow(rs));
        }
        return books;
    }

    /** Returns all books that currently have at least one available copy. */
    public List<Book> getAvailableBooks() throws SQLException {
        String sql = "SELECT * FROM Books WHERE available_copies > 0 ORDER BY title";
        List<Book> books = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) books.add(mapRow(rs));
        }
        return books;
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────────

    /** Updates all editable fields of a book. */
    public boolean updateBook(Book book) throws SQLException {
        String sql = "UPDATE Books SET title=?, author=?, isbn=?, genre=?, " +
                     "total_copies=?, available_copies=? WHERE book_id=?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getAuthor());
            ps.setString(3, book.getIsbn());
            ps.setString(4, book.getGenre());
            ps.setInt(5, book.getTotalCopies());
            ps.setInt(6, book.getAvailableCopies());
            ps.setInt(7, book.getBookId());
            int rows = ps.executeUpdate();
            System.out.println("[BookDAO] Updated " + rows + " book record(s).");
            return rows > 0;
        }
    }

    /**
     * Adjusts available_copies by delta (e.g., -1 when borrowing, +1 when returning).
     * Called internally during transactions — NOT auto-committed here.
     */
    public void adjustAvailableCopies(int bookId, int delta, Connection conn) throws SQLException {
        String sql = "UPDATE Books SET available_copies = available_copies + ? WHERE book_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setInt(2, bookId);
            ps.executeUpdate();
        }
    }

    // ── DELETE ───────────────────────────────────────────────────────────────────

    /** Deletes a book by ID (cascades to transactions via FK). */
    public boolean deleteBook(int bookId) throws SQLException {
        String sql = "DELETE FROM Books WHERE book_id = ?";
        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            int rows = ps.executeUpdate();
            System.out.println("[BookDAO] Deleted " + rows + " book record(s).");
            return rows > 0;
        }
    }

    // ── Helper ───────────────────────────────────────────────────────────────────

    private Book mapRow(ResultSet rs) throws SQLException {
        Book b = new Book();
        b.setBookId(rs.getInt("book_id"));
        b.setTitle(rs.getString("title"));
        b.setAuthor(rs.getString("author"));
        b.setIsbn(rs.getString("isbn"));
        b.setGenre(rs.getString("genre"));
        b.setTotalCopies(rs.getInt("total_copies"));
        b.setAvailableCopies(rs.getInt("available_copies"));
        Timestamp ts = rs.getTimestamp("added_date");
        if (ts != null) b.setAddedDate(ts.toLocalDateTime());
        return b;
    }
}
