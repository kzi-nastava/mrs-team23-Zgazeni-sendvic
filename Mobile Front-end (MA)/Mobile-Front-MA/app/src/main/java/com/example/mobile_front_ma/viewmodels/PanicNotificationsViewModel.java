package com.example.mobile_front_ma.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mobile_front_ma.data.PanicRepository;
import com.example.mobile_front_ma.data.network.ApiCallback;
import com.example.mobile_front_ma.models.dto.PageResponse;
import com.example.mobile_front_ma.models.dto.PanicResponse;
import com.example.mobile_front_ma.util.Resource;

import java.util.ArrayList;
import java.util.List;

/**
 * Backs the admin panic-notifications screen (spec 2.6.3): loads every stored panic alert,
 * resolves them, and folds in live socket events while the screen is open. Holds the current
 * list so live updates and resolves can be applied without a full reload.
 */
public class PanicNotificationsViewModel extends AndroidViewModel {

    private static final int PAGE_SIZE = 100;

    private final PanicRepository repository;
    private final MutableLiveData<Resource<List<PanicResponse>>> panics = new MutableLiveData<>();
    private final MutableLiveData<Resource<PanicResponse>> resolveResult = new MutableLiveData<>();
    private final List<PanicResponse> current = new ArrayList<>();

    public PanicNotificationsViewModel(@NonNull Application application) {
        super(application);
        this.repository = new PanicRepository(application);
    }

    public LiveData<Resource<List<PanicResponse>>> getPanics() {
        return panics;
    }

    public LiveData<Resource<PanicResponse>> getResolveResult() {
        return resolveResult;
    }

    public void load() {
        panics.setValue(Resource.loading());
        repository.getPanics(0, PAGE_SIZE, new ApiCallback<PageResponse<PanicResponse>>() {
            @Override
            public void onSuccess(PageResponse<PanicResponse> data) {
                current.clear();
                if (data != null && data.content != null) {
                    current.addAll(data.content);
                }
                publish();
            }

            @Override
            public void onError(String message) {
                panics.setValue(Resource.error(message));
            }
        });
    }

    public void resolve(long panicId) {
        resolveResult.setValue(Resource.loading());
        repository.resolve(panicId, new ApiCallback<PanicResponse>() {
            @Override
            public void onSuccess(PanicResponse data) {
                applyResolved(data);
                resolveResult.setValue(Resource.success(data));
            }

            @Override
            public void onError(String message) {
                resolveResult.setValue(Resource.error(message));
            }
        });
    }

    /** A new panic arrived over the socket while the screen is open: put it on top. */
    public void onLivePanicCreated(PanicResponse panic) {
        if (panic == null) {
            return;
        }
        removeById(panic.id);
        current.add(0, panic);
        publish();
    }

    /** A resolution arrived over the socket: update the matching row in place. */
    public void onLivePanicResolved(PanicResponse panic) {
        applyResolved(panic);
    }

    private void applyResolved(PanicResponse resolved) {
        if (resolved == null || resolved.id == null) {
            return;
        }
        boolean found = false;
        for (int i = 0; i < current.size(); i++) {
            PanicResponse p = current.get(i);
            if (p.id != null && p.id.equals(resolved.id)) {
                current.set(i, resolved);
                found = true;
                break;
            }
        }
        if (found) {
            publish();
        }
    }

    private void removeById(Long id) {
        if (id == null) {
            return;
        }
        for (int i = current.size() - 1; i >= 0; i--) {
            Long pid = current.get(i).id;
            if (pid != null && pid.equals(id)) {
                current.remove(i);
            }
        }
    }

    private void publish() {
        panics.setValue(Resource.success(new ArrayList<>(current)));
    }
}
