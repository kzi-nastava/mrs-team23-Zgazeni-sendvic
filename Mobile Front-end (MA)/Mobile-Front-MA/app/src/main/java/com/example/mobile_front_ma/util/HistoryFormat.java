package com.example.mobile_front_ma.util;

import com.example.mobile_front_ma.models.dto.LocationDto;

import java.util.Locale;

/**
 * Small formatting helpers shared by the ride-history screens: turning the backend's ISO
 * datetimes and lat/lon points into short, readable strings for the list and detail views.
 */
public final class HistoryFormat {

    private HistoryFormat() {
    }

    public static String safe(String s) {
        return s == null ? "" : s;
    }

    /** "2026-01-20T14:30:00" -> "2026-01-20 14:30". Falls back gracefully on odd input. */
    public static String dateTime(String iso) {
        if (iso == null || iso.isEmpty()) {
            return "—";
        }
        int t = iso.indexOf('T');
        if (t < 0) {
            return iso;
        }
        String date = iso.substring(0, t);
        String time = iso.substring(t + 1);
        // Trim seconds/fraction: keep HH:mm
        if (time.length() >= 5) {
            time = time.substring(0, 5);
        }
        return date + " " + time;
    }

    /** Date portion only: "2026-01-20T14:30:00" -> "2026-01-20". */
    public static String date(String iso) {
        if (iso == null || iso.isEmpty()) {
            return "—";
        }
        int t = iso.indexOf('T');
        return t < 0 ? iso : iso.substring(0, t);
    }

    /** A map point as a compact "lat, lon" label. */
    public static String point(LocationDto p) {
        if (p == null || !p.isValid()) {
            return "?";
        }
        return String.format(Locale.US, "%.4f, %.4f", p.getLatitude(), p.getLongitude());
    }
}
