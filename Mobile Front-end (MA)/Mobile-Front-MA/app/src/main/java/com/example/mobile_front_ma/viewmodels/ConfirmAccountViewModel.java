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
import com.example.mobile_front_ma.models.dto.ConfirmAccountRequest;
import com.example.mobile_front_ma.util.Resource;

/**
 * Drives account activation (spec 2.2.2): the user types the 6-digit code emailed after
 * registration to confirm their account before they can sign in.
 */
public class ConfirmAccountViewModel extends AndroidViewModel {

    private final AuthRepository repository = new AuthRepository();

    private final MutableLiveData<Resource<Void>> confirmResult = new MutableLiveData<>();

    public ConfirmAccountViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<Resource<Void>> getConfirmResult() {
        return confirmResult;
    }

    public void confirm(String email, String code) {
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            confirmResult.setValue(Resource.error("Please enter a valid email address."));
            return;
        }
        if (TextUtils.isEmpty(code)) {
            confirmResult.setValue(Resource.error("Please enter the code from your email."));
            return;
        }

        confirmResult.setValue(Resource.loading());
        ConfirmAccountRequest request = new ConfirmAccountRequest(email.trim(), code.trim());
        repository.confirmAccount(request, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                confirmResult.setValue(Resource.success(null));
            }

            @Override
            public void onError(String message) {
                confirmResult.setValue(Resource.error(message));
            }
        });
    }
}
