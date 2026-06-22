package com.example.mobile_front_ma.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mobile_front_ma.data.HistoryRepository;
import com.example.mobile_front_ma.data.RideRepository;
import com.example.mobile_front_ma.data.network.ApiCallback;
import com.example.mobile_front_ma.models.dto.PanicResponse;
import com.example.mobile_front_ma.models.dto.RideDetails;
import com.example.mobile_front_ma.util.Resource;

/**
 * Backs the ride detail screen (spec 2.9.1 / 2.9.3): loads the driver, passengers,
 * inconsistency reports and ratings for one ride, re-orders the same route, and cancels a
 * scheduled ride (spec 2.5).
 */
public class RideDetailViewModel extends AndroidViewModel {

    private final HistoryRepository repository;
    private final RideRepository rideRepository;
    private final MutableLiveData<Resource<RideDetails>> details = new MutableLiveData<>();
    private final MutableLiveData<Resource<Void>> reorderResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<Void>> cancelResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<PanicResponse>> panicResult = new MutableLiveData<>();

    public RideDetailViewModel(@NonNull Application application) {
        super(application);
        this.repository = new HistoryRepository(application);
        this.rideRepository = new RideRepository(application);
    }

    public LiveData<Resource<RideDetails>> getDetails() {
        return details;
    }

    public LiveData<Resource<Void>> getReorderResult() {
        return reorderResult;
    }

    public LiveData<Resource<Void>> getCancelResult() {
        return cancelResult;
    }

    public LiveData<Resource<PanicResponse>> getPanicResult() {
        return panicResult;
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

    /** Cancel a scheduled ride (spec 2.5). The backend authorizes the caller from the JWT. */
    public void cancel(long rideId, String reason) {
        cancelResult.setValue(Resource.loading());
        rideRepository.cancelRide(rideId, reason, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                cancelResult.setValue(Resource.success(null));
            }

            @Override
            public void onError(String message) {
                cancelResult.setValue(Resource.error(message));
            }
        });
    }

    /** Raise the PANIC alarm on an active ride (spec 2.6.3). Backend authorizes from the JWT. */
    public void panic(long rideId) {
        panicResult.setValue(Resource.loading());
        rideRepository.panicRide(rideId, new ApiCallback<PanicResponse>() {
            @Override
            public void onSuccess(PanicResponse data) {
                panicResult.setValue(Resource.success(data));
            }

            @Override
            public void onError(String message) {
                panicResult.setValue(Resource.error(message));
            }
        });
    }
}
