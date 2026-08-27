package com.example.mobile_front_ma.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mobile_front_ma.data.AccountRepository;
import com.example.mobile_front_ma.data.network.ApiCallback;
import com.example.mobile_front_ma.models.dto.GetAccountDTO;
import com.example.mobile_front_ma.util.Resource;

public class ProfileViewModel extends AndroidViewModel {

    private final AccountRepository repository;

    private final MutableLiveData<Resource<GetAccountDTO>> profile =
            new MutableLiveData<>();

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        repository = new AccountRepository(application);
    }

    public LiveData<Resource<GetAccountDTO>> getProfile() {
        return profile;
    }

    public void loadProfile() {
        profile.setValue(Resource.loading());

        repository.getMe(new ApiCallback<>() {
            @Override
            public void onSuccess(GetAccountDTO data) {
                profile.postValue(Resource.success(data));
            }

            @Override
            public void onError(String message) {
                profile.postValue(Resource.error(message));
            }
        });
    }
}