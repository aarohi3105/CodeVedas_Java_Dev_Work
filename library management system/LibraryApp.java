import model.Book;
import model.Transaction;
import model.User;
import service.LibraryService;
import util.DatabaseConnection;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * ╔══════════════════════════════════════════════════════╗
 * ║         Library Management System  — CLI             ║
 * ║   JDBC + MySQL  |  Transactions  |  CRUD             ║
 * ╚══════════════════════════════════════════════════════╝
 *
 * Entry point. Provides a console-based menu to exercise
 * every feature of the system.
 */
public class LibraryApp {

    private static final LibraryService service = new LibraryService();
    private static final Scanner        scanner = new Scanner(System.in);

    public static void main(String[] args) {
        printBanner();
        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt("Enter choice: ");
            switch (choice) {
                case 1  -> bookMenu();
                case 2  -> userMenu();
                case 3  -> transactionMenu();
                case 4  -> reportsMenu();
                case 0  -> running = false;
                default -> System.out.println("  ✖  Invalid option.");
            }
        }
        DatabaseConnection.closeConnection();
        System.out.println("\n  Goodbye! 📚\n");
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // MENUS
    // ══════════════════════════════════════════════════════════════════════════════

    static void printMainMenu() {
        System.out.println("""
            \n┌────────────────────────────────┐
            │       MAIN  MENU               │
            ├────────────────────────────────┤
            │  1 › Book Management           │
            │  2 › User Management           │
            │  3 › Borrow / Return           │
            │  4 › Reports                   │
            │  0 › Exit                      │
            └────────────────────────────────┘""");
    }

    // ── Book Menu ────────────────────────────────────────────────────────────────

    static void bookMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("""
                \n── BOOK MANAGEMENT ─────────────────
                  1 › Add Book
                  2 › View All Books
                  3 › Search by Title
                  4 › View Available Books
                  5 › Update Book
                  6 › Delete Book
                  0 › Back
                ────────────────────────────────────""");
            switch (readInt("Choice: ")) {
                case 1  -> addBook();
                case 2  -> listBooks(service.getAllBooks());
                case 3  -> searchBooks();
                case 4  -> listBooks(service.getAvailableBooks());
                case 5  -> updateBook();
                case 6  -> deleteBook();
                case 0  -> back = true;
                default -> System.out.println("  ✖  Invalid option.");
            }
        }
    }

    // ── User Menu ────────────────────────────────────────────────────────────────

    static void userMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("""
                \n── USER MANAGEMENT ─────────────────
                  1 › Register User
                  2 › View All Users
                  3 › Update User
                  4 › Deactivate User
                  0 › Back
                ────────────────────────────────────""");
            switch (readInt("Choice: ")) {
                case 1  -> registerUser();
                case 2  -> listUsers(service.getAllUsers());
                case 3  -> updateUser();
                case 4  -> deactivateUser();
                case 0  -> back = true;
                default -> System.out.println("  ✖  Invalid option.");
            }
        }
    }

    // ── Transaction Menu ─────────────────────────────────────────────────────────

    static void transactionMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("""
                \n── BORROW / RETURN ─────────────────
                  1 › Borrow a Book
                  2 › Return a Book
                  0 › Back
                ────────────────────────────────────""");
            switch (readInt("Choice: ")) {
                case 1  -> borrowBook();
                case 2  -> returnBook();
                case 0  -> back = true;
                default -> System.out.println("  ✖  Invalid option.");
            }
        }
    }

    // ── Reports Menu ─────────────────────────────────────────────────────────────

    static void reportsMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("""
                \n── REPORTS ─────────────────────────
                  1 › All Transactions
                  2 › User Borrow History
                  3 › Overdue Books
                  4 › Mark Overdue Transactions
                  0 › Back
                ────────────────────────────────────""");
            switch (readInt("Choice: ")) {
                case 1  -> listTransactions(service.getAllTransactions());
                case 2  -> userHistory();
                case 3  -> listTransactions(service.getOverdueTransactions());
                case 4  -> { int n = service.markOverdueBooks();
                             System.out.println("  ✔  Marked " + n + " overdue transaction(s)."); }
                case 0  -> back = true;
                default -> System.out.println("  ✖  Invalid option.");
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // ACTIONS
    // ══════════════════════════════════════════════════════════════════════════════

    static void addBook() {
        try {
            System.out.println("\n  [ Add Book ]");
            String title   = readStr("  Title   : ");
            String author  = readStr("  Author  : ");
            String isbn    = readStr("  ISBN    : ");
            String genre   = readStr("  Genre   : ");
            int    copies  = readInt("  Copies  : ");
            Book b = new Book(title, author, isbn, genre, copies);
            int id = service.addBook(b);
            System.out.println("  ✔  Book added — ID: " + id);
        } catch (Exception e) {
            System.out.println("  ✖  " + e.getMessage());
        }
    }

    static void searchBooks() {
        try {
            String kw = readStr("  Keyword: ");
            listBooks(service.searchBooks(kw));
        } catch (Exception e) {
            System.out.println("  ✖  " + e.getMessage());
        }
    }

    static void updateBook() {
        try {
            int id = readInt("  Book ID to update: ");
            Optional<Book> opt = service.getBookById(id);
            if (opt.isEmpty()) { System.out.println("  ✖  Book not found."); return; }
            Book b = opt.get();
            System.out.println("  Current: " + b);
            b.setTitle(readStr("  New title  [" + b.getTitle()  + "]: ", b.getTitle()));
            b.setAuthor(readStr("  New author [" + b.getAuthor() + "]: ", b.getAuthor()));
            b.setGenre(readStr("  New genre  [" + b.getGenre()  + "]: ", b.getGenre()));
            service.updateBook(b);
            System.out.println("  ✔  Book updated.");
        } catch (Exception e) {
            System.out.println("  ✖  " + e.getMessage());
        }
    }

    static void deleteBook() {
        try {
            int id = readInt("  Book ID to delete: ");
            boolean ok = service.deleteBook(id);
            System.out.println(ok ? "  ✔  Book deleted." : "  ✖  Book not found.");
        } catch (Exception e) {
            System.out.println("  ✖  " + e.getMessage());
        }
    }

    static void registerUser() {
        try {
            System.out.println("\n  [ Register User ]");
            String name  = readStr("  Name   : ");
            String email = readStr("  Email  : ");
            String phone = readStr("  Phone  : ");
            User u = new User(name, email, phone);
            int id = service.registerUser(u);
            System.out.println("  ✔  User registered — ID: " + id);
        } catch (Exception e) {
            System.out.println("  ✖  " + e.getMessage());
        }
    }

    static void updateUser() {
        try {
            int id = readInt("  User ID to update: ");
            Optional<User> opt = service.getUserById(id);
            if (opt.isEmpty()) { System.out.println("  ✖  User not found."); return; }
            User u = opt.get();
            u.setName(readStr("  New name  [" + u.getName()  + "]: ", u.getName()));
            u.setEmail(readStr("  New email [" + u.getEmail() + "]: ", u.getEmail()));
            u.setPhone(readStr("  New phone [" + u.getPhone() + "]: ", u.getPhone()));
            service.updateUser(u);
            System.out.println("  ✔  User updated.");
        } catch (Exception e) {
            System.out.println("  ✖  " + e.getMessage());
        }
    }

    static void deactivateUser() {
        try {
            int id = readInt("  User ID to deactivate: ");
            boolean ok = service.deactivateUser(id);
            System.out.println(ok ? "  ✔  User deactivated." : "  ✖  User not found.");
        } catch (Exception e) {
            System.out.println("  ✖  " + e.getMessage());
        }
    }

    static void borrowBook() {
        try {
            int userId = readInt("  User ID : ");
            int bookId = readInt("  Book ID : ");
            int days   = readInt("  Loan days (default 14): ");
            if (days <= 0) days = 14;
            Transaction t = service.borrowBook(userId, bookId, days);
            System.out.println("  ✔  Borrowed! Transaction ID: " + t.getTransactionId()
                + "  |  Due: " + t.getDueDate());
        } catch (Exception e) {
            System.out.println("  ✖  " + e.getMessage());
        }
    }

    static void returnBook() {
        try {
            int txnId = readInt("  Transaction ID: ");
            Transaction t = service.returnBook(txnId);
            System.out.println("  ✔  Returned! Recorded at: " + t.getReturnDate());
        } catch (Exception e) {
            System.out.println("  ✖  " + e.getMessage());
        }
    }

    static void userHistory() {
        try {
            int userId = readInt("  User ID: ");
            listTransactions(service.getUserBorrowHistory(userId));
        } catch (Exception e) {
            System.out.println("  ✖  " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // DISPLAY HELPERS
    // ══════════════════════════════════════════════════════════════════════════════

    static void listBooks(List<Book> books) {
        if (books.isEmpty()) { System.out.println("  (no books found)"); return; }
        System.out.printf("  %-4s  %-35s %-22s %-14s %-12s %s%n",
            "ID", "Title", "Author", "ISBN", "Genre", "Avail/Total");
        System.out.println("  " + "─".repeat(100));
        for (Book b : books) {
            System.out.printf("  %-4d  %-35s %-22s %-14s %-12s %d/%d%n",
                b.getBookId(), truncate(b.getTitle(), 33),
                truncate(b.getAuthor(), 20), b.getIsbn(),
                truncate(b.getGenre(), 10),
                b.getAvailableCopies(), b.getTotalCopies());
        }
    }

    static void listUsers(List<User> users) {
        if (users.isEmpty()) { System.out.println("  (no users found)"); return; }
        System.out.printf("  %-4s  %-25s %-30s %-12s %s%n",
            "ID", "Name", "Email", "Phone", "Active");
        System.out.println("  " + "─".repeat(80));
        for (User u : users) {
            System.out.printf("  %-4d  %-25s %-30s %-12s %s%n",
                u.getUserId(), u.getName(), u.getEmail(),
                u.getPhone(), u.isActive() ? "✔" : "✖");
        }
    }

    static void listTransactions(List<Transaction> txns) {
        if (txns.isEmpty()) { System.out.println("  (no transactions found)"); return; }
        System.out.printf("  %-5s %-6s %-6s %-20s %-20s %-20s %-10s%n",
            "TxnID","UserID","BookID","Borrowed","Due","Returned","Status");
        System.out.println("  " + "─".repeat(95));
        for (Transaction t : txns) {
            System.out.printf("  %-5d %-6d %-6d %-20s %-20s %-20s %-10s%n",
                t.getTransactionId(), t.getUserId(), t.getBookId(),
                t.getBorrowDate(), t.getDueDate(),
                t.getReturnDate() != null ? t.getReturnDate() : "—",
                t.getStatus());
        }
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // INPUT HELPERS
    // ══════════════════════════════════════════════════════════════════════════════

    static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return Integer.parseInt(scanner.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.println("  Enter a valid number."); }
        }
    }

    static String readStr(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    static String readStr(String prompt, String defaultVal) {
        System.out.print(prompt);
        String val = scanner.nextLine().trim();
        return val.isEmpty() ? defaultVal : val;
    }

    static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    static void printBanner() {
        System.out.println("""
            \n╔══════════════════════════════════════════╗
            ║   📚  Library Management System  📚      ║
            ║   Java + JDBC + MySQL                    ║
            ╚══════════════════════════════════════════╝""");
    }
}
