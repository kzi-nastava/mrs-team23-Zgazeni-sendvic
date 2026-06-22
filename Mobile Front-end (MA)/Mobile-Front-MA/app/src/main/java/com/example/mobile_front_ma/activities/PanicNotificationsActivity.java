package com.example.mobile_front_ma.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile_front_ma.R;
import com.example.mobile_front_ma.adapters.PanicNotificationAdapter;
import com.example.mobile_front_ma.data.realtime.PanicRealtimeManager;
import com.example.mobile_front_ma.models.dto.PanicResponse;
import com.example.mobile_front_ma.util.Resource;
import com.example.mobile_front_ma.viewmodels.PanicNotificationsViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin screen listing all panic alerts (spec 2.6.3). Loads the stored alerts from the backend
 * so an admin who was offline still catches up, lets the admin resolve them, and — while the
 * screen is visible — folds in live alerts pushed over the panic WebSocket.
 */
public class PanicNotificationsActivity extends AppCompatActivity
        implements PanicNotificationAdapter.OnResolveListener, PanicRealtimeManager.Observer {

    private PanicNotificationsViewModel viewModel;
    private PanicNotificationAdapter adapter;
    private ProgressBar progressBar;
    private TextView emptyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_panic_notifications);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        progressBar = findViewById(R.id.progressBar);
        emptyText = findViewById(R.id.emptyText);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        adapter = new PanicNotificationAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        findViewById(R.id.refreshButton).setOnClickListener(v -> viewModel.load());

        viewModel = new ViewModelProvider(this).get(PanicNotificationsViewModel.class);
        viewModel.getPanics().observe(this, this::render);
        viewModel.getResolveResult().observe(this, this::renderResolve);
        viewModel.load();
    }

    private void render(Resource<List<PanicResponse>> resource) {
        if (resource == null) {
            return;
        }
        switch (resource.status) {
            case LOADING:
                progressBar.setVisibility(View.VISIBLE);
                emptyText.setVisibility(View.GONE);
                break;
            case SUCCESS:
                progressBar.setVisibility(View.GONE);
                List<PanicResponse> data = resource.data == null ? new ArrayList<>() : resource.data;
                adapter.submit(data);
                emptyText.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
                break;
            case ERROR:
                progressBar.setVisibility(View.GONE);
                adapter.submit(new ArrayList<>());
                emptyText.setVisibility(View.VISIBLE);
                Toast.makeText(this, resource.message, Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void renderResolve(Resource<PanicResponse> resource) {
        if (resource == null) {
            return;
        }
        if (resource.status == Resource.Status.SUCCESS) {
            Toast.makeText(this, R.string.panic_resolved_toast, Toast.LENGTH_SHORT).show();
        } else if (resource.status == Resource.Status.ERROR) {
            Toast.makeText(this, resource.message, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResolve(PanicResponse panic) {
        if (panic == null || panic.id == null) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.panic_resolve_confirm_title)
                .setMessage(R.string.panic_resolve_confirm_message)
                .setNegativeButton(R.string.panic_resolve_confirm_no, null)
                .setPositiveButton(R.string.panic_resolve_confirm_yes,
                        (dialog, which) -> viewModel.resolve(panic.id))
                .show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Receive live alerts while this screen is visible (in addition to system notifications).
        PanicRealtimeManager manager = PanicRealtimeManager.get();
        if (manager != null) {
            manager.addObserver(this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        PanicRealtimeManager manager = PanicRealtimeManager.get();
        if (manager != null) {
            manager.removeObserver(this);
        }
    }

    // ---- PanicRealtimeManager.Observer (called on the main thread) ----

    @Override
    public void onPanicCreated(PanicResponse panic) {
        viewModel.onLivePanicCreated(panic);
    }

    @Override
    public void onPanicResolved(PanicResponse panic) {
        viewModel.onLivePanicResolved(panic);
    }
}
