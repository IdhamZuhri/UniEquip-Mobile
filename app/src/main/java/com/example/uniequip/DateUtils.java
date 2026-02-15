package com.example.uniequip;

import com.google.firebase.Timestamp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    // Parse "YYYY-MM-DD" -> Timestamp
    public static Timestamp parseDateToTimestamp(String ymd) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            sdf.setLenient(false);
            Date d = sdf.parse(ymd);
            if (d == null) return null;
            return new Timestamp(d);
        } catch (ParseException e) {
            return null;
        }
    }

    // Format Timestamp -> "YYYY-MM-DD"
    public static String formatYMD(Timestamp ts) {
        if (ts == null) return "-";
        Date d = ts.toDate();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(d);
    }

    // TRUE if now > endDate
    public static boolean isLateNow(Timestamp endDate) {
        if (endDate == null) return false;
        long nowMs = System.currentTimeMillis();
        long endMs = endDate.toDate().getTime();
        return nowMs > endMs;
    }

    // Return display status (late only for display)
    public static String toDisplayStatus(String realStatus, Timestamp endDate) {
        if (realStatus == null) return "-";
        if ("borrowed".equalsIgnoreCase(realStatus) && isLateNow(endDate)) {
            return "late";
        }
        return realStatus;
    }
}
