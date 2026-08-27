package com.example.mobile_front_ma.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mobile_front_ma.data.AccountRepository;
import com.example.mobile_front_ma.data.network.ApiCallback;
import com.example.mobile_front_ma.models.dto.UpdateAccountDTO;
import com.example.mobile_front_ma.util.Resource;

public class EditProfileViewModel extends AndroidViewModel {

    private final AccountRepository repository;

    private final MutableLiveData<Resource<String>> updateResult =
            new MutableLiveData<>();

    public EditProfileViewModel(@NonNull Application application) {
        super(application);
        repository = new AccountRepository(application);
    }

    public LiveData<Resource<String>> getUpdateResult() {
        return updateResult;
    }

    public void updateProfile(UpdateAccountDTO request) {

        updateResult.setValue(Resource.loading());

        repository.updateMe(
                request,
                new ApiCallback<String>() {

                    @Override
                    public void onSuccess(String data) {
                        updateResult.postValue(
                                Resource.success(data)
                        );
                    }

                    @Override
                    public void onError(String message) {
                        updateResult.postValue(
                                Resource.error(message)
                        );
                    }
                }
        );
    }
}
