package com.example.mobile_front_ma.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.mobile_front_ma.R;
import com.example.mobile_front_ma.data.network.GeoApiClient;
import com.example.mobile_front_ma.models.dto.AccountDetails;
import com.example.mobile_front_ma.models.dto.RideDetails;
import com.example.mobile_front_ma.models.dto.RideNoteDto;
import com.example.mobile_front_ma.models.dto.RideRatingDto;
import com.example.mobile_front_ma.util.Resource;
import com.example.mobile_front_ma.viewmodels.RideDetailViewModel;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Detail view of a single ride (spec 2.9.1 / 2.9.3): a map with the marked route, the
 * driver (and passengers for admins), inconsistency reports, ratings, and the option to
 * order the same route again now or later.
 */
public class RideHistoryDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RIDE_ID = "ride_id";
    public static final String EXTRA_ADMIN = "admin";
    public static final String EXTRA_STATUS = "status";
    public static final String EXTRA_ROUTE_LATS = "route_lats";
    public static final String EXTRA_ROUTE_LONS = "route_lons";

    /** Result code returned to the history list when a ride was canceled, so it can refresh. */
    public static final int RESULT_RIDE_CANCELED = RESULT_FIRST_USER;

    /**
     * Feature flag: whether an administrator may cancel a ride from the detail screen.
     * The spec (2.5) only grants cancellation to the passenger who ordered the ride, so this
     * is {@code false} and admins never see the Cancel button. The backend still supports
     * admin cancellation, so flip this to {@code true} to bring the button back for admins.
     */
    private static final boolean ADMIN_CAN_CANCEL = false;

    private static final int ROUTE_PADDING_PX = 90;

    private RideDetailViewModel viewModel;
    private MapView map;
    private ProgressBar progressBar;
    private TextView driverText;
    private TextView passengersHeader;
    private TextView passengersText;
    private TextView reportsText;
    private TextView ratingsText;
    private Button cancelButton;

    private long rideId;
    private boolean adminMode;
    private String rideStatus;
    private List<GeoPoint> waypoints = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx,
                ctx.getSharedPreferences("osmdroid", Context.MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue(ctx.getPackageName());

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ride_history_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        rideId = getIntent().getLongExtra(EXTRA_RIDE_ID, -1);
        adminMode = getIntent().getBooleanExtra(EXTRA_ADMIN, false);
        rideStatus = getIntent().getStringExtra(EXTRA_STATUS);

        progressBar = findViewById(R.id.progressBar);
        driverText = findViewById(R.id.driverText);
        passengersHeader = findViewById(R.id.passengersHeader);
        passengersText = findViewById(R.id.passengersText);
        reportsText = findViewById(R.id.reportsText);
        ratingsText = findViewById(R.id.ratingsText);

        // Passengers are an admin-only field (spec 2.9.3); hide for the user view (2.9.1).
        if (!adminMode) {
            passengersHeader.setVisibility(View.GONE);
            passengersText.setVisibility(View.GONE);
        }

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        ((Button) findViewById(R.id.reorderNowButton)).setOnClickListener(v -> reorderNow());
        ((Button) findViewById(R.id.reorderLaterButton)).setOnClickListener(v -> reorderLater());

        // Cancel (spec 2.5) only makes sense for a ride that is still scheduled. For any other
        // status (active/finished/already canceled) the backend would reject it, so hide it.
        cancelButton = findViewById(R.id.cancelRideButton);
        cancelButton.setVisibility(isCancellable() ? View.VISIBLE : View.GONE);
        cancelButton.setOnClickListener(v -> confirmCancel());

        setupMap();
        drawRouteFromExtras();

        viewModel = new ViewModelProvider(this).get(RideDetailViewModel.class);
        viewModel.getDetails().observe(this, this::renderDetails);
        viewModel.getReorderResult().observe(this, this::renderReorder);
        viewModel.getCancelResult().observe(this, this::renderCancel);
        viewModel.load(rideId, adminMode);
    }

    private boolean isCancellable() {
        // Only scheduled rides can be canceled (spec 2.5).
        if (!"SCHEDULED".equalsIgnoreCase(rideStatus)) {
            return false;
        }
        // Admins don't get the button unless the feature flag is turned on.
        return !adminMode || ADMIN_CAN_CANCEL;
    }

    private void setupMap() {
        map = findViewById(R.id.detailMap);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.getController().setZoom(13.0);
    }

    private void drawRouteFromExtras() {
        double[] lats = getIntent().getDoubleArrayExtra(EXTRA_ROUTE_LATS);
        double[] lons = getIntent().getDoubleArrayExtra(EXTRA_ROUTE_LONS);
        waypoints = new ArrayList<>();
        if (lats != null && lons != null && lats.length == lons.length) {
            for (int i = 0; i < lats.length; i++) {
                waypoints.add(new GeoPoint(lats[i], lons[i]));
            }
        }
        if (waypoints.isEmpty()) {
            return;
        }

        // Place start/end markers immediately; fetch the real road geometry for the line.
        addMarker(waypoints.get(0), R.drawable.ic_marker_start);
        addMarker(waypoints.get(waypoints.size() - 1), R.drawable.ic_marker_end);
        fetchRoadRoute();
    }

    /** Ask OSRM for the driving geometry through the waypoints; straight line on failure. */
    private void fetchRoadRoute() {
        if (waypoints.size() < 2) {
            drawPolyline(waypoints);
            zoomToRoute(waypoints);
            return;
        }
        StringBuilder coords = new StringBuilder();
        for (int i = 0; i < waypoints.size(); i++) {
            GeoPoint p = waypoints.get(i);
            if (i > 0) {
                coords.append(';');
            }
            coords.append(String.format(Locale.US, "%f,%f", p.getLongitude(), p.getLatitude()));
        }

        GeoApiClient.osrm().route(coords.toString(), "full", "geojson")
                .enqueue(new Callback<com.example.mobile_front_ma.models.dto.OsrmRouteResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<com.example.mobile_front_ma.models.dto.OsrmRouteResponse> call,
                                           @NonNull Response<com.example.mobile_front_ma.models.dto.OsrmRouteResponse> response) {
                        List<GeoPoint> line = new ArrayList<>();
                        com.example.mobile_front_ma.models.dto.OsrmRouteResponse body = response.body();
                        if (response.isSuccessful() && body != null
                                && body.routes != null && !body.routes.isEmpty()
                                && body.routes.get(0).geometry != null
                                && body.routes.get(0).geometry.coordinates != null) {
                            for (List<Double> c : body.routes.get(0).geometry.coordinates) {
                                if (c.size() >= 2) {
                                    line.add(new GeoPoint(c.get(1), c.get(0))); // [lon,lat] -> lat,lon
                                }
                            }
                        }
                        if (line.isEmpty()) {
                            line = waypoints; // fall back to straight segments
                        }
                        drawPolyline(line);
                        zoomToRoute(line);
                    }

                    @Override
                    public void onFailure(@NonNull Call<com.example.mobile_front_ma.models.dto.OsrmRouteResponse> call,
                                          @NonNull Throwable t) {
                        drawPolyline(waypoints);
                        zoomToRoute(waypoints);
                    }
                });
    }

    private void drawPolyline(List<GeoPoint> points) {
        if (map == null || points.isEmpty()) {
            return;
        }
        Polyline line = new Polyline(map);
        line.setPoints(points);
        line.getOutlinePaint().setColor(ContextCompat.getColor(this, R.color.route_line));
        line.getOutlinePaint().setStrokeWidth(12f);
        map.getOverlays().add(line);
        map.invalidate();
    }

    private void zoomToRoute(List<GeoPoint> points) {
        if (map == null || points.isEmpty()) {
            return;
        }
        final BoundingBox box = BoundingBox.fromGeoPoints(points);
        map.post(() -> map.zoomToBoundingBox(box, false, ROUTE_PADDING_PX));
    }

    private void addMarker(GeoPoint position, int iconRes) {
        Marker marker = new Marker(map);
        marker.setPosition(position);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setIcon(ContextCompat.getDrawable(this, iconRes));
        map.getOverlays().add(marker);
    }

    private void renderDetails(Resource<RideDetails> resource) {
        if (resource == null) {
            return;
        }
        if (resource.status == Resource.Status.LOADING) {
            progressBar.setVisibility(View.VISIBLE);
            return;
        }
        progressBar.setVisibility(View.GONE);
        if (resource.status == Resource.Status.ERROR) {
            Toast.makeText(this, resource.message, Toast.LENGTH_LONG).show();
            return;
        }

        RideDetails d = resource.data;
        if (d == null) {
            return;
        }
        bindDriver(d.driver);
        if (adminMode) {
            bindPassengers(d.passengers);
        }
        bindReports(d.rideNotes);
        bindRatings(d.rideDriverRatings);
    }

    private void bindDriver(AccountDetails driver) {
        if (driver == null || (driver.fullName().isEmpty() && driver.email == null)) {
            driverText.setText(R.string.hor_detail_no_driver);
            return;
        }
        String name = driver.fullName().isEmpty() ? "—" : driver.fullName();
        driverText.setText(name + (driver.email != null ? "\n" + driver.email : ""));
    }

    private void bindPassengers(List<AccountDetails> passengers) {
        if (passengers == null || passengers.isEmpty()) {
            passengersText.setText(R.string.hor_detail_no_passengers);
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (AccountDetails p : passengers) {
            if (sb.length() > 0) {
                sb.append('\n');
            }
            String name = p.fullName().isEmpty() ? "—" : p.fullName();
            sb.append("• ").append(name);
            if (p.email != null) {
                sb.append(" (").append(p.email).append(')');
            }
        }
        passengersText.setText(sb.toString());
    }

    private void bindReports(List<RideNoteDto> notes) {
        if (notes == null || notes.isEmpty()) {
            reportsText.setText(R.string.hor_detail_no_reports);
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (RideNoteDto n : notes) {
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append("• ").append(n.note == null ? "" : n.note);
        }
        reportsText.setText(sb.toString());
    }

    private void bindRatings(List<RideRatingDto> ratings) {
        if (ratings == null || ratings.isEmpty()) {
            ratingsText.setText(R.string.hor_detail_no_ratings);
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (RideRatingDto r : ratings) {
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append(getString(R.string.hor_detail_rating_line, r.driverRating, r.vehicleRating));
            if (r.comment != null && !r.comment.trim().isEmpty()) {
                sb.append("\n  \"").append(r.comment.trim()).append('"');
            }
        }
        ratingsText.setText(sb.toString());
    }

    private void reorderNow() {
        viewModel.reorder(rideId, null);
    }

    private void reorderLater() {
        final Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (dp, year, month, day) -> {
            new TimePickerDialog(this, (tp, hour, minute) -> {
                c.set(year, month, day, hour, minute, 0);
                String iso = String.format(Locale.US, "%04d-%02d-%02dT%02d:%02d:00",
                        year, month + 1, day, hour, minute);
                viewModel.reorder(rideId, iso);
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void renderReorder(Resource<Void> resource) {
        if (resource == null || resource.status == Resource.Status.LOADING) {
            return;
        }
        if (resource.status == Resource.Status.SUCCESS) {
            Toast.makeText(this, R.string.hor_reorder_success, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, resource.message, Toast.LENGTH_LONG).show();
        }
    }

    /** Ask for confirmation before cancelling, since it can't be undone (spec 2.5). */
    private void confirmCancel() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.hor_cancel_confirm_title)
                .setMessage(R.string.hor_cancel_confirm_message)
                .setNegativeButton(R.string.hor_cancel_confirm_no, null)
                .setPositiveButton(R.string.hor_cancel_confirm_yes, (dialog, which) ->
                        viewModel.cancel(rideId, null))
                .show();
    }

    private void renderCancel(Resource<Void> resource) {
        if (resource == null) {
            return;
        }
        if (resource.status == Resource.Status.LOADING) {
            // Prevent a double-tap while the request is in flight.
            cancelButton.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
            return;
        }
        progressBar.setVisibility(View.GONE);
        if (resource.status == Resource.Status.SUCCESS) {
            Toast.makeText(this, R.string.hor_cancel_success, Toast.LENGTH_LONG).show();
            // The ride is no longer scheduled; tell the list to refresh and close this screen.
            setResult(RESULT_RIDE_CANCELED);
            finish();
        } else {
            // Cancellation failed (too late, not allowed, network, ...): surface why and let
            // the user try again.
            cancelButton.setEnabled(true);
            Toast.makeText(this, resource.message, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (map != null) {
            map.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (map != null) {
            map.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (map != null) {
            map.onDetach();
            map = null;
        }
    }
}
