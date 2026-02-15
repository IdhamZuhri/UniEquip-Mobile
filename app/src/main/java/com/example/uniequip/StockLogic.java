package com.example.uniequip;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class StockLogic {

    public interface ReservedMapCallback {
        void onSuccess(HashMap<String, Integer> map);
        void onError(String message);
    }

    /**
     * 1. COMPUTE RESERVED MAP (Future/Date-Based)
     * Calculates how many items are booked during a specific date range.
     * Considers: "approved", "borrowed", "late".
     * Used in: Booking Creation (Client side), Approval Check (Admin side).
     */
    public static void computeReservedMapForRange(
            FirebaseFirestore db,
            Timestamp startDate,
            Timestamp endDate,
            String ignoreBookingId,
            ReservedMapCallback callback
    ) {
        if (startDate == null || endDate == null) {
            callback.onError("Missing date range");
            return;
        }

        // Query potentially overlapping bookings
        db.collection("bookings")
                .whereIn("status", Arrays.asList("approved", "borrowed", "late"))
                .get()
                .addOnSuccessListener(bookingsSnap -> {
                    List<DocumentSnapshot> validBookings = new ArrayList<>();

                    for (DocumentSnapshot doc : bookingsSnap.getDocuments()) {
                        // Skip ignored booking (e.g. the one currently being edited)
                        if (ignoreBookingId != null && ignoreBookingId.equals(doc.getId())) {
                            continue;
                        }

                        // Check Date Overlap
                        Timestamp bStart = doc.getTimestamp("start_date");
                        Timestamp bEnd = doc.getTimestamp("end_date");

                        if (bStart != null && bEnd != null && isOverlappingInclusive(startDate, endDate, bStart, bEnd)) {
                            validBookings.add(doc);
                        }
                    }

                    // Hand off to helper to sum up items
                    fetchAndAggregateItems(db, validBookings, callback);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * 2. COMPUTE OUT MAP (Current Physical Stock)
     * Calculates how many items are physically out of the store RIGHT NOW.
     * Considers: "borrowed", "late".
     * Used in: Admin Equipment List (Total - Out = In Store).
     */
    public static void computeOutMapNow(FirebaseFirestore db, ReservedMapCallback callback) {
        db.collection("bookings")
                .whereIn("status", Arrays.asList("borrowed", "late"))
                .get()
                .addOnSuccessListener(bookingsSnap -> {
                    // All bookings with these statuses are considered "Out"
                    fetchAndAggregateItems(db, bookingsSnap.getDocuments(), callback);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // --- HELPER METHODS ---

    /**
     * Helper to fetch sub-collections ("items") for a list of bookings and sum quantities.
     */
    private static void fetchAndAggregateItems(
            FirebaseFirestore db,
            List<DocumentSnapshot> bookings,
            ReservedMapCallback callback
    ) {
        if (bookings.isEmpty()) {
            callback.onSuccess(new HashMap<>());
            return;
        }

        List<Task<QuerySnapshot>> tasks = new ArrayList<>();
        for (DocumentSnapshot doc : bookings) {
            tasks.add(doc.getReference().collection("items").get());
        }

        Tasks.whenAllSuccess(tasks)
                .addOnSuccessListener(results -> {
                    HashMap<String, Integer> quantityMap = new HashMap<>();

                    for (Object obj : results) {
                        if (obj instanceof QuerySnapshot) {
                            QuerySnapshot snap = (QuerySnapshot) obj;
                            for (DocumentSnapshot itemDoc : snap.getDocuments()) {
                                String eqId = itemDoc.getString("equipment_id");
                                Long q = itemDoc.getLong("qty");
                                int qty = (q == null) ? 0 : q.intValue();

                                if (eqId != null && qty > 0) {
                                    int current = quantityMap.containsKey(eqId) ? quantityMap.get(eqId) : 0;
                                    quantityMap.put(eqId, current + qty);
                                }
                            }
                        }
                    }
                    callback.onSuccess(quantityMap);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Inclusive overlap check: (StartA <= EndB) && (StartB <= EndA)
     */
    private static boolean isOverlappingInclusive(Timestamp aStart, Timestamp aEnd, Timestamp bStart, Timestamp bEnd) {
        long sA = aStart.toDate().getTime();
        long eA = aEnd.toDate().getTime();
        long sB = bStart.toDate().getTime();
        long eB = bEnd.toDate().getTime();
        return sA <= eB && sB <= eA;
    }

    /**
     * Helper to safely get value from the map
     */
    public static int getReservedFor(HashMap<String, Integer> map, String equipmentId) {
        if (map == null || equipmentId == null) return 0;
        return map.containsKey(equipmentId) ? map.get(equipmentId) : 0;
    }
}