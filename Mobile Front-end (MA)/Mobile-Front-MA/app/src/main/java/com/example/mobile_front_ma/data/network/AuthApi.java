package com.example.mobile_front_ma.data.network;

import com.example.mobile_front_ma.models.dto.LoginRequest;
import com.example.mobile_front_ma.models.dto.LoginResponse;
import com.example.mobile_front_ma.models.dto.RegisterRequest;
import com.example.mobile_front_ma.models.dto.RegisterResponse;

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

    @POST("api/auth/register")
    Call<RegisterResponse> register(@Body RegisterRequest body);

    /** Optional: upload the profile picture chosen during registration. */
    @Multipart
    @POST("api/pictures/register/profile")
    Call<ResponseBody> uploadRegistrationPicture(
            @Part MultipartBody.Part file,
            @Part("pictureToken") RequestBody pictureToken);
}
