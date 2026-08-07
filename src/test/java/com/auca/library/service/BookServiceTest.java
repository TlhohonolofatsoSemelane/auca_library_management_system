package com.auca.library.service;

import com.auca.library.domain.*;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class BookServiceTest {
    private BookService bookService;
    private LocationService locationService;

    @Before
    public void setUp() {
        bookService = new BookService();
        locationService = new LocationService();
    }

    @Test
    public void assignBookToShelf_matchingCategories_savesSuccessfully() {
        Location libraryBuilding = locationService.createLocation(new Location("Main Library", "LIB-MAIN", Location.LocationType.PROVINCE), null);
        Room room = bookService.createRoom(new Room("Room 101", libraryBuilding));
        Shelf shelf = bookService.createShelf(new Shelf("SHELF-SCI-01", BookCategory.SCIENCE, room));
        Book book = bookService.createBook(new Book("Introduction to Physics", "978-0134051482", BookCategory.SCIENCE));

        bookService.assignBookToShelf(book.getId(), shelf.getId());

        // Verify assignment
        Book updatedBook = new com.auca.library.dao.BookDao().findById(book.getId());
        assertNotNull(updatedBook.getShelf());
        assertEquals("SHELF-SCI-01", updatedBook.getShelf().getShelfCode());
    }

    @Test(expected = IllegalStateException.class)
    public void assignBookToShelf_mismatchedCategories_throwsException() {
        Location libraryBuilding = locationService.createLocation(new Location("Main Library", "LIB-MAIN-2", Location.LocationType.PROVINCE), null);
        Room room = bookService.createRoom(new Room("Room 102", libraryBuilding));
        Shelf shelf = bookService.createShelf(new Shelf("SHELF-HIST-01", BookCategory.HISTORY, room));
        Book book = bookService.createBook(new Book("Introduction to Physics", "978-0134051483", BookCategory.SCIENCE));

        // Attempting to place SCIENCE book on HISTORY shelf should throw exception
        bookService.assignBookToShelf(book.getId(), shelf.getId());
    }

    @Test
    public void getBookLocationName_returnsCorrectLocationName() {
        Location libraryBuilding = locationService.createLocation(new Location("Science Branch", "LIB-SCI", Location.LocationType.PROVINCE), null);
        Room room = bookService.createRoom(new Room("Room 303", libraryBuilding));
        Shelf shelf = bookService.createShelf(new Shelf("SHELF-SCI-02", BookCategory.SCIENCE, room));
        Book book = bookService.createBook(new Book("Organic Chemistry", "978-0321768414", BookCategory.SCIENCE));

        bookService.assignBookToShelf(book.getId(), shelf.getId());

        String locationName = bookService.getBookLocationName(book.getId());
        assertEquals("Science Branch", locationName);
    }

    @Test(expected = IllegalStateException.class)
    public void getBookLocationName_unassignedBook_throwsException() {
        Book book = bookService.createBook(new Book("Unshelved Book", "978-1111111111", BookCategory.FICTION));
        bookService.getBookLocationName(book.getId());
    }
}