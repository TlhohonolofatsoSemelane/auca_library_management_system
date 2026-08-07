package com.auca.library.service;

import com.auca.library.dao.BookDao;
import com.auca.library.dao.RoomDao;
import com.auca.library.dao.ShelfDao;
import com.auca.library.domain.Book;
import com.auca.library.domain.Room;
import com.auca.library.domain.Shelf;
import java.util.UUID;

public class BookService {
    private final BookDao bookDao = new BookDao();
    private final ShelfDao shelfDao = new ShelfDao();
    private final RoomDao roomDao = new RoomDao();

    // Requirement 8: Assign Book to Shelf with Category Validation
    public void assignBookToShelf(UUID bookId, UUID shelfId) {
        Book book = bookDao.findById(bookId);
        Shelf shelf = shelfDao.findById(shelfId);

        if (book == null || shelf == null) {
            throw new IllegalArgumentException("Book or Shelf not found.");
        }

        // Validate categories match
        if (book.getCategory() != shelf.getCategory()) {
            throw new IllegalStateException("Cannot place book of category " + book.getCategory() 
                + " on shelf of category " + shelf.getCategory());
        }

        book.setShelf(shelf);
        bookDao.update(book);
    }

    // Requirement 6: Find Book Location Name
    public String getBookLocationName(UUID bookId) {
        Book book = bookDao.findById(bookId);
        if (book == null) {
            throw new IllegalArgumentException("Book not found.");
        }

        Shelf shelf = book.getShelf();
        if (shelf == null) {
            throw new IllegalStateException("Book is not currently assigned to any shelf.");
        }

        Room room = shelf.getRoom();
        if (room == null || room.getLocation() == null) {
            throw new IllegalStateException("Shelf room or location configuration is incomplete.");
        }

        return room.getLocation().getName();
    }

    // Helpers for test setup
    public Book createBook(Book book) {
        bookDao.save(book);
        return book;
    }

    public Room createRoom(Room room) {
        roomDao.save(room);
        return room;
    }

    public Shelf createShelf(Shelf shelf) {
        shelfDao.save(shelf);
        return shelf;
    }
}