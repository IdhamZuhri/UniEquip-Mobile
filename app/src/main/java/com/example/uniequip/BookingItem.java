package com.example.uniequip;

public class BookingItem {
    public String id;            // doc id (optional)
    public String equipmentId;
    public String name;
    public int qty;

    public BookingItem() {}

    public BookingItem(String id, String equipmentId, String name, int qty) {
        this.id = id;
        this.equipmentId = equipmentId;
        this.name = name;
        this.qty = qty;
    }
}
