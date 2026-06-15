package com.example.mobile_front_ma.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mobile_front_ma.data.AccountRepository;
import com.example.mobile_front_ma.data.network.ApiCallback;
import com.example.mobile_front_ma.models.dto.AccountListItem;
import com.example.mobile_front_ma.models.dto.PageResponse;
import com.example.mobile_front_ma.util.Resource;

import java.util.List;

/**
 * Backs the admin "whose history?" screen (spec 2.9.3): searches the account directory.
 */
public class AccountSearchViewModel extends AndroidViewModel {

    private final AccountRepository repository;
    private final MutableLiveData<Resource<List<AccountListItem>>> accounts = new MutableLiveData<>();

    public AccountSearchViewModel(@NonNull Application application) {
        super(application);
        this.repository = new AccountRepository(application);
    }

    public LiveData<Resource<List<AccountListItem>>> getAccounts() {
        return accounts;
    }

    public void search(String query) {
        accounts.setValue(Resource.loading());
        repository.search(query, new ApiCallback<PageResponse<AccountListItem>>() {
            @Override
            public void onSuccess(PageResponse<AccountListItem> data) {
                accounts.setValue(Resource.success(data.getContent()));
            }

            @Override
            public void onError(String message) {
                accounts.setValue(Resource.error(message));
            }
        });
    }
}
