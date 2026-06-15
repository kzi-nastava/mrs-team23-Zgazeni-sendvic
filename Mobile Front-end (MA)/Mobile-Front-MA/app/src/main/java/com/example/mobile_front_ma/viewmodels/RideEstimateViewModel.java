package com.example.mobile_front_ma.viewmodels;

import java.util.Collections;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mobile_front_ma.data.GeoRepository;
import com.example.mobile_front_ma.data.network.ApiCallback;
import com.example.mobile_front_ma.models.Place;
import com.example.mobile_front_ma.models.RouteEstimate;
import com.example.mobile_front_ma.util.Resource;

/**
 * Holds the ride-estimation screen state (spec 2.1.2). Drives the start/destination
 * autocomplete suggestions and, once both points are chosen, the route + estimated time.
 */
public class RideEstimateViewModel extends ViewModel {

    private final GeoRepository repository = new GeoRepository();

    private final MutableLiveData<List<Place>> startSuggestions = new MutableLiveData<>();
    private final MutableLiveData<List<Place>> destinationSuggestions = new MutableLiveData<>();
    private final MutableLiveData<Resource<RouteEstimate>> routeEstimate = new MutableLiveData<>();

    public LiveData<List<Place>> getStartSuggestions() {
        return startSuggestions;
    }

    public LiveData<List<Place>> getDestinationSuggestions() {
        return destinationSuggestions;
    }

    public LiveData<Resource<RouteEstimate>> getRouteEstimate() {
        return routeEstimate;
    }

    public void searchStart(String query) {
        search(query, startSuggestions);
    }

    public void searchDestination(String query) {
        search(query, destinationSuggestions);
    }

    private void search(String query, MutableLiveData<List<Place>> target) {
        repository.searchPlaces(query, new ApiCallback<List<Place>>() {
            @Override
            public void onSuccess(List<Place> data) {
                target.setValue(data);
            }

            @Override
            public void onError(String message) {
                // Autocomplete stays quiet on failure: just show no suggestions.
                target.setValue(Collections.emptyList());
            }
        });
    }

    public void estimate(Place start, Place destination) {
        routeEstimate.setValue(Resource.loading());
        repository.estimateRoute(start, destination, new ApiCallback<RouteEstimate>() {
            @Override
            public void onSuccess(RouteEstimate data) {
                routeEstimate.setValue(Resource.success(data));
            }

            @Override
            public void onError(String message) {
                routeEstimate.setValue(Resource.error(message));
            }
        });
    }
}
