package com.example.mobile_front_ma.util;

import android.content.Context;
import android.content.Intent;

import com.example.mobile_front_ma.activities.LoginActivity;
import com.example.mobile_front_ma.data.SessionManager;

public final class SessionExpiredHandler {

    private static boolean handlingExpiration = false;

    private SessionExpiredHandler() {
    }

    public static synchronized void handle(Context context) {

        if (handlingExpiration) {
            return;
        }

        handlingExpiration = true;

        Context appContext =
                context.getApplicationContext();

        SessionManager session =
                new SessionManager(appContext);

        session.clear();

        Intent intent =
                new Intent(
                        appContext,
                        LoginActivity.class
                );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        appContext.startActivity(intent);

        handlingExpiration = false;
    }
}