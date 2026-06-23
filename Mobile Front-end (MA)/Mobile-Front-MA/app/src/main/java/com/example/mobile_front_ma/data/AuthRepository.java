package com.example.mobile_front_ma.data;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.mobile_front_ma.data.network.ApiCallback;
import com.example.mobile_front_ma.data.network.ApiClient;
import com.example.mobile_front_ma.data.network.AuthApi;
import com.example.mobile_front_ma.models.dto.ConfirmAccountRequest;
import com.example.mobile_front_ma.models.dto.ForgotPasswordRequest;
import com.example.mobile_front_ma.models.dto.LoginRequest;
import com.example.mobile_front_ma.models.dto.LoginResponse;
import com.example.mobile_front_ma.models.dto.RegisterRequest;
import com.example.mobile_front_ma.models.dto.RegisterResponse;
import com.example.mobile_front_ma.models.dto.ResetPasswordRequest;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Talks to the backend auth endpoints and hands plain success/error results to the
 * caller. All Retrofit calls run asynchronously (enqueue), so callbacks fire on the
 * main thread and the UI never blocks.
 */
public class AuthRepository {

    private final AuthApi api;

    public AuthRepository() {
        this.api = ApiClient.create(AuthApi.class);
    }

    public void login(LoginRequest request, ApiCallback<LoginResponse> callback) {
        api.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call,
                                   @NonNull Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(loginErrorMessage(response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                callback.onError(networkErrorMessage());
            }
        });
    }

    public void register(RegisterRequest request, ApiCallback<RegisterResponse> callback) {
        api.register(request).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(@NonNull Call<RegisterResponse> call,
                                   @NonNull Response<RegisterResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(registerErrorMessage(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<RegisterResponse> call, @NonNull Throwable t) {
                callback.onError(networkErrorMessage());
            }
        });
    }

    /** Step 1 of password reset: ask the backend to email a 6-digit code. */
    public void forgotPassword(ForgotPasswordRequest request, ApiCallback<Void> callback) {
        api.forgotPassword(request).enqueue(simpleCallback(callback));
    }

    /** Step 2 of password reset: submit the emailed code plus the new password. */
    public void resetPassword(ResetPasswordRequest request, ApiCallback<Void> callback) {
        api.resetPassword(request).enqueue(simpleCallback(callback));
    }

    /** Activate a freshly registered account with the emailed 6-digit code. */
    public void confirmAccount(ConfirmAccountRequest request, ApiCallback<Void> callback) {
        api.confirmAccount(request).enqueue(simpleCallback(callback));
    }

    /**
     * Log the current user out. Goes through the authenticated client so the backend
     * can identify the user from the JWT and (for an active driver) flip them inactive.
     *
     * Local logout must be unconditional: the user can never be trapped in a session
     * they can't leave. So the server call is best-effort and we report success in
     * every case EXCEPT the one legitimate business veto -- HTTP 400, which the backend
     * returns only for a still-available driver who must go inactive first. Everything
     * else (expired/invalid JWT -> the backend's permitAll path returns 200 anyway,
     * 401/403, 5xx, or an unreachable server) still clears the local session.
     * See LOGOUT_RESILIENCE.md for the full rationale and truth table.
     */
    public void logout(Context context, ApiCallback<Void> callback) {
        AuthApi authApi = ApiClient.createAuthenticated(context, AuthApi.class);
        authApi.logout().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call,
                                   @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() || response.code() != 400) {
                    // 2xx, or a failure unrelated to the business rule (expired/401,
                    // 403, 5xx): log out locally anyway -- never trap the user.
                    callback.onSuccess(null);
                } else {
                    // The one real block: an available driver must go inactive first
                    // (backend returns 400 + a human-readable reason).
                    callback.onError(codeErrorMessage(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                // Server unreachable: still clear the local session so logout works.
                callback.onSuccess(null);
            }
        });
    }

    /**
     * Shared callback for the endpoints that return a plain text status body. On error
     * we surface the backend message (e.g. "Invalid or expired code") when present.
     */
    private Callback<ResponseBody> simpleCallback(ApiCallback<Void> callback) {
        return new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call,
                                   @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError(codeErrorMessage(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                callback.onError(networkErrorMessage());
            }
        };
    }

    private String codeErrorMessage(Response<?> response) {
        String body = readErrorBody(response);
        if (body != null && !body.trim().isEmpty()) {
            // Backend sends a human-readable reason (e.g. "Invalid or expired code").
            return body.trim().replace("\"", "");
        }
        if (response.code() == 401 || response.code() == 400) {
            return "The code is invalid or has expired. Please try again.";
        }
        return "Request failed (error " + response.code() + ").";
    }

    /** Optional follow-up call after a successful registration, when a picture was chosen. */
    public void uploadRegistrationPicture(String pictureToken, byte[] bytes, String fileName,
                                          String contentType, ApiCallback<Void> callback) {
        MediaType type = MediaType.parse(contentType != null ? contentType : "image/jpeg");
        RequestBody fileBody = RequestBody.create(type, bytes);
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", fileName, fileBody);
        RequestBody tokenBody = RequestBody.create(MediaType.parse("text/plain"), pictureToken);

        api.uploadRegistrationPicture(filePart, tokenBody).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call,
                                   @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Picture upload failed (" + response.code() + ").");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                callback.onError(networkErrorMessage());
            }
        });
    }

    private String loginErrorMessage(int code) {
        if (code == 401 || code == 403) {
            return "Invalid email or password, or your account hasn't been activated yet.";
        }
        if (code == 400) {
            return "Please enter a valid email and password.";
        }
        return "Login failed (error " + code + ").";
    }

    private String registerErrorMessage(Response<?> response) {
        String body = readErrorBody(response);
        if (body != null && body.toLowerCase().contains("email")) {
            return "This email is already in use.";
        }
        if (response.code() == 400) {
            return "Some fields are invalid. Please check your input and try again.";
        }
        return "Registration failed. The email may already be in use.";
    }

    private String readErrorBody(Response<?> response) {
        try (ResponseBody errorBody = response.errorBody()) {
            return errorBody != null ? errorBody.string() : null;
        } catch (IOException e) {
            return null;
        }
    }

    private String networkErrorMessage() {
        return "Cannot reach the server. Make sure the backend is running and the address is correct.";
    }
}
