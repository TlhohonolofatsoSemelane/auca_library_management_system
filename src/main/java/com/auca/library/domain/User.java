package com.auca.library.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"username"})
})
public class User extends Person {

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password; // Raw password for simplicity in class stack

    @Enumerated(EnumType.STRING)
    @Column(name = "membership_type")
    private MembershipType membershipType;

    @Column(name = "membership_approved")
    private boolean membershipApproved = false;

    @Column(name = "active_borrows_count")
    private int activeBorrowsCount = 0;

    // Constructors
    public User() {}

    public User(String firstName, String lastName, String username, String password) {
        this.setFirstName(firstName);
        this.setLastName(lastName);
        this.username = username;
        this.password = password;
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public MembershipType getMembershipType() { return membershipType; }
    public void setMembershipType(MembershipType membershipType) { this.membershipType = membershipType; }
    public boolean isMembershipApproved() { return membershipApproved; }
    public void setMembershipApproved(boolean membershipApproved) { this.membershipApproved = membershipApproved; }
    public int getActiveBorrowsCount() { return activeBorrowsCount; }
    public void setActiveBorrowsCount(int activeBorrowsCount) { this.activeBorrowsCount = activeBorrowsCount; }
}