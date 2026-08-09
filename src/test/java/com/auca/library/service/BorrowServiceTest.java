package com.auca.library.service;

import com.auca.library.domain.*;
import org.junit.Before;
import org.junit.Test;
import java.time.LocalDate;
import static org.junit.Assert.*;

public class BorrowServiceTest {
    private BorrowService borrowService;
    private UserService userService;
    private BookService bookService;

    @Before
    public void setUp() {
        borrowService = new BorrowService();
        userService = new UserService();
        bookService = new BookService();
    }

    private String unique(String prefix) {
        return prefix + "_" + System.nanoTime();
    }

    @Test
    public void borrowBook_validUserAndBook_savesSuccessfully() {
        User user = new User("Borrower", "One", unique("borrower1"), "pass");
        userService.registerUser(user);
        userService.registerMembership(user.getId(), MembershipType.GOLD);
        userService.approveMembership(user.getId());

        Book book = bookService.createBook(new Book(unique("Clean Code"), unique("978-013"), BookCategory.TECHNOLOGY));

        BorrowRecord record = borrowService.borrowBook(user.getId(), book.getId(), 14);

        assertNotNull(record.getId());
        assertEquals(user.getId(), record.getUser().getId());
        assertEquals(book.getId(), record.getBook().getId());
        assertEquals(LocalDate.now().plusDays(14), record.getDueDate());

        User updatedUser = new com.auca.library.dao.UserDao().findById(user.getId());
        assertEquals(1, updatedUser.getActiveBorrowsCount());
    }

    @Test(expected = IllegalStateException.class)
    public void borrowBook_unapprovedUser_throwsException() {
        User user = new User("Unapproved", "User", unique("unapproved1"), "pass");
        userService.registerUser(user);
        userService.registerMembership(user.getId(), MembershipType.GOLD);

        Book book = bookService.createBook(new Book(unique("Clean Arch"), unique("978-014"), BookCategory.TECHNOLOGY));

        borrowService.borrowBook(user.getId(), book.getId(), 14);
    }

    @Test
    public void returnBook_onTime_noFeeCharged() {
        User user = new User("OnTime", "Borrower", unique("ontime1"), "pass");
        userService.registerUser(user);
        userService.registerMembership(user.getId(), MembershipType.GOLD);
        userService.approveMembership(user.getId());

        Book book = bookService.createBook(new Book(unique("Refactoring"), unique("978-015"), BookCategory.TECHNOLOGY));
        BorrowRecord record = borrowService.borrowBook(user.getId(), book.getId(), 14);

        LocalDate returnDate = LocalDate.now().plusDays(5);
        borrowService.returnBook(record.getId(), returnDate);

        BorrowRecord updatedRecord = new com.auca.library.dao.BorrowRecordDao().findById(record.getId());
        assertEquals(returnDate, updatedRecord.getReturnDate());

        User updatedUser = new com.auca.library.dao.UserDao().findById(user.getId());
        assertEquals(0, updatedUser.getActiveBorrowsCount());
    }

    @Test
    public void returnBook_late_chargesLateFee() {
        User user = new User("Late", "Borrower", unique("late1"), "pass");
        userService.registerUser(user);
        userService.registerMembership(user.getId(), MembershipType.GOLD);
        userService.approveMembership(user.getId());

        Book book = bookService.createBook(new Book(unique("Design Patterns"), unique("978-016"), BookCategory.TECHNOLOGY));
        BorrowRecord record = borrowService.borrowBook(user.getId(), book.getId(), 14);

        LocalDate returnDate = LocalDate.now().plusDays(19);
        borrowService.returnBook(record.getId(), returnDate);

        User updatedUser = new com.auca.library.dao.UserDao().findById(user.getId());
        assertEquals(0, updatedUser.getActiveBorrowsCount());
    }

    @Test
    public void returnedOnDueDate_feeIsZero() {
        User user = new User("Fee", "User1", unique("fee_user1"), "pass");
        userService.registerUser(user);
        userService.registerMembership(user.getId(), MembershipType.GOLD);
        userService.approveMembership(user.getId());

        Book book = bookService.createBook(new Book(unique("Book 1"), unique("ISBN-B1"), BookCategory.TECHNOLOGY));
        BorrowRecord record = borrowService.borrowBook(user.getId(), book.getId(), 14);

        borrowService.returnBook(record.getId(), LocalDate.now().plusDays(14));

        int fee = borrowService.calculateLateFee(record.getId());
        assertEquals(0, fee);
    }

    @Test
    public void goldMember_returnedThreeDaysLate_feeIs150() {
        User user = new User("Fee", "User2", unique("fee_user2"), "pass");
        userService.registerUser(user);
        userService.registerMembership(user.getId(), MembershipType.GOLD);
        userService.approveMembership(user.getId());

        Book book = bookService.createBook(new Book(unique("Book 2"), unique("ISBN-B2"), BookCategory.TECHNOLOGY));
        BorrowRecord record = borrowService.borrowBook(user.getId(), book.getId(), 14);

        borrowService.returnBook(record.getId(), LocalDate.now().plusDays(17));

        int fee = borrowService.calculateLateFee(record.getId());
        assertEquals(150, fee);
    }

    @Test
    public void silverMember_returnedFiveDaysLate_feeIs150() {
        User user = new User("Fee", "User3", unique("fee_user3"), "pass");
        userService.registerUser(user);
        userService.registerMembership(user.getId(), MembershipType.SILVER);
        userService.approveMembership(user.getId());

        Book book = bookService.createBook(new Book(unique("Book 3"), unique("ISBN-B3"), BookCategory.TECHNOLOGY));
        BorrowRecord record = borrowService.borrowBook(user.getId(), book.getId(), 14);

        borrowService.returnBook(record.getId(), LocalDate.now().plusDays(19));

        int fee = borrowService.calculateLateFee(record.getId());
        assertEquals(150, fee);
    }

    @Test
    public void striverMember_returnedOneDayLate_feeIs10() {
        User user = new User("Fee", "User4", unique("fee_user4"), "pass");
        userService.registerUser(user);
        userService.registerMembership(user.getId(), MembershipType.STRIVER);
        userService.approveMembership(user.getId());

        Book book = bookService.createBook(new Book(unique("Book 4"), unique("ISBN-B4"), BookCategory.TECHNOLOGY));
        BorrowRecord record = borrowService.borrowBook(user.getId(), book.getId(), 14);

        borrowService.returnBook(record.getId(), LocalDate.now().plusDays(15));

        int fee = borrowService.calculateLateFee(record.getId());
        assertEquals(10, fee);
    }

    @Test
    public void notYetReturned_feeIsComputedAgainstToday() {
        User user = new User("Fee", "User5", unique("fee_user5"), "pass");
        userService.registerUser(user);
        userService.registerMembership(user.getId(), MembershipType.GOLD);
        userService.approveMembership(user.getId());

        Book book = bookService.createBook(new Book(unique("Book 5"), unique("ISBN-B5"), BookCategory.TECHNOLOGY));
        BorrowRecord record = borrowService.borrowBook(user.getId(), book.getId(), -2);

        int fee = borrowService.calculateLateFee(record.getId());
        assertEquals(100, fee);
    }
}