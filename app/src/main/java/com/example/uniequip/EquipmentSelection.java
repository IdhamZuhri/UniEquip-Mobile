package com.example.uniequip;

import java.util.HashMap;
import java.util.Map;

public class EquipmentSelection {
    public String equipmentId;
    public String name;
    public String category;
    public String model;
    public int qty;

    public EquipmentSelection() {}

    public EquipmentSelection(String equipmentId, String name, String category, String model, int qty) {
        this.equipmentId = equipmentId;
        this.name = name;
        this.category = category;
        this.model = model;
        this.qty = qty;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> m = new HashMap<>();
        m.put("equipment_id", equipmentId);
        m.put("name", name);
        m.put("category", category);
        m.put("model", model);
        m.put("qty", qty);
        return m;
    }
}
