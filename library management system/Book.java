package model;

import java.time.LocalDateTime;

/**
 * Model representing a book in the library.
 */
public class Book {

    private int           bookId;
    private String        title;
    private String        author;
    private String        isbn;
    private String        genre;
    private int           totalCopies;
    private int           availableCopies;
    private LocalDateTime addedDate;

    // ── Constructors ────────────────────────────────────────────────────────────

    public Book() {}

    /** Constructor for inserting a new book (no ID yet). */
    public Book(String title, String author, String isbn, String genre, int totalCopies) {
        this.title           = title;
        this.author          = author;
        this.isbn            = isbn;
        this.genre           = genre;
        this.totalCopies     = totalCopies;
        this.availableCopies = totalCopies; // all copies available initially
    }

    // ── Getters & Setters ────────────────────────────────────────────────────────

    public int           getBookId()          { return bookId; }
    public void          setBookId(int id)    { this.bookId = id; }

    public String        getTitle()           { return title; }
    public void          setTitle(String t)   { this.title = t; }

    public String        getAuthor()          { return author; }
    public void          setAuthor(String a)  { this.author = a; }

    public String        getIsbn()            { return isbn; }
    public void          setIsbn(String i)    { this.isbn = i; }

    public String        getGenre()           { return genre; }
    public void          setGenre(String g)   { this.genre = g; }

    public int           getTotalCopies()     { return totalCopies; }
    public void          setTotalCopies(int c){ this.totalCopies = c; }

    public int           getAvailableCopies() { return availableCopies; }
    public void          setAvailableCopies(int c) { this.availableCopies = c; }

    public LocalDateTime getAddedDate()       { return addedDate; }
    public void          setAddedDate(LocalDateTime d) { this.addedDate = d; }

    public boolean       isAvailable()        { return availableCopies > 0; }

    // ── toString ─────────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return String.format(
            "Book{id=%d, title='%s', author='%s', isbn='%s', genre='%s', available=%d/%d}",
            bookId, title, author, isbn, genre, availableCopies, totalCopies
        );
    }
}
