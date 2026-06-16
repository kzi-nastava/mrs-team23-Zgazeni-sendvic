package com.example.mobile_front_ma.data.network;

import com.example.mobile_front_ma.models.dto.ConfirmAccountRequest;
import com.example.mobile_front_ma.models.dto.ForgotPasswordRequest;
import com.example.mobile_front_ma.models.dto.LoginRequest;
import com.example.mobile_front_ma.models.dto.LoginResponse;
import com.example.mobile_front_ma.models.dto.RegisterRequest;
import com.example.mobile_front_ma.models.dto.RegisterResponse;
import com.example.mobile_front_ma.models.dto.ResetPasswordRequest;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Auth + registration endpoints exposed by the Spring backend (AuthController / PictureController).
 */
public interface AuthApi {

    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest body);

    /**
     * Log the current user out. For drivers the backend refuses (400) while they are
     * still marked available, so they must go inactive first. Requires the JWT, so
     * call it through the authenticated Retrofit client.
     */
    @POST("api/auth/logout")
    Call<ResponseBody> logout();

    @POST("api/auth/register")
    Call<RegisterResponse> register(@Body RegisterRequest body);

    /** Step 1 of password reset: ask the backend to email a 6-digit code. */
    @POST("api/auth/forgot-password")
    Call<ResponseBody> forgotPassword(@Body ForgotPasswordRequest body);

    /** Step 2 of password reset: submit the emailed code plus the new password. */
    @POST("api/auth/reset-password")
    Call<ResponseBody> resetPassword(@Body ResetPasswordRequest body);

    /** Activate a freshly registered account with the emailed 6-digit code. */
    @POST("api/auth/confirm-account")
    Call<ResponseBody> confirmAccount(@Body ConfirmAccountRequest body);

    /** Optional: upload the profile picture chosen during registration. */
    @Multipart
    @POST("api/pictures/register/profile")
    Call<ResponseBody> uploadRegistrationPicture(
            @Part MultipartBody.Part file,
            @Part("pictureToken") RequestBody pictureToken);
}
