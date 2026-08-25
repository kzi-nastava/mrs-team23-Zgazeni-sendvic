package com.example.mobile_front_ma.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mobile_front_ma.data.GeoRepository;
import com.example.mobile_front_ma.data.RideRequestRepository;
import com.example.mobile_front_ma.data.network.ApiCallback;
import com.example.mobile_front_ma.models.LocationRequest;
import com.example.mobile_front_ma.models.Place;
import com.example.mobile_front_ma.models.RouteEstimate;
import com.example.mobile_front_ma.models.VehicleType;
import com.example.mobile_front_ma.models.dto.CreateRideRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RideOrderViewModel extends AndroidViewModel {

    private final GeoRepository geoRepository;
    private final RideRequestRepository rideRequestRepository;

    private final MutableLiveData<List<Place>> startSuggestions =
            new MutableLiveData<>();

    private final MutableLiveData<List<Place>> destinationSuggestions =
            new MutableLiveData<>();

    private final MutableLiveData<RouteEstimate> routeEstimate =
            new MutableLiveData<>();

    private final MutableLiveData<Boolean> orderResult =
            new MutableLiveData<>();

    private final MutableLiveData<String> orderError =
            new MutableLiveData<>();

    /*
     * Every new search receives a new generation number.
     *
     * If request #5 is sent after request #4, then request #4 is no
     * longer allowed to update the UI, even if its network response
     * arrives later.
     */
    private final AtomicInteger startSearchGeneration =
            new AtomicInteger(0);

    private final AtomicInteger destinationSearchGeneration =
            new AtomicInteger(0);

    public RideOrderViewModel(
            @NonNull Application application
    ) {
        super(application);

        geoRepository = new GeoRepository();

        rideRequestRepository =
                new RideRequestRepository(application);
    }

    public LiveData<List<Place>> getStartSuggestions() {
        return startSuggestions;
    }

    public LiveData<List<Place>> getDestinationSuggestions() {
        return destinationSuggestions;
    }

    public LiveData<RouteEstimate> getRouteEstimate() {
        return routeEstimate;
    }

    public LiveData<Boolean> getOrderResult() {
        return orderResult;
    }

    public LiveData<String> getOrderError() {
        return orderError;
    }

    /**
     * Searches for places using Nominatim.
     *
     * Each request gets a generation number. Only the most recent
     * request is allowed to publish its result.
     */
    public void searchPlaces(
            String query,
            boolean start
    ) {

        if (query == null) {
            return;
        }

        query = query.trim();

        if (query.length() < 3) {
            return;
        }

        /*
         * Give this request its own generation number.
         *
         * incrementAndGet() means that every new search automatically
         * invalidates all previous searches for the same field.
         */
        final int generation;

        if (start) {
            generation =
                    startSearchGeneration.incrementAndGet();
        } else {
            generation =
                    destinationSearchGeneration.incrementAndGet();
        }

        final String requestedQuery = query;

        geoRepository.searchPlaces(
                requestedQuery,
                new ApiCallback<List<Place>>() {

                    @Override
                    public void onSuccess(
                            List<Place> data
                    ) {

                        /*
                         * If another search has started since this
                         * request was created, ignore this response.
                         */
                        if (!isLatestSearch(
                                start,
                                generation
                        )) {
                            return;
                        }

                        if (start) {
                            startSuggestions.setValue(
                                    data != null
                                            ? data
                                            : new ArrayList<>()
                            );
                        } else {
                            destinationSuggestions.setValue(
                                    data != null
                                            ? data
                                            : new ArrayList<>()
                            );
                        }
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        /*
                         * Do not let an old failed request clear
                         * results belonging to a newer request.
                         */
                        if (!isLatestSearch(
                                start,
                                generation
                        )) {
                            return;
                        }

                        if (start) {
                            startSuggestions.setValue(
                                    new ArrayList<>()
                            );
                        } else {
                            destinationSuggestions.setValue(
                                    new ArrayList<>()
                            );
                        }
                    }
                }
        );
    }

    /**
     * Returns true only when the supplied request is still the
     * newest request for its input field.
     */
    private boolean isLatestSearch(
            boolean start,
            int generation
    ) {

        if (start) {
            return generation ==
                    startSearchGeneration.get();
        }

        return generation ==
                destinationSearchGeneration.get();
    }

    public void estimateRoute(
            List<Place> points
    ) {

        if (points == null || points.size() < 2) {
            return;
        }

        geoRepository.estimateRoute(
                points,
                new ApiCallback<RouteEstimate>() {

                    @Override
                    public void onSuccess(
                            RouteEstimate data
                    ) {
                        routeEstimate.setValue(data);
                    }

                    @Override
                    public void onError(
                            String message
                    ) {
                        orderError.setValue(message);
                    }
                }
        );
    }

    public RouteEstimate getCurrentRoute() {
        return routeEstimate.getValue();
    }

    public void createRideRequest(
            List<LocationRequest> locations,
            VehicleType vehicleType,
            boolean babiesAllowed,
            boolean petsAllowed,
            String scheduledTime,
            List<String> passengers,
            double distanceKm,
            double price
    ) {

        CreateRideRequest rideRequest =
                new CreateRideRequest();

        rideRequest.setLocations(locations);
        rideRequest.setVehicleType(vehicleType);
        rideRequest.setBabiesAllowed(babiesAllowed);
        rideRequest.setPetsAllowed(petsAllowed);
        rideRequest.setScheduledTime(scheduledTime);
        rideRequest.setInvitedPassengerEmails(passengers);
        rideRequest.setEstimatedDistanceKm(distanceKm);
        rideRequest.setEstimatedPrice(price);

        rideRequestRepository.createRideRequest(
                rideRequest,
                new ApiCallback<Void>() {

                    @Override
                    public void onSuccess(Void data) {
                        orderResult.postValue(true);
                    }

                    @Override
                    public void onError(String message) {
                        orderError.postValue(message);
                    }
                }
        );
    }
}