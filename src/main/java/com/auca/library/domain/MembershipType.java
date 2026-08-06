package com.auca.library.domain;

public enum MembershipType {
    GOLD(50, 5),
    SILVER(30, 3),
    STRIVER(10, 2);

    private final int dailyRate;
    private final int maxBooks;

    MembershipType(int dailyRate, int maxBooks) {
        this.dailyRate = dailyRate;
        this.maxBooks = maxBooks;
    }

    public int getDailyRate() { return dailyRate; }
    public int getMaxBooks() { return maxBooks; }
}
