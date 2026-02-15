package com.example.uniequip;

public class BookingEquipmentItem {
    public String name, category, model;
    public int qty;

    public BookingEquipmentItem(String name, String category, String model, int qty) {
        this.name = name;
        this.category = category;
        this.model = model;
        this.qty = qty;
    }
}