package com.auca.library.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "shelves")
public class Shelf {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "shelf_code", nullable = false, unique = true)
    private String shelfCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private BookCategory category;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    public Shelf() {}

    public Shelf(String shelfCode, BookCategory category, Room room) {
        this.shelfCode = shelfCode;
        this.category = category;
        this.room = room;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getShelfCode() { return shelfCode; }
    public void setShelfCode(String shelfCode) { this.shelfCode = shelfCode; }
    public BookCategory getCategory() { return category; }
    public void setCategory(BookCategory category) { this.category = category; }
    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }
}