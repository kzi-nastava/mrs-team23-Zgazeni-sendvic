package com.example.mobile_front_ma.viewmodels;

import android.app.Application;
import android.text.TextUtils;
import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mobile_front_ma.data.AuthRepository;
import com.example.mobile_front_ma.data.SessionManager;
import com.example.mobile_front_ma.data.network.ApiCallback;
import com.example.mobile_front_ma.models.dto.LoginRequest;
import com.example.mobile_front_ma.models.dto.LoginResponse;
import com.example.mobile_front_ma.util.Resource;

/**
 * Holds the login screen state. Validates input, calls the backend through the
 * repository, and on success stores the session before reporting back to the UI.
 */
public class LoginViewModel extends AndroidViewModel {

    private final AuthRepository repository = new AuthRepository();
    private final SessionManager sessionManager;

    private final MutableLiveData<Resource<LoginResponse>> loginResult = new MutableLiveData<>();

    public LoginViewModel(@NonNull Application application) {
        super(application);
        sessionManager = new SessionManager(application);
    }

    public LiveData<Resource<LoginResponse>> getLoginResult() {
        return loginResult;
    }

    public boolean isAlreadyLoggedIn() {
        return sessionManager.isLoggedIn();
    }

    public void login(String email, String password) {
        String validationError = validate(email, password);
        if (validationError != null) {
            loginResult.setValue(Resource.error(validationError));
            return;
        }

        loginResult.setValue(Resource.loading());
        repository.login(new LoginRequest(email.trim(), password), new ApiCallback<LoginResponse>() {
            @Override
            public void onSuccess(LoginResponse data) {
                sessionManager.saveSession(data);
                loginResult.setValue(Resource.success(data));
            }

            @Override
            public void onError(String message) {
                loginResult.setValue(Resource.error(message));
            }
        });
    }

    private String validate(String email, String password) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            return "Please enter both email and password.";
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            return "Please enter a valid email address.";
        }
        return null;
    }
}
