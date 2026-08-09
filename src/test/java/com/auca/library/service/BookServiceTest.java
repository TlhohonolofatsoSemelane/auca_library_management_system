package com.auca.library.service;

import com.auca.library.dao.ShelfDao;
import com.auca.library.domain.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class BookServiceTest {
    private BookService bookService;
    private LocationService locationService;
    private ShelfDao shelfDao;

    @Before
    public void setUp() {
        bookService = new BookService();
        locationService = new LocationService();
        shelfDao = new ShelfDao();
    }

    private String unique(String prefix) {
        return prefix + "_" + System.nanoTime();
    }

    @Test
    public void createRoom_validRoom_savesSuccessfully() {
        Location location = locationService.createLocation(
                new Location(unique("Kigali"), unique("KGL"), Location.LocationType.PROVINCE),
                null
        );

        Room room = bookService.createRoom(new Room(unique("Room 101"), location));

        assertNotNull(room.getId());
    }

    @Test
    public void createShelf_validShelf_savesSuccessfully() {
        Location location = locationService.createLocation(
                new Location(unique("Kigali Shelf"), unique("KGL-S"), Location.LocationType.PROVINCE),
                null
        );

        Room room = bookService.createRoom(new Room(unique("Room 102"), location));
        Shelf shelf = bookService.createShelf(new Shelf(unique("SHELF-A"), BookCategory.SCIENCE, room));

        assertNotNull(shelf.getId());
    }

    @Test
    public void createBook_validBook_savesSuccessfully() {
        Book book = bookService.createBook(
                new Book(unique("Algorithms"), unique("978-026"), BookCategory.SCIENCE)
        );

        assertNotNull(book.getId());
    }

    @Test
    public void assignBookToShelf_validCategory_succeeds() {
        Location location = locationService.createLocation(
                new Location(unique("Kigali Book"), unique("KGL-B"), Location.LocationType.PROVINCE),
                null
        );

        Room room = bookService.createRoom(new Room(unique("Room 103"), location));
        Shelf shelf = bookService.createShelf(new Shelf(unique("SHELF-B"), BookCategory.SCIENCE, room));
        Book book = bookService.createBook(new Book(unique("Clean Code"), unique("978-013"), BookCategory.SCIENCE));

        bookService.assignBookToShelf(book.getId(), shelf.getId());

        Book updatedBook = new com.auca.library.dao.BookDao().findById(book.getId());

        assertNotNull(updatedBook.getShelf());
        assertEquals(shelf.getId(), updatedBook.getShelf().getId());
    }

    @Test(expected = IllegalStateException.class)
    public void assignBookToShelf_mismatchedCategory_throwsException() {
        Location location = locationService.createLocation(
                new Location(unique("Kigali Mismatch"), unique("KGL-M"), Location.LocationType.PROVINCE),
                null
        );

        Room room = bookService.createRoom(new Room(unique("Room 104"), location));
        Shelf shelf = bookService.createShelf(new Shelf(unique("SHELF-C"), BookCategory.SCIENCE, room));
        Book book = bookService.createBook(new Book(unique("The Hobbit"), unique("978-0261"), BookCategory.FICTION));

        bookService.assignBookToShelf(book.getId(), shelf.getId());
    }

    @Test
    public void assignShelfToRoom_updatesShelfRoomId() {
        Location location = locationService.createLocation(
                new Location(unique("Library Branch"), unique("LIB-A"), Location.LocationType.PROVINCE),
                null
        );

        Room room1 = bookService.createRoom(new Room(unique("Room 101"), location));
        Room room2 = bookService.createRoom(new Room(unique("Room 102"), location));
        Shelf shelf = bookService.createShelf(new Shelf(unique("SHELF-01"), BookCategory.SCIENCE, room1));

        bookService.assignShelfToRoom(shelf.getId(), room2.getId());

        try (org.hibernate.Session session = com.auca.library.util.HibernateUtil
                .getSessionFactory()
                .openSession()) {

            Shelf updatedShelf = session.get(Shelf.class, shelf.getId());

            assertNotNull("Shelf should exist in database", updatedShelf);
            assertNotNull("Shelf should be assigned to a room", updatedShelf.getRoom());
            assertEquals("Shelf should be reassigned to room2", room2.getId(), updatedShelf.getRoom().getId());
        }
    }

    @Test
    public void roomWithMultipleShelves_sumsBookCountsAcrossShelves() {
        Location location = locationService.createLocation(
                new Location(unique("Library Branch"), unique("LIB-B"), Location.LocationType.PROVINCE),
                null
        );

        Room room = bookService.createRoom(new Room(unique("Room 201"), location));

        Shelf shelf1 = bookService.createShelf(new Shelf(unique("SHELF-02"), BookCategory.SCIENCE, room));
        Shelf shelf2 = bookService.createShelf(new Shelf(unique("SHELF-03"), BookCategory.FICTION, room));

        Book book1 = bookService.createBook(new Book(unique("Science 1"), unique("ISBN-S1"), BookCategory.SCIENCE));
        Book book2 = bookService.createBook(new Book(unique("Science 2"), unique("ISBN-S2"), BookCategory.SCIENCE));
        Book book3 = bookService.createBook(new Book(unique("Fiction 1"), unique("ISBN-F1"), BookCategory.FICTION));

        bookService.assignBookToShelf(book1.getId(), shelf1.getId());
        bookService.assignBookToShelf(book2.getId(), shelf1.getId());
        bookService.assignBookToShelf(book3.getId(), shelf2.getId());

        int count = bookService.countBooksInRoom(room.getId());

        assertEquals(3, count);
    }

    @Test
    public void roomWithNoShelves_returnsZero() {
        Location location = locationService.createLocation(
                new Location(unique("Library Branch"), unique("LIB-C"), Location.LocationType.PROVINCE),
                null
        );

        Room room = bookService.createRoom(new Room(unique("Room Empty"), location));

        int count = bookService.countBooksInRoom(room.getId());

        assertEquals(0, count);
    }

    @Test
    public void multipleRooms_returnsRoomWithLowestBookCount() {
        Location location = locationService.createLocation(
                new Location(unique("Library Branch"), unique("LIB-D"), Location.LocationType.PROVINCE),
                null
        );

        Room roomWithBooks = bookService.createRoom(new Room(unique("Room Busy"), location));
        Shelf shelfBusy = bookService.createShelf(new Shelf(unique("SHELF-BUSY"), BookCategory.SCIENCE, roomWithBooks));
        Book book = bookService.createBook(new Book(unique("Busy Book"), unique("ISBN-BUSY"), BookCategory.SCIENCE));

        bookService.assignBookToShelf(book.getId(), shelfBusy.getId());

        bookService.createRoom(new Room(unique("Room Quiet"), location));

        Room result = bookService.findRoomWithFewestBooks();

        assertNotNull(result);

        int countFewest = bookService.countBooksInRoom(result.getId());
        int countBusy = bookService.countBooksInRoom(roomWithBooks.getId());

        assertTrue(countFewest <= countBusy);
    }
}