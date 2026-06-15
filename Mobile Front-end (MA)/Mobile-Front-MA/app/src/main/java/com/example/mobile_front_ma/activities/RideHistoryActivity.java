package com.example.mobile_front_ma.activities;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile_front_ma.R;
import com.example.mobile_front_ma.adapters.RideHistoryAdapter;
import com.example.mobile_front_ma.models.dto.LocationDto;
import com.example.mobile_front_ma.models.dto.RideHistoryItem;
import com.example.mobile_front_ma.util.Resource;
import com.example.mobile_front_ma.util.ShakeDetector;
import com.example.mobile_front_ma.viewmodels.RideHistoryViewModel;

import android.app.DatePickerDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Ride history screen, shared by:
 *  - registered user (spec 2.9.1): EXTRA_MODE = MODE_USER
 *  - administrator   (spec 2.9.3): EXTRA_MODE = MODE_ADMIN + EXTRA_TARGET_ID/NAME
 *
 * Lists past rides newest-first, lets the user sort by any field and filter by creation
 * date, and (user mode) re-sorts by date on a device shake. Tapping a ride opens the map
 * detail view.
 */
public class RideHistoryActivity extends AppCompatActivity
        implements RideHistoryAdapter.OnRideClickListener, ShakeDetector.OnShakeListener {

    public static final String EXTRA_MODE = "mode";
    public static final String EXTRA_TARGET_ID = "target_id";
    public static final String EXTRA_TARGET_NAME = "target_name";
    public static final String MODE_USER = "USER";
    public static final String MODE_ADMIN = "ADMIN";

    private RideHistoryViewModel viewModel;
    private RideHistoryAdapter adapter;

    private ProgressBar progressBar;
    private TextView emptyText;
    private TextView shakeHint;
    private Button sortDirButton;
    private Button fromDateButton;
    private Button toDateButton;

    private boolean adminMode;
    private String[] sortFields;     // backend field names, parallel to spinner entries

    private Calendar fromCal;
    private Calendar toCal;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private ShakeDetector shakeDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ride_history);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        adminMode = MODE_ADMIN.equals(getIntent().getStringExtra(EXTRA_MODE));

        progressBar = findViewById(R.id.progressBar);
        emptyText = findViewById(R.id.emptyText);
        shakeHint = findViewById(R.id.shakeHint);
        sortDirButton = findViewById(R.id.sortDirButton);
        fromDateButton = findViewById(R.id.fromDateButton);
        toDateButton = findViewById(R.id.toDateButton);

        viewModel = new ViewModelProvider(this).get(RideHistoryViewModel.class);
        viewModel.getRides().observe(this, this::render);

        setupTitleAndBack();
        setupRecyclerView();
        setupSortControls();
        setupDateFilter();
        setupShakeSensor();

        if (adminMode) {
            long targetId = getIntent().getLongExtra(EXTRA_TARGET_ID, -1);
            viewModel.initAdmin(targetId);
        } else {
            viewModel.initUser();
        }
    }

    private void setupTitleAndBack() {
        TextView title = findViewById(R.id.historyTitle);
        if (adminMode) {
            String name = getIntent().getStringExtra(EXTRA_TARGET_NAME);
            title.setText(name != null && !name.isEmpty()
                    ? getString(R.string.hor_admin_title_for, name)
                    : getString(R.string.hor_admin_title));
            // Shake-to-resort is a registered-user feature (spec 2.9.1).
            shakeHint.setVisibility(View.GONE);
        } else {
            title.setText(R.string.hor_user_title);
        }
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        adapter = new RideHistoryAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupSortControls() {
        String[] labels;
        if (adminMode) {
            labels = new String[]{
                    getString(R.string.hor_sort_creation),
                    getString(R.string.hor_sort_start_time),
                    getString(R.string.hor_sort_end_time),
                    getString(R.string.hor_sort_price),
                    getString(R.string.hor_sort_status)};
            sortFields = new String[]{
                    RideHistoryViewModel.SORT_CREATION,
                    RideHistoryViewModel.SORT_START,
                    RideHistoryViewModel.SORT_END,
                    RideHistoryViewModel.SORT_PRICE,
                    RideHistoryViewModel.SORT_STATUS};
        } else {
            labels = new String[]{
                    getString(R.string.hor_sort_creation),
                    getString(R.string.hor_sort_start_time),
                    getString(R.string.hor_sort_end_time),
                    getString(R.string.hor_sort_start_loc),
                    getString(R.string.hor_sort_end_loc)};
            sortFields = new String[]{
                    RideHistoryViewModel.SORT_CREATION,
                    RideHistoryViewModel.SORT_START,
                    RideHistoryViewModel.SORT_END,
                    RideHistoryViewModel.SORT_START_LOC,
                    RideHistoryViewModel.SORT_END_LOC};
        }

        Spinner spinner = findViewById(R.id.sortSpinner);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, labels);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            private boolean firstCallback = true;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Spinner fires once right after setAdapter; that initial selection already
                // matches the ViewModel's default, so skip it to avoid a redundant reload.
                if (firstCallback) {
                    firstCallback = false;
                    return;
                }
                viewModel.setSort(sortFields[position], viewModel.getSortDir());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        updateSortDirLabel();
        sortDirButton.setOnClickListener(v -> {
            String next = RideHistoryViewModel.DIR_DESC.equals(viewModel.getSortDir())
                    ? RideHistoryViewModel.DIR_ASC : RideHistoryViewModel.DIR_DESC;
            viewModel.setSort(viewModel.getSortField(), next);
            updateSortDirLabel();
        });
    }

    private void updateSortDirLabel() {
        boolean desc = RideHistoryViewModel.DIR_DESC.equals(viewModel.getSortDir());
        sortDirButton.setText(desc ? R.string.hor_sort_dir_desc : R.string.hor_sort_dir_asc);
    }

    private void setupDateFilter() {
        fromDateButton.setOnClickListener(v -> pickDate(true));
        toDateButton.setOnClickListener(v -> pickDate(false));

        findViewById(R.id.applyFilterButton).setOnClickListener(v -> applyDateFilter());
        findViewById(R.id.clearFilterButton).setOnClickListener(v -> {
            fromCal = null;
            toCal = null;
            fromDateButton.setText(R.string.hor_filter_from);
            toDateButton.setText(R.string.hor_filter_to);
            viewModel.clearDateFilter();
        });
    }

    private void pickDate(boolean isFrom) {
        Calendar initial = isFrom ? fromCal : toCal;
        if (initial == null) {
            initial = Calendar.getInstance();
        }
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, day) -> {
            Calendar c = Calendar.getInstance();
            c.set(year, month, day, 0, 0, 0);
            if (isFrom) {
                fromCal = c;
                fromDateButton.setText(formatDay(c));
            } else {
                toCal = c;
                toDateButton.setText(formatDay(c));
            }
        }, initial.get(Calendar.YEAR), initial.get(Calendar.MONTH), initial.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void applyDateFilter() {
        // From = start of the chosen day, To = end of the chosen day, as ISO datetimes the
        // backend now parses (ISO.DATE_TIME). Either bound may be left unset.
        String fromIso = fromCal == null ? null : isoDateTime(fromCal, 0, 0, 0);
        String toIso = toCal == null ? null : isoDateTime(toCal, 23, 59, 59);
        viewModel.setDateFilter(fromIso, toIso);
    }

    private String formatDay(Calendar c) {
        return String.format(Locale.US, "%04d-%02d-%02d",
                c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
    }

    private String isoDateTime(Calendar c, int h, int min, int s) {
        return String.format(Locale.US, "%04d-%02d-%02dT%02d:%02d:%02d",
                c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH),
                h, min, s);
    }

    private void setupShakeSensor() {
        if (adminMode) {
            return; // user-only feature
        }
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        shakeDetector = new ShakeDetector(this);
    }

    @Override
    public void onShake() {
        viewModel.toggleDateSort();
        updateSortDirLabel();
        Toast.makeText(this, R.string.hor_shake_hint, Toast.LENGTH_SHORT).show();
    }

    private void render(Resource<List<RideHistoryItem>> resource) {
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
                List<RideHistoryItem> data = resource.data == null
                        ? new ArrayList<>() : resource.data;
                adapter.submitList(data);
                emptyText.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
                break;
            case ERROR:
                progressBar.setVisibility(View.GONE);
                adapter.submitList(new ArrayList<>());
                emptyText.setVisibility(View.VISIBLE);
                Toast.makeText(this, resource.message, Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    public void onRideClick(RideHistoryItem ride) {
        if (ride.rideID == null) {
            return;
        }
        Intent intent = new Intent(this, RideHistoryDetailActivity.class);
        intent.putExtra(RideHistoryDetailActivity.EXTRA_RIDE_ID, ride.rideID);
        intent.putExtra(RideHistoryDetailActivity.EXTRA_ADMIN, adminMode);

        // Pass the route waypoints so the detail map can draw the route without a refetch.
        List<LocationDto> points = ride.destinations;
        if (points != null && !points.isEmpty()) {
            double[] lats = new double[points.size()];
            double[] lons = new double[points.size()];
            int n = 0;
            for (LocationDto p : points) {
                if (p != null && p.isValid()) {
                    lats[n] = p.getLatitude();
                    lons[n] = p.getLongitude();
                    n++;
                }
            }
            intent.putExtra(RideHistoryDetailActivity.EXTRA_ROUTE_LATS, java.util.Arrays.copyOf(lats, n));
            intent.putExtra(RideHistoryDetailActivity.EXTRA_ROUTE_LONS, java.util.Arrays.copyOf(lons, n));
        }
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(shakeDetector, accelerometer,
                    SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(shakeDetector);
        }
    }
}
