package model;

import java.time.LocalDateTime;

/**
 * Model representing a borrow/return transaction.
 */
public class Transaction {

    public enum Status { BORROWED, RETURNED, OVERDUE }

    private int           transactionId;
    private int           userId;
    private int           bookId;
    private LocalDateTime borrowDate;
    private LocalDateTime dueDate;
    private LocalDateTime returnDate;   // null when still borrowed
    private Status        status;

    // ── Constructors ────────────────────────────────────────────────────────────

    public Transaction() {}

    /** Constructor for creating a new borrow transaction. */
    public Transaction(int userId, int bookId, LocalDateTime dueDate) {
        this.userId     = userId;
        this.bookId     = bookId;
        this.borrowDate = LocalDateTime.now();
        this.dueDate    = dueDate;
        this.status     = Status.BORROWED;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────────

    public int           getTransactionId()       { return transactionId; }
    public void          setTransactionId(int id) { this.transactionId = id; }

    public int           getUserId()              { return userId; }
    public void          setUserId(int id)        { this.userId = id; }

    public int           getBookId()              { return bookId; }
    public void          setBookId(int id)        { this.bookId = id; }

    public LocalDateTime getBorrowDate()          { return borrowDate; }
    public void          setBorrowDate(LocalDateTime d) { this.borrowDate = d; }

    public LocalDateTime getDueDate()             { return dueDate; }
    public void          setDueDate(LocalDateTime d)    { this.dueDate = d; }

    public LocalDateTime getReturnDate()          { return returnDate; }
    public void          setReturnDate(LocalDateTime d) { this.returnDate = d; }

    public Status        getStatus()              { return status; }
    public void          setStatus(Status s)      { this.status = s; }

    public boolean       isOverdue() {
        return status == Status.BORROWED && LocalDateTime.now().isAfter(dueDate);
    }

    // ── toString ─────────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return String.format(
            "Transaction{id=%d, userId=%d, bookId=%d, borrowed=%s, due=%s, returned=%s, status=%s}",
            transactionId, userId, bookId, borrowDate, dueDate,
            returnDate != null ? returnDate.toString() : "N/A", status
        );
    }
}
