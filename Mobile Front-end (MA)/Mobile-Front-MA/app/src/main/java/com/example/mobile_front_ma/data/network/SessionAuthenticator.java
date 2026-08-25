package com.example.mobile_front_ma.data.network;

import android.content.Context;

import androidx.annotation.Nullable;

import com.example.mobile_front_ma.data.SessionManager;
import com.example.mobile_front_ma.util.SessionExpiredHandler;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class SessionAuthenticator implements Authenticator {

    private final Context context;
    private final SessionManager session;

    public SessionAuthenticator(Context context) {

        this.context =
                context.getApplicationContext();

        this.session =
                new SessionManager(this.context);
    }

    @Nullable
    @Override
    public Request authenticate(
            @Nullable Route route,
            Response response
    ) throws IOException {

        /*
         * We don't have a refresh-token mechanism.
         *
         * Therefore a 401 means that the current JWT
         * can no longer authenticate the user.
         */

        SessionExpiredHandler.handle(context);

        /*
         * Returning null tells OkHttp that authentication
         * cannot be recovered and that it should not retry
         * the request.
         */
        return null;
    }
}