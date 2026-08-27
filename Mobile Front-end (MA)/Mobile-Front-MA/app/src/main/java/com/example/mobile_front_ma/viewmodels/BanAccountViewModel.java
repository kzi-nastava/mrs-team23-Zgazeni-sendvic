package com.example.mobile_front_ma.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mobile_front_ma.data.AccountRepository;
import com.example.mobile_front_ma.data.network.ApiCallback;
import com.example.mobile_front_ma.util.Resource;

public class BanAccountViewModel extends AndroidViewModel {

    private final AccountRepository repository;

    private final MutableLiveData<Resource<String>> banResult =
            new MutableLiveData<>();

    public BanAccountViewModel(@NonNull Application application) {
        super(application);
        repository = new AccountRepository(application);
    }

    public LiveData<Resource<String>> getBanResult() {
        return banResult;
    }

    public void banAccount(Long accountId, String reason) {

        if (accountId == null) {
            banResult.setValue(
                    Resource.error("Invalid account.")
            );
            return;
        }

        if (reason == null || reason.trim().isEmpty()) {
            banResult.setValue(
                    Resource.error("Please provide a reason for the ban.")
            );
            return;
        }

        banResult.setValue(Resource.loading());

        repository.banAccount(
                accountId,
                reason.trim(),
                new ApiCallback<String>() {

                    @Override
                    public void onSuccess(String message) {
                        banResult.postValue(
                                Resource.success(message)
                        );
                    }

                    @Override
                    public void onError(String message) {
                        banResult.postValue(
                                Resource.error(message)
                        );
                    }
                }
        );
    }
}