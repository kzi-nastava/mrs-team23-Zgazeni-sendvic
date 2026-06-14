package com.example.mobile_front_ma.viewmodels;

import android.app.Application;
import android.text.TextUtils;
import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mobile_front_ma.data.AuthRepository;
import com.example.mobile_front_ma.data.network.ApiCallback;
import com.example.mobile_front_ma.models.dto.RegisterRequest;
import com.example.mobile_front_ma.models.dto.RegisterResponse;
import com.example.mobile_front_ma.util.Resource;

/**
 * Holds the registration screen state. Client-side validation mirrors the backend
 * constraints (email format, password >= 8, names >= 2, phone >= 10 digits) so the
 * user gets instant feedback. On success the (optional) profile picture is uploaded.
 *
 * The success payload is an optional info message; a non-null message means
 * "registered, but with a caveat" (e.g. picture upload failed).
 */
public class RegisterViewModel extends AndroidViewModel {

    private final AuthRepository repository = new AuthRepository();

    private final MutableLiveData<Resource<String>> registerResult = new MutableLiveData<>();

    public RegisterViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<Resource<String>> getRegisterResult() {
        return registerResult;
    }

    public void register(String email, String password, String confirmPassword,
                         String firstName, String lastName, String address, String phone,
                         @Nullable byte[] pictureBytes, @Nullable String pictureName,
                         @Nullable String pictureType) {

        String error = validate(email, password, confirmPassword, firstName, lastName, address, phone);
        if (error != null) {
            registerResult.setValue(Resource.error(error));
            return;
        }

        registerResult.setValue(Resource.loading());
        RegisterRequest request = new RegisterRequest(
                email.trim(), password, firstName.trim(), lastName.trim(),
                address.trim(), phone.trim(), null);

        repository.register(request, new ApiCallback<RegisterResponse>() {
            @Override
            public void onSuccess(RegisterResponse data) {
                if (pictureBytes != null && data.getPictureToken() != null) {
                    uploadPicture(data.getPictureToken(), pictureBytes, pictureName, pictureType);
                } else {
                    registerResult.setValue(Resource.success(null));
                }
            }

            @Override
            public void onError(String message) {
                registerResult.setValue(Resource.error(message));
            }
        });
    }

    private void uploadPicture(String pictureToken, byte[] pictureBytes,
                               @Nullable String pictureName, @Nullable String pictureType) {
        repository.uploadRegistrationPicture(
                pictureToken, pictureBytes,
                pictureName != null ? pictureName : "profile.jpg",
                pictureType,
                new ApiCallback<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        registerResult.setValue(Resource.success(null));
                    }

                    @Override
                    public void onError(String message) {
                        // The account itself was created; only the picture failed.
                        registerResult.setValue(Resource.success(
                                "Account created, but the profile picture couldn't be uploaded."));
                    }
                });
    }

    private String validate(String email, String password, String confirmPassword,
                            String firstName, String lastName, String address, String phone) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)
                || TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName)
                || TextUtils.isEmpty(address) || TextUtils.isEmpty(phone)) {
            return "Please fill in all required fields.";
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            return "Please enter a valid email address.";
        }
        if (firstName.trim().length() < 2 || lastName.trim().length() < 2) {
            return "First and last name must be at least 2 characters.";
        }
        if (password.length() < 8) {
            return "Password must be at least 8 characters.";
        }
        if (!password.equals(confirmPassword)) {
            return "Passwords do not match.";
        }
        if (!phone.trim().matches("\\d{10,}")) {
            return "Phone number must be at least 10 digits (numbers only).";
        }
        return null;
    }
}
