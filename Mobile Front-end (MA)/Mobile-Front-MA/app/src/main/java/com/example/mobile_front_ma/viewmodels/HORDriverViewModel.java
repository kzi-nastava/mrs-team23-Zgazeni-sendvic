package com.example.mobile_front_ma.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mobile_front_ma.data.HORRepository;
import com.example.mobile_front_ma.models.Ride;

import java.util.List;

public class HORDriverViewModel extends ViewModel {
    private final MutableLiveData<List<Ride>> ridesLiveData = new MutableLiveData<>();
    private final HORRepository repository = new HORRepository();

    public HORDriverViewModel() {
        loadTestRides();
    }

    private void loadTestRides() {
        ridesLiveData.setValue(repository.getTestRides());
    }

    public LiveData<List<Ride>> getRidesLiveData() {
        return ridesLiveData;
    }
}
