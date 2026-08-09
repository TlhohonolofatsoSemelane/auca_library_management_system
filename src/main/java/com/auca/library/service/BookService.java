package com.auca.library.service;

import com.auca.library.dao.BookDao;
import com.auca.library.dao.RoomDao;
import com.auca.library.dao.ShelfDao;
import com.auca.library.domain.Book;
import com.auca.library.domain.Room;
import com.auca.library.domain.Shelf;
import com.auca.library.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;
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

    // Requirement 9: Assign a shelf to a room
    public void assignShelfToRoom(UUID shelfId, UUID roomId) {
        Transaction transaction = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Shelf shelf = session.get(Shelf.class, shelfId);
            Room room = session.get(Room.class, roomId);

            if (shelf == null || room == null) {
                throw new IllegalArgumentException("Shelf or Room not found.");
            }

            shelf.setRoom(room);

            /*
             * No persist() here.
             * The shelf was loaded in this same session, so it is managed.
             * Hibernate automatically flushes the room_id update on commit.
             */
            transaction.commit();

        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        }
    }

    // Requirement 10: Count how many books are in a specific room
    public int countBooksInRoom(UUID roomId) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        boolean hasActiveTransaction = session.getTransaction().isActive();

        if (!hasActiveTransaction) {
            session.beginTransaction();
        }

        try {
            Long count = session.createQuery(
                            "select count(b) from Book b where b.shelf.room.id = :roomId",
                            Long.class
                    )
                    .setParameter("roomId", roomId)
                    .uniqueResult();

            if (!hasActiveTransaction) {
                session.getTransaction().commit();
            }

            return count != null ? count.intValue() : 0;

        } catch (Exception e) {
            if (!hasActiveTransaction && session.getTransaction().isActive()) {
                session.getTransaction().rollback();
            }
            throw e;
        }
    }

    // Requirement 11: Find the room with the fewest books
    public Room findRoomWithFewestBooks() {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        boolean hasActiveTransaction = session.getTransaction().isActive();

        if (!hasActiveTransaction) {
            session.beginTransaction();
        }

        try {
            List<Room> rooms = session.createQuery("from Room", Room.class).list();

            if (rooms.isEmpty()) {
                if (!hasActiveTransaction) {
                    session.getTransaction().commit();
                }
                return null;
            }

            Room fewestRoom = null;
            int minBooks = Integer.MAX_VALUE;

            for (Room room : rooms) {
                Long count = session.createQuery(
                                "select count(b) from Book b where b.shelf.room.id = :roomId",
                                Long.class
                        )
                        .setParameter("roomId", room.getId())
                        .uniqueResult();

                int bookCount = count != null ? count.intValue() : 0;

                if (bookCount < minBooks) {
                    minBooks = bookCount;
                    fewestRoom = room;
                }
            }

            if (!hasActiveTransaction) {
                session.getTransaction().commit();
            }

            return fewestRoom;

        } catch (Exception e) {
            if (!hasActiveTransaction && session.getTransaction().isActive()) {
                session.getTransaction().rollback();
            }
            throw e;
        }
    }
}