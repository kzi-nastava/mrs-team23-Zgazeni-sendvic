package com.example.mobile_front_ma.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.AndroidViewModel;

import com.example.mobile_front_ma.data.RideRepository;
import com.example.mobile_front_ma.data.network.ApiCallback;
import com.example.mobile_front_ma.models.Ride;
import com.example.mobile_front_ma.models.dto.DriverRideResponse;

import java.util.List;
import java.util.stream.Collectors;

public class HORDriverViewModel
        extends AndroidViewModel {

    private final RideRepository repository;

    private final MutableLiveData<List<Ride>>
            ridesLiveData =
            new MutableLiveData<>();

    private final MutableLiveData<Boolean>
            loading =
            new MutableLiveData<>(false);

    public HORDriverViewModel(
            @NonNull Application application
    ) {

        super(application);

        repository =
                new RideRepository(application);
    }

    public LiveData<List<Ride>> getRidesLiveData() {
        return ridesLiveData;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public void loadRides() {

        loading.setValue(true);

        repository.getDriverRides(
                new ApiCallback<List<DriverRideResponse>>() {

                    @Override
                    public void onSuccess(
                            List<DriverRideResponse> response
                    ) {

                        List<Ride> rides =
                                response.stream()
                                        .map(HORDriverViewModel::toRide)
                                        .collect(Collectors.toList());

                        ridesLiveData.postValue(rides);
                        loading.postValue(false);
                    }

                    @Override
                    public void onError(String message) {

                        loading.postValue(false);
                    }
                }
        );
    }

    private static Ride toRide(
            DriverRideResponse dto
    ) {

        return new Ride(
                dto.getRideId(),
                dto.getStartLocation(),
                dto.getEndLocation(),
                dto.getPrice() != null
                        ? String.valueOf(dto.getPrice())
                        : "0",
                dto.getDepartureTime(),
                dto.getStatus()
        );
    }

    public void startRide(long rideId) {

        repository.startRide(
                rideId,
                new ApiCallback<Void>() {

                    @Override
                    public void onSuccess(Void ignored) {

                        loadRides();
                    }

                    @Override
                    public void onError(String message) {

                        // We can expose this through another LiveData
                        // or Toast it from the Activity.
                    }
                }
        );
    }
}
