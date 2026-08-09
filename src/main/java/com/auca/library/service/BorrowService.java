package com.auca.library.service;

import com.auca.library.dao.BookDao;
import com.auca.library.dao.BorrowRecordDao;
import com.auca.library.dao.FeeDao;
import com.auca.library.dao.UserDao;
import com.auca.library.domain.Book;
import com.auca.library.domain.BorrowRecord;
import com.auca.library.domain.Fee;
import com.auca.library.domain.User;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class BorrowService {
    private final BorrowRecordDao borrowRecordDao = new BorrowRecordDao();
    private final UserDao userDao = new UserDao();
    private final BookDao bookDao = new BookDao();
    private final FeeDao feeDao = new FeeDao();
    private final UserService userService = new UserService();

    // Requirement 7: Process Book Borrowing
    public BorrowRecord borrowBook(UUID userId, UUID bookId, int borrowDurationDays) {
        User user = userDao.findById(userId);
        Book book = bookDao.findById(bookId);

        if (user == null || book == null) {
            throw new IllegalArgumentException("User or Book not found.");
        }

        // Validate membership & limits using UserService
        userService.validateBorrowLimit(userId);

        // Create borrow record
        LocalDate borrowDate = LocalDate.now();
        LocalDate dueDate = borrowDate.plusDays(borrowDurationDays);
        BorrowRecord record = new BorrowRecord(user, book, borrowDate, dueDate);
        borrowRecordDao.save(record);

        // Increment user's active borrow count
        user.setActiveBorrowsCount(user.getActiveBorrowsCount() + 1);
        userDao.update(user);

        return record;
    }

    // Requirement 7: Process Return & Late Fee Calculation
    public void returnBook(UUID recordId, LocalDate returnDate) {
        BorrowRecord record = borrowRecordDao.findById(recordId);
        if (record == null) {
            throw new IllegalArgumentException("Borrow record not found.");
        }

        if (record.getReturnDate() != null) {
            throw new IllegalStateException("Book has already been returned.");
        }

        record.setReturnDate(returnDate);
        borrowRecordDao.update(record);

        // Decrement user's active borrow count
        User user = record.getUser();
        if (user.getActiveBorrowsCount() > 0) {
            user.setActiveBorrowsCount(user.getActiveBorrowsCount() - 1);
            userDao.update(user);
        }

        // Calculate Late Fee: 200 RWF per late day
        if (returnDate.isAfter(record.getDueDate())) {
            long lateDays = ChronoUnit.DAYS.between(record.getDueDate(), returnDate);
            double feeAmount = lateDays * 200.0;

            Fee fee = new Fee(user, feeAmount);
            feeDao.save(fee);
        }
    } 

    // Requirement 13: Calculate late return fees (using membership rates)
    public int calculateLateFee(UUID borrowRecordId) {
        BorrowRecord record = borrowRecordDao.findById(borrowRecordId);
        if (record == null) {
            throw new IllegalArgumentException("Borrow record not found.");
        }

        LocalDate endCompare = record.getReturnDate() != null ? record.getReturnDate() : LocalDate.now();
        if (!endCompare.isAfter(record.getDueDate())) {
            return 0;
        }

        long lateDays = ChronoUnit.DAYS.between(record.getDueDate(), endCompare);
        int dailyRate = 0;

        switch (record.getUser().getMembershipType()) {
            case GOLD:
                dailyRate = 50;
                break;
            case SILVER:
                dailyRate = 30;
                break;
            case STRIVER:
                dailyRate = 10;
                break;
        }

        return (int) (lateDays * dailyRate);
    }
}
