package model;

import java.time.LocalDateTime;

/**
 * Model representing a library member/user.
 */
public class User {

    private int           userId;
    private String        name;
    private String        email;
    private String        phone;
    private LocalDateTime membershipDate;
    private boolean       isActive;

    // ── Constructors ────────────────────────────────────────────────────────────

    public User() {}

    /** Constructor for creating a new user. */
    public User(String name, String email, String phone) {
        this.name     = name;
        this.email    = email;
        this.phone    = phone;
        this.isActive = true;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────────

    public int           getUserId()           { return userId; }
    public void          setUserId(int id)     { this.userId = id; }

    public String        getName()             { return name; }
    public void          setName(String n)     { this.name = n; }

    public String        getEmail()            { return email; }
    public void          setEmail(String e)    { this.email = e; }

    public String        getPhone()            { return phone; }
    public void          setPhone(String p)    { this.phone = p; }

    public LocalDateTime getMembershipDate()   { return membershipDate; }
    public void          setMembershipDate(LocalDateTime d) { this.membershipDate = d; }

    public boolean       isActive()            { return isActive; }
    public void          setActive(boolean a)  { this.isActive = a; }

    // ── toString ─────────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return String.format(
            "User{id=%d, name='%s', email='%s', phone='%s', active=%b}",
            userId, name, email, phone, isActive
        );
    }
}
