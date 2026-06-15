package com.example.mobile_front_ma.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mobile_front_ma.data.HistoryRepository;
import com.example.mobile_front_ma.data.network.ApiCallback;
import com.example.mobile_front_ma.models.dto.RideDetails;
import com.example.mobile_front_ma.util.Resource;

/**
 * Backs the ride detail screen (spec 2.9.1 / 2.9.3): loads the driver, passengers,
 * inconsistency reports and ratings for one ride, and re-orders the same route.
 */
public class RideDetailViewModel extends AndroidViewModel {

    private final HistoryRepository repository;
    private final MutableLiveData<Resource<RideDetails>> details = new MutableLiveData<>();
    private final MutableLiveData<Resource<Void>> reorderResult = new MutableLiveData<>();

    public RideDetailViewModel(@NonNull Application application) {
        super(application);
        this.repository = new HistoryRepository(application);
    }

    public LiveData<Resource<RideDetails>> getDetails() {
        return details;
    }

    public LiveData<Resource<Void>> getReorderResult() {
        return reorderResult;
    }

    public void load(long rideId, boolean adminMode) {
        details.setValue(Resource.loading());
        ApiCallback<RideDetails> callback = new ApiCallback<RideDetails>() {
            @Override
            public void onSuccess(RideDetails data) {
                details.setValue(Resource.success(data));
            }

            @Override
            public void onError(String message) {
                details.setValue(Resource.error(message));
            }
        };
        if (adminMode) {
            repository.getAdminRideDetails(rideId, callback);
        } else {
            repository.getUserRideDetails(rideId, callback);
        }
    }

    /** Re-order the route. {@code scheduledIso} = null orders now; otherwise schedules it. */
    public void reorder(long rideId, String scheduledIso) {
        reorderResult.setValue(Resource.loading());
        repository.reorder(rideId, scheduledIso, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                reorderResult.setValue(Resource.success(null));
            }

            @Override
            public void onError(String message) {
                reorderResult.setValue(Resource.error(message));
            }
        });
    }
}
