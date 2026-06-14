package com.example.mobile_front_ma.viewmodels;

import android.app.Application;
import android.text.TextUtils;
import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mobile_front_ma.data.AuthRepository;
import com.example.mobile_front_ma.data.network.ApiCallback;
import com.example.mobile_front_ma.models.dto.ForgotPasswordRequest;
import com.example.mobile_front_ma.models.dto.ResetPasswordRequest;
import com.example.mobile_front_ma.util.Resource;

/**
 * Drives the two-step password reset (spec 2.2.1): first request a code by email, then
 * submit that code together with the new password. Validation mirrors the backend so
 * the user gets instant feedback.
 */
public class ForgotPasswordViewModel extends AndroidViewModel {

    private final AuthRepository repository = new AuthRepository();

    private final MutableLiveData<Resource<Void>> sendCodeResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<Void>> resetResult = new MutableLiveData<>();

    public ForgotPasswordViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<Resource<Void>> getSendCodeResult() {
        return sendCodeResult;
    }

    public LiveData<Resource<Void>> getResetResult() {
        return resetResult;
    }

    /** Step 1: ask the backend to email a reset code to this address. */
    public void sendCode(String email) {
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            sendCodeResult.setValue(Resource.error("Please enter a valid email address."));
            return;
        }

        sendCodeResult.setValue(Resource.loading());
        repository.forgotPassword(new ForgotPasswordRequest(email.trim()), new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                sendCodeResult.setValue(Resource.success(null));
            }

            @Override
            public void onError(String message) {
                sendCodeResult.setValue(Resource.error(message));
            }
        });
    }

    /** Step 2: submit the emailed code and the chosen new password. */
    public void resetPassword(String email, String code, String newPassword, String confirmPassword) {
        String error = validate(code, newPassword, confirmPassword);
        if (error != null) {
            resetResult.setValue(Resource.error(error));
            return;
        }

        resetResult.setValue(Resource.loading());
        ResetPasswordRequest request = new ResetPasswordRequest(email.trim(), code.trim(), newPassword);
        repository.resetPassword(request, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                resetResult.setValue(Resource.success(null));
            }

            @Override
            public void onError(String message) {
                resetResult.setValue(Resource.error(message));
            }
        });
    }

    private String validate(String code, String newPassword, String confirmPassword) {
        if (TextUtils.isEmpty(code)) {
            return "Please enter the code from your email.";
        }
        if (TextUtils.isEmpty(newPassword)) {
            return "Please enter a new password.";
        }
        if (newPassword.length() < 8) {
            return "Password must be at least 8 characters.";
        }
        if (!newPassword.equals(confirmPassword)) {
            return "Passwords do not match.";
        }
        return null;
    }
}
