package com.example.mobile_front_ma.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mobile_front_ma.data.HistoryRepository;
import com.example.mobile_front_ma.data.network.ApiCallback;
import com.example.mobile_front_ma.models.dto.RideReport;
import com.example.mobile_front_ma.util.Resource;

public class ReportsViewModel extends AndroidViewModel {

    private final HistoryRepository repository;

    private final MutableLiveData<Resource<RideReport>> report =
            new MutableLiveData<>();

    private String fromDate;
    private String toDate;

    private Long targetUserId;

    public ReportsViewModel(@NonNull Application application) {
        super(application);
        repository = new HistoryRepository(application);
    }

    public LiveData<Resource<RideReport>> getReport() {
        return report;
    }

    public void setTargetUser(Long targetUserId) {
        this.targetUserId = targetUserId;
    }

    public void setDateRange(String from, String to) {
        this.fromDate = from;
        this.toDate = to;
    }

    public void load() {

        report.setValue(Resource.loading());

        repository.getReport(
                targetUserId,
                fromDate,
                toDate,
                new ApiCallback<RideReport>() {

                    @Override
                    public void onSuccess(RideReport data) {
                        report.setValue(Resource.success(data));
                    }

                    @Override
                    public void onError(String message) {
                        report.setValue(Resource.error(message));
                    }
                }
        );
    }
}