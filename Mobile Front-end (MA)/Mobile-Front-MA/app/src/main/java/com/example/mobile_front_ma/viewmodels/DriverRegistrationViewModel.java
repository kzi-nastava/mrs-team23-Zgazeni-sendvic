package com.example.mobile_front_ma.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mobile_front_ma.data.DriverRepository;
import com.example.mobile_front_ma.data.network.ApiCallback;
import com.example.mobile_front_ma.models.dto.CreateDriverRequest;
import com.example.mobile_front_ma.models.dto.CreatedDriverResponse;
import com.example.mobile_front_ma.models.dto.RegisterVehicleRequest;
import com.example.mobile_front_ma.models.dto.RegisteredVehicleResponse;
import com.example.mobile_front_ma.util.Resource;

public class DriverRegistrationViewModel extends AndroidViewModel {

    private final DriverRepository repository;

    private final MutableLiveData<Resource<CreatedDriverResponse>>
            registrationResult =
            new MutableLiveData<>();

    public DriverRegistrationViewModel(
            @NonNull Application application
    ) {
        super(application);
        repository = new DriverRepository(application);
    }

    public LiveData<Resource<CreatedDriverResponse>>
    getRegistrationResult() {
        return registrationResult;
    }

    public void registerDriver(
            RegisterVehicleRequest vehicleRequest,
            CreateDriverRequest driverRequest
    ) {

        registrationResult.setValue(
                Resource.loading()
        );

        /*
         * STEP 1:
         * Create the vehicle first.
         */
        repository.registerVehicle(
                vehicleRequest,
                new ApiCallback<RegisteredVehicleResponse>() {

                    @Override
                    public void onSuccess(
                            RegisteredVehicleResponse vehicle
                    ) {

                        /*
                         * We now have the vehicle ID required
                         * by CreateDriverDTO.
                         */
                        CreateDriverRequest finalRequest =
                                new CreateDriverRequest(
                                        driverRequest.getEmail(),
                                        driverRequest.getPassword(),
                                        driverRequest.getName(),
                                        driverRequest.getLastName(),
                                        driverRequest.getAddress(),
                                        vehicle.getId(),
                                        driverRequest.getPhoneNumber(),
                                        driverRequest.getImgString()
                                );

                        /*
                         * STEP 2:
                         * Create the driver.
                         */
                        repository.createDriver(
                                finalRequest,
                                new ApiCallback<CreatedDriverResponse>() {

                                    @Override
                                    public void onSuccess(
                                            CreatedDriverResponse data
                                    ) {
                                        registrationResult.postValue(
                                                Resource.success(data)
                                        );
                                    }

                                    @Override
                                    public void onError(
                                            String message
                                    ) {
                                        registrationResult.postValue(
                                                Resource.error(message)
                                        );
                                    }
                                }
                        );
                    }

                    @Override
                    public void onError(String message) {

                        registrationResult.postValue(
                                Resource.error(
                                        "Vehicle registration failed: "
                                                + message
                                )
                        );
                    }
                }
        );
    }
}