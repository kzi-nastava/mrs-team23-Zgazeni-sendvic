package com.example.mobile_front_ma.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.mobile_front_ma.models.dto.LoginResponse;

/**
 * Persists the logged-in session (JWT + basic user info) in SharedPreferences,
 * as required by the spec ("Podešavanja aplikacije čuvati u SharedPreferences").
 */
public class SessionManager {

    private static final String PREFS = "auth_session";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_FIRST_NAME = "first_name";
    private static final String KEY_LAST_NAME = "last_name";
    private static final String KEY_ROLE = "role";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_DRIVER_ACTIVE = "driver_active";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void saveSession(LoginResponse response) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_TOKEN, response.getToken());
        if (response.getUser() != null) {
            editor.putString(KEY_EMAIL, response.getUser().getEmail());
            editor.putString(KEY_FIRST_NAME, response.getUser().getFirstName());
            editor.putString(KEY_LAST_NAME, response.getUser().getLastName());
            editor.putString(KEY_ROLE, response.getUser().getRole());
            if (response.getUser().getUserID() != null) {
                editor.putLong(KEY_USER_ID, response.getUser().getUserID());
            }
        }
        editor.apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, null);
    }

    /** Backend account id of the logged-in user, or -1 if unknown. */
    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, -1L);
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }

    public String getFirstName() {
        return prefs.getString(KEY_FIRST_NAME, null);
    }

    /**
     * Whether the logged-in driver is currently active (available for rides).
     * The backend has no "read current status" endpoint, so we remember the last
     * value set from this device; drivers start inactive after logging in.
     */
    public boolean isDriverActive() {
        return prefs.getBoolean(KEY_DRIVER_ACTIVE, false);
    }

    public void setDriverActive(boolean active) {
        prefs.edit().putBoolean(KEY_DRIVER_ACTIVE, active).apply();
    }

    /** Clears the session (used on logout). */
    public void clear() {
        prefs.edit().clear().apply();
    }
}
