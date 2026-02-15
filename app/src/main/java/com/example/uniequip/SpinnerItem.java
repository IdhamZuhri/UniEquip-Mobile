package com.example.uniequip;

public class SpinnerItem {
    public String id;
    public String name;

    public SpinnerItem(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return name; // Spinner will display this
    }
}
