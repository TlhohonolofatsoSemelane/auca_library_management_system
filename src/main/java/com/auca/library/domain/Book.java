package com.auca.library.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "isbn", nullable = false, unique = true)
    private String isbn;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private BookCategory category;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "shelf_id")
    private Shelf shelf;

    public Book() {}

    public Book(String title, String isbn, BookCategory category) {
        this.title = title;
        this.isbn = isbn;
        this.category = category;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public BookCategory getCategory() { return category; }
    public void setCategory(BookCategory category) { this.category = category; }
    public Shelf getShelf() { return shelf; }
    public void setShelf(Shelf shelf) { this.shelf = shelf; }
}