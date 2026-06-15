package com.example.mobile_front_ma.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mobile_front_ma.data.HistoryRepository;
import com.example.mobile_front_ma.data.network.ApiCallback;
import com.example.mobile_front_ma.models.dto.PageResponse;
import com.example.mobile_front_ma.models.dto.RideHistoryItem;
import com.example.mobile_front_ma.util.Resource;

import java.util.List;

/**
 * Shared ViewModel for both ride-history screens:
 *  - registered user (spec 2.9.1): {@link #initUser()}
 *  - administrator   (spec 2.9.3): {@link #initAdmin(long)} with the target account id
 *
 * Holds the current sort field/direction and date-range filter and reloads the list when
 * they change. Default sort is newest-first by creation date, as the spec requires.
 */
public class RideHistoryViewModel extends AndroidViewModel {

    public static final String SORT_CREATION = "creationDate";
    public static final String SORT_START = "startTime";
    public static final String SORT_END = "endTime";
    public static final String SORT_START_LOC = "startLatitude";
    public static final String SORT_END_LOC = "endLatitude";
    public static final String SORT_PRICE = "price";
    public static final String SORT_STATUS = "status";

    public static final String DIR_ASC = "asc";
    public static final String DIR_DESC = "desc";

    // We show all of a user's rides on one screen for the demo, so request a generous page.
    private static final int PAGE_SIZE = 100;

    private final HistoryRepository repository;
    private final MutableLiveData<Resource<List<RideHistoryItem>>> rides = new MutableLiveData<>();

    private boolean adminMode = false;
    private long targetId = -1;

    private String sortField = SORT_CREATION;
    private String sortDir = DIR_DESC;
    private String fromDate = null;
    private String toDate = null;

    public RideHistoryViewModel(@NonNull Application application) {
        super(application);
        this.repository = new HistoryRepository(application);
    }

    public void initUser() {
        this.adminMode = false;
        load();
    }

    public void initAdmin(long targetAccountId) {
        this.adminMode = true;
        this.targetId = targetAccountId;
        load();
    }

    public LiveData<Resource<List<RideHistoryItem>>> getRides() {
        return rides;
    }

    public String getSortField() {
        return sortField;
    }

    public String getSortDir() {
        return sortDir;
    }

    public boolean isAdminMode() {
        return adminMode;
    }

    /** Apply a new sort field/direction and reload. */
    public void setSort(String field, String dir) {
        this.sortField = field;
        this.sortDir = dir;
        load();
    }

    /**
     * Shake gesture handler (spec 2.9.1): alternately sort the history by date. Forces the
     * sort field back to creation date and flips the direction each time.
     */
    public void toggleDateSort() {
        this.sortField = SORT_CREATION;
        this.sortDir = DIR_DESC.equals(sortDir) ? DIR_ASC : DIR_DESC;
        load();
    }

    /** Set the creation-date range filter (ISO datetime strings, either may be null) and reload. */
    public void setDateFilter(String fromIso, String toIso) {
        this.fromDate = fromIso;
        this.toDate = toIso;
        load();
    }

    public void clearDateFilter() {
        this.fromDate = null;
        this.toDate = null;
        load();
    }

    public void load() {
        rides.setValue(Resource.loading());
        String sort = sortField + "," + sortDir;

        ApiCallback<PageResponse<RideHistoryItem>> callback =
                new ApiCallback<PageResponse<RideHistoryItem>>() {
                    @Override
                    public void onSuccess(PageResponse<RideHistoryItem> data) {
                        rides.setValue(Resource.success(data.getContent()));
                    }

                    @Override
                    public void onError(String message) {
                        rides.setValue(Resource.error(message));
                    }
                };

        if (adminMode) {
            repository.getAdminHistory(targetId, 0, PAGE_SIZE, sort, fromDate, toDate, callback);
        } else {
            repository.getUserHistory(0, PAGE_SIZE, sort, fromDate, toDate, callback);
        }
    }
}
