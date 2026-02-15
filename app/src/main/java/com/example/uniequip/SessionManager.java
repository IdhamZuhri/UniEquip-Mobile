package com.example.uniequip;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF = "uniequip_session";
    private static final String KEY_STUD_NUM = "stud_num";

    public static void setStudentNumber(Context c, String studNum) {
        SharedPreferences sp = c.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_STUD_NUM, studNum).apply();
    }

    public static String getStudentNumber(Context c) {
        SharedPreferences sp = c.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        // fallback if you haven’t set it yet
        return sp.getString(KEY_STUD_NUM, "000000");
    }
}
