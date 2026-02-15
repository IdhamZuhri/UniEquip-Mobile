package com.example.uniequip;

public class EquipmentItem {

    public String id;
    public String name;
    public String category;
    public String model;
    public int totalQty;
    public String imageUrl;

    // ✅ For USER booking screen (availability for selected date range)
    public int availableQty = 0;

    // ✅ For USER selection input
    public int selectedQty = 0;

    // ✅ For ADMIN equipment screen (physical in store now)
    public int inStoreQty = 0;

    public EquipmentItem() {}

    public EquipmentItem(String id, String name, String category, String model, int totalQty, String imageUrl) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.model = model;
        this.totalQty = totalQty;
        this.imageUrl = imageUrl;

        // defaults
        this.availableQty = totalQty;
        this.inStoreQty = totalQty;
        this.selectedQty = 0;
    }
}
