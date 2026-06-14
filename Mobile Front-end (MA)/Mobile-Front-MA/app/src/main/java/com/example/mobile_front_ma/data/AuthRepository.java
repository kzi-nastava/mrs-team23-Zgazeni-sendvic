package com.example.mobile_front_ma.data;

import androidx.annotation.NonNull;

import com.example.mobile_front_ma.data.network.ApiCallback;
import com.example.mobile_front_ma.data.network.ApiClient;
import com.example.mobile_front_ma.data.network.AuthApi;
import com.example.mobile_front_ma.models.dto.LoginRequest;
import com.example.mobile_front_ma.models.dto.LoginResponse;
import com.example.mobile_front_ma.models.dto.RegisterRequest;
import com.example.mobile_front_ma.models.dto.RegisterResponse;

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
