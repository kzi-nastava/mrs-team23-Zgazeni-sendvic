package com.example.mobile_front_ma.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mobile_front_ma.data.RouteRepository;
import com.example.mobile_front_ma.data.network.ApiCallback;
import com.example.mobile_front_ma.models.dto.RouteResponse;
import com.example.mobile_front_ma.util.Resource;

import java.util.List;

public class FavoriteRoutesViewModel extends AndroidViewModel {

    private final RouteRepository repository;

    private final MutableLiveData<Resource<List<RouteResponse>>> routes =
            new MutableLiveData<>();

    public FavoriteRoutesViewModel(@NonNull Application application) {
        super(application);
        repository = new RouteRepository(application);
    }

    public LiveData<Resource<List<RouteResponse>>> getRoutes() {
        return routes;
    }

    public void load() {
        routes.setValue(Resource.loading());

        repository.getFavorites(new ApiCallback<List<RouteResponse>>() {

            @Override
            public void onSuccess(List<RouteResponse> data) {
                routes.setValue(Resource.success(data));
            }

            @Override
            public void onError(String message) {
                routes.setValue(Resource.error(message));
            }
        });
    }

    public void delete(long routeId) {
        repository.deleteFavorite(
                routeId,
                new ApiCallback<Void>() {

                    @Override
                    public void onSuccess(Void ignored) {
                        load();
                    }

                    @Override
                    public void onError(String message) {
                        routes.setValue(Resource.error(message));
                    }
                }
        );
    }
}