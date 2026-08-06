package com.auca.library.service;

import com.auca.library.dao.UserDao;
import com.auca.library.domain.Location;
import com.auca.library.domain.MembershipType;
import com.auca.library.domain.User;
import java.util.UUID;

public class UserService {
    private final UserDao userDao = new UserDao();

    // Requirement 3: Person ID -> Province Name
    public String getProvinceNameByPersonId(UUID personId) {
        User user = userDao.findById(personId);
        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }
        Location village = user.getVillage();
        if (village == null) {
            throw new IllegalArgumentException("User does not have a registered location.");
        }

        Location current = village;
        while (current.getParent() != null) {
            current = current.getParent();
        }
        return current.getName();
    }

    // Requirement 4: Authenticate User
    public boolean authenticate(String username, String rawPassword) {
        if (username == null || username.trim().isEmpty() || rawPassword == null || rawPassword.trim().isEmpty()) {
            return false;
        }
        User user = userDao.findByUsername(username);
        if (user == null) {
            return false;
        }
        return user.getPassword().equals(rawPassword);
    }

    // Requirement 5: Register Membership
    public User registerMembership(UUID userId, MembershipType type) {
        User user = userDao.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }
        if (user.getMembershipType() != null && user.isMembershipApproved()) {
            throw new IllegalStateException("User already has an active membership.");
        }

        user.setMembershipType(type);
        user.setMembershipApproved(false); // Starts as pending approval
        userDao.update(user);
        return user;
    }

    // Requirement 7: Validate Borrow Limit
    public void validateBorrowLimit(UUID readerId) {
        User user = userDao.findById(readerId);
        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }
        if (user.getMembershipType() == null || !user.isMembershipApproved()) {
            throw new IllegalStateException("User does not have an approved membership.");
        }

        int limit = user.getMembershipType().getMaxBooks();
        if (user.getActiveBorrowsCount() >= limit) {
            throw new IllegalStateException("Borrow limit exceeded for membership type: " + user.getMembershipType());
        }
    }
    
    // Helper to register user
    public User registerUser(User user) {
        userDao.save(user);
        return user;
    }

    // Helper to update an existing user
    public void updateUser(User user) {
        userDao.update(user);
    }

    // Helper to approve membership (for testing)
    public void approveMembership(UUID userId) {
        User user = userDao.findById(userId);
        user.setMembershipApproved(true);
        userDao.update(user);
    }
}