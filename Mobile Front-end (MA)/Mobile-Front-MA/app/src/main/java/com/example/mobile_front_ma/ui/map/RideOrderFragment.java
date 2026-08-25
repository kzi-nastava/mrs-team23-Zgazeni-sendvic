package com.example.mobile_front_ma.ui.map;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.mobile_front_ma.R;
import com.example.mobile_front_ma.activities.RideOrderActivity;
import com.example.mobile_front_ma.data.GeoRepository;
import com.example.mobile_front_ma.data.network.ApiCallback;
import com.example.mobile_front_ma.models.LatLng;
import com.example.mobile_front_ma.models.LocationRequest;
import com.example.mobile_front_ma.models.OrderStop;
import com.example.mobile_front_ma.models.Place;
import com.example.mobile_front_ma.models.RouteEstimate;
import com.example.mobile_front_ma.models.VehicleType;
import com.example.mobile_front_ma.models.dto.LocationDto;
import com.example.mobile_front_ma.models.dto.RouteResponse;
import com.example.mobile_front_ma.viewmodels.RideOrderViewModel;

import org.osmdroid.api.IMapController;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class RideOrderFragment extends Fragment {

    private static final double DEFAULT_ZOOM = 13.5;
    private static final int ROUTE_PADDING = 100;
    private static final double PRICE_PER_KM = 120.0;
    private static final long SEARCH_DELAY = 350;

    private boolean suppressInputWatcher = false;

    private int startSearchGeneration = 0;
    private int destinationSearchGeneration = 0;

    private MapView map;

    private AutoCompleteTextView startInput;
    private AutoCompleteTextView destinationInput;

    private Place selectedStart;
    private Place selectedDestination;

    private final List<OrderStop> stops = new ArrayList<>();

    private LinearLayout stopsContainer;
    private LinearLayout passengersContainer;

    private Spinner vehicleTypeSpinner;

    private CheckBox babiesCheckBox;
    private CheckBox petsCheckBox;

    private EditText passengerEmailInput;

    private RadioGroup scheduleGroup;
    private RadioButton nowRadio;
    private RadioButton scheduleRadio;

    private TimePicker scheduleTimePicker;

    private TextView distanceText;
    private TextView durationText;
    private TextView priceText;

    private Button orderRideButton;
    private ProgressBar progress;

    private PlaceSuggestionAdapter startAdapter;
    private PlaceSuggestionAdapter destinationAdapter;

    private RideOrderViewModel viewModel;

    private Polyline routeLine;

    private final List<Marker> routeMarkers = new ArrayList<>();

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable startSearchRunnable =
            () -> searchStart(startInput);

    private final Runnable destinationSearchRunnable =
            () -> searchDestination(destinationInput);

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(
                R.layout.fragment_ride_order,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        bindViews(view);
        setupMap();
        setupVehicleSpinner();
        setupInputs();
        setupScheduling();
        setupPassengers();

        viewModel = new ViewModelProvider(this)
                .get(RideOrderViewModel.class);

        observeViewModel();

        /*
         * If this screen was opened through "Order again"
         * from Favorite Routes, restore the saved route.
         */
        loadFavoriteRoute();
    }

    private void observeViewModel() {

        viewModel.getStartSuggestions().observe(
                getViewLifecycleOwner(),
                places -> {
                    startAdapter.setData(places);

                    if (!places.isEmpty()
                            && startInput.hasFocus()) {
                        startInput.showDropDown();
                    }
                }
        );

        viewModel.getDestinationSuggestions().observe(
                getViewLifecycleOwner(),
                places -> {
                    destinationAdapter.setData(places);

                    if (!places.isEmpty()
                            && destinationInput.hasFocus()) {
                        destinationInput.showDropDown();
                    }
                }
        );

        viewModel.getRouteEstimate().observe(
                getViewLifecycleOwner(),
                estimate -> {

                    if (estimate == null) {
                        return;
                    }

                    drawRoute(estimate);
                    showEstimate(estimate);
                }
        );

        viewModel.getOrderResult().observe(
                getViewLifecycleOwner(),
                success -> {

                    if (Boolean.TRUE.equals(success)) {

                        setLoading(false);

                        Toast.makeText(
                                requireContext(),
                                "Ride ordered successfully!",
                                Toast.LENGTH_LONG
                        ).show();

                        requireActivity().finish();
                    }
                }
        );

        viewModel.getOrderError().observe(
                getViewLifecycleOwner(),
                message -> {

                    if (message == null) {
                        return;
                    }

                    setLoading(false);

                    Toast.makeText(
                            requireContext(),
                            message,
                            Toast.LENGTH_LONG
                    ).show();
                }
        );
    }

    private void bindViews(View view) {

        map = view.findViewById(R.id.rideOrderMap);

        startInput = view.findViewById(R.id.orderStartInput);
        destinationInput = view.findViewById(R.id.orderDestinationInput);

        stopsContainer = view.findViewById(R.id.stopsContainer);
        passengersContainer = view.findViewById(R.id.passengersContainer);

        vehicleTypeSpinner = view.findViewById(R.id.vehicleTypeSpinner);

        babiesCheckBox = view.findViewById(R.id.babiesCheckBox);
        petsCheckBox = view.findViewById(R.id.petsCheckBox);

        passengerEmailInput = view.findViewById(R.id.passengerEmailInput);

        scheduleGroup = view.findViewById(R.id.scheduleGroup);
        nowRadio = view.findViewById(R.id.nowRadio);
        scheduleRadio = view.findViewById(R.id.scheduleRadio);

        scheduleTimePicker = view.findViewById(R.id.scheduleTimePicker);

        distanceText = view.findViewById(R.id.orderDistance);
        durationText = view.findViewById(R.id.orderDuration);
        priceText = view.findViewById(R.id.orderPrice);

        orderRideButton = view.findViewById(R.id.orderRideButton);
        progress = view.findViewById(R.id.orderProgress);

        view.findViewById(R.id.addStopButton)
                .setOnClickListener(v -> addStop());

        orderRideButton.setOnClickListener(v -> orderRide());
    }

    private void setupMap() {

        map.setTileSource(
                org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK
        );

        map.setMultiTouchControls(true);

        IMapController controller = map.getController();

        controller.setZoom(DEFAULT_ZOOM);

        controller.setCenter(
                new GeoPoint(
                        GeoRepository.NOVI_SAD_LAT,
                        GeoRepository.NOVI_SAD_LON
                )
        );
    }

    private void setupVehicleSpinner() {

        VehicleType[] types = VehicleType.values();

        ArrayAdapter<VehicleType> adapter =
                new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        types
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        vehicleTypeSpinner.setAdapter(adapter);
    }

    private void setupInputs() {

        startAdapter =
                new PlaceSuggestionAdapter(requireContext());

        destinationAdapter =
                new PlaceSuggestionAdapter(requireContext());

        startInput.setAdapter(startAdapter);
        destinationInput.setAdapter(destinationAdapter);

        /*
         * The AutoCompleteTextViews should not wait for Android's
         * internal filtering system to decide when to show results.
         */
        startInput.setThreshold(1);
        destinationInput.setThreshold(1);

        /*
         * START selection
         */
        startInput.setOnItemClickListener(
                (parent, view, position, id) -> {

                    Place place =
                            startAdapter.getItem(position);

                    if (place == null) {
                        return;
                    }

                    selectedStart = place;

                    handler.removeCallbacks(
                            startSearchRunnable
                    );

                    suppressInputWatcher = true;

                    startInput.setText(
                            place.getLabel(),
                            false
                    );

                    suppressInputWatcher = false;

                    startInput.dismissDropDown();

                    calculateRoute();
                }
        );

        /*
         * DESTINATION selection
         */
        destinationInput.setOnItemClickListener(
                (parent, view, position, id) -> {

                    Place place =
                            destinationAdapter.getItem(position);

                    if (place == null) {
                        return;
                    }

                    selectedDestination = place;

                    handler.removeCallbacks(
                            destinationSearchRunnable
                    );

                    suppressInputWatcher = true;

                    destinationInput.setText(
                            place.getLabel(),
                            false
                    );

                    suppressInputWatcher = false;

                    destinationInput.dismissDropDown();

                    calculateRoute();
                }
        );

        /*
         * START typing
         */
        startInput.addTextChangedListener(
                new SimpleTextWatcher() {

                    @Override
                    public void afterTextChanged(
                            android.text.Editable editable
                    ) {
                        if (suppressInputWatcher) {
                            return;
                        }

                        String query =
                                editable.toString().trim();

                        selectedStart = null;

                        handler.removeCallbacks(
                                startSearchRunnable
                        );

                        startSearchGeneration++;

                        if (query.length() < 3) {

                            startAdapter.setData(
                                    Collections.emptyList()
                            );

                            startInput.dismissDropDown();

                            return;
                        }

                        handler.postDelayed(
                                startSearchRunnable,
                                SEARCH_DELAY
                        );
                    }
                }
        );

        /*
         * DESTINATION typing
         */
        destinationInput.addTextChangedListener(
                new SimpleTextWatcher() {

                    @Override
                    public void afterTextChanged(
                            android.text.Editable editable
                    ) {
                        if (suppressInputWatcher) {
                            return;
                        }

                        String query =
                                editable.toString().trim();

                        selectedDestination = null;

                        handler.removeCallbacks(
                                destinationSearchRunnable
                        );

                        destinationSearchGeneration++;

                        if (query.length() < 3) {

                            destinationAdapter.setData(
                                    Collections.emptyList()
                            );

                            destinationInput.dismissDropDown();

                            return;
                        }

                        handler.postDelayed(
                                destinationSearchRunnable,
                                SEARCH_DELAY
                        );
                    }
                }
        );
    }

    private abstract static class SimpleTextWatcher
            implements android.text.TextWatcher {

        @Override
        public void beforeTextChanged(
                CharSequence s,
                int start,
                int count,
                int after
        ) {
        }

        @Override
        public void onTextChanged(
                CharSequence s,
                int start,
                int before,
                int count
        ) {
        }
    }

    private void searchStart(AutoCompleteTextView input) {

        final String query =
                input.getText()
                .toString()
                .trim();

        if (query.length() < 3) {
            return;
        }

        final int generation =
                startSearchGeneration;

        viewModel.searchPlaces(
                query,
                true
        );
    }

    private void searchDestination(
            AutoCompleteTextView input
    ) {

        final String query =
                input.getText()
                        .toString()
                        .trim();

        if (query.length() < 3) {
            return;
        }

        final int generation =
                destinationSearchGeneration;

        viewModel.searchPlaces(
                query,
                false
        );
    }

    private void addStop() {

        AutoCompleteTextView input =
                new AutoCompleteTextView(requireContext());

        input.setHint("Intermediate stop");
        input.setSingleLine(true);
        input.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT |
                        android.text.InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS
        );

        input.setLayoutParams(
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1
                )
        );

        PlaceSuggestionAdapter adapter =
                new PlaceSuggestionAdapter(requireContext());

        input.setAdapter(adapter);

        OrderStop stop = new OrderStop(input);

        LinearLayout row =
                new LinearLayout(requireContext());

        row.setOrientation(LinearLayout.HORIZONTAL);

        row.setGravity(
                android.view.Gravity.CENTER_VERTICAL
        );

        row.setPadding(0, 4, 0, 4);

        Button removeButton =
                new Button(requireContext());

        removeButton.setText("×");

        removeButton.setOnClickListener(v -> {

            stops.remove(stop);
            stopsContainer.removeView(row);

            calculateRoute();
        });

        row.addView(input);
        row.addView(removeButton);

        stopsContainer.addView(row);

        stops.add(stop);

        wireStopSearch(stop, adapter);
    }

    private void wireStopSearch(
            OrderStop stop,
            PlaceSuggestionAdapter adapter
    ) {

        AutoCompleteTextView input =
                stop.getInput();

        input.setAdapter(adapter);
        input.setThreshold(3);

        final Handler stopHandler =
                new Handler(Looper.getMainLooper());

        final Runnable[] searchRunnable =
                new Runnable[1];

        input.addTextChangedListener(
                new SimpleTextWatcher() {

                    @Override
                    public void afterTextChanged(
                            android.text.Editable editable
                    ) {
                        if (suppressInputWatcher) {
                            return;
                        }

                        stop.setPlace(null);

                        stopHandler.removeCallbacks(
                                searchRunnable[0]
                        );

                        String query =
                                editable.toString().trim();

                        if (query.length() < 3) {

                            adapter.setData(
                                    Collections.emptyList()
                            );

                            input.dismissDropDown();

                            return;
                        }

                        searchRunnable[0] = () ->
                                new GeoRepository().searchPlaces(
                                        query,
                                        new ApiCallback<List<Place>>() {

                                            @Override
                                            public void onSuccess(
                                                    List<Place> data
                                            ) {

                                                adapter.setData(data);

                                                if (input.hasFocus()
                                                        && !data.isEmpty()) {

                                                    input.showDropDown();
                                                }
                                            }

                                            @Override
                                            public void onError(
                                                    String message
                                            ) {

                                                adapter.setData(
                                                        Collections.emptyList()
                                                );
                                            }
                                        }
                                );

                        stopHandler.postDelayed(
                                searchRunnable[0],
                                SEARCH_DELAY
                        );
                    }
                }
        );

        input.setOnItemClickListener(
                (parent, view, position, id) -> {

                    Place place =
                            adapter.getItem(position);

                    if (place == null) {
                        return;
                    }

                    stop.setPlace(place);

                    /*
                     * setText() triggers the TextWatcher.
                     * Suppress it because this is a confirmed
                     * selection, not the user typing something new.
                     */
                    suppressInputWatcher = true;

                    input.setText(
                            place.getLabel(),
                            false
                    );

                    suppressInputWatcher = false;

                    input.dismissDropDown();

                    calculateRoute();
                }
        );
    }

    private final List<String> passengerEmails =
            new ArrayList<>();

    private void setupPassengers() {

        ((Button) requireView()
                .findViewById(R.id.addPassengerButton))
                .setOnClickListener(v -> {

                    String email =
                            passengerEmailInput
                                    .getText()
                                    .toString()
                                    .trim();

                    if (email.isEmpty()) {
                        passengerEmailInput.setError(
                                "Enter an email address"
                        );
                        return;
                    }

                    if (!android.util.Patterns.EMAIL_ADDRESS
                            .matcher(email)
                            .matches()) {

                        passengerEmailInput.setError(
                                "Invalid email address"
                        );

                        return;
                    }

                    if (passengerEmails.contains(email)) {
                        Toast.makeText(
                                requireContext(),
                                "Passenger already added.",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    passengerEmails.add(email);

                    addPassengerLabel(email);

                    passengerEmailInput.setText("");
                });
    }

    private void addPassengerLabel(String email) {

        TextView passenger =
                new TextView(requireContext());

        passenger.setText("• " + email);
        passenger.setTextSize(14);

        passenger.setPadding(
                0,
                6,
                0,
                6
        );

        passenger.setOnClickListener(v -> {

            passengerEmails.remove(email);
            passengersContainer.removeView(passenger);
        });

        passengersContainer.addView(passenger);
    }

    private void setupScheduling() {

        scheduleGroup.setOnCheckedChangeListener(
                (group, checkedId) -> {

                    scheduleTimePicker.setVisibility(
                            checkedId == R.id.scheduleRadio
                                    ? View.VISIBLE
                                    : View.GONE
                    );
                }
        );
    }

    private void calculateRoute() {

        if (selectedStart == null ||
                selectedDestination == null) {

            return;
        }

        List<Place> points =
                getOrderedPlaces();

        viewModel.estimateRoute(points);
    }

    private List<Place> getOrderedPlaces() {

        List<Place> points =
                new ArrayList<>();

        points.add(selectedStart);

        for (OrderStop stop : stops) {

            if (stop.getPlace() != null) {
                points.add(stop.getPlace());
            }
        }

        points.add(selectedDestination);

        return points;
    }

    private void drawRoute(RouteEstimate estimate) {

        clearMap();

        List<GeoPoint> points =
                new ArrayList<>();

        for (LatLng point : estimate.getGeometry()) {

            points.add(
                    new GeoPoint(
                            point.getLat(),
                            point.getLon()
                    )
            );
        }

        if (points.isEmpty()) {
            return;
        }

        routeLine = new Polyline(map);

        routeLine.setPoints(points);

        routeLine.getOutlinePaint().setColor(
                ContextCompat.getColor(
                        requireContext(),
                        R.color.route_line
                )
        );

        routeLine.getOutlinePaint()
                .setStrokeWidth(12f);

        map.getOverlays()
                .add(routeLine);

        List<Place> locations =
                getOrderedPlaces();

        for (int i = 0;
             i < locations.size();
             i++) {

            Place place =
                    locations.get(i);

            Marker marker = new Marker(map);

            marker.setPosition(
                    new GeoPoint(
                            place.getLat(),
                            place.getLon()
                    )
            );

            marker.setAnchor(
                    Marker.ANCHOR_CENTER,
                    Marker.ANCHOR_BOTTOM
            );

            marker.setTitle(
                    i == 0
                            ? "Start"
                            : i == locations.size() - 1
                            ? "Destination"
                            : "Stop " + i
            );

            /*
             * Use our custom marker icons.
             */
            int markerResource;

            if (i == 0) {
                markerResource = R.drawable.ic_marker_start;
            } else if (i == locations.size() - 1) {
                markerResource = R.drawable.ic_marker_end;
            } else {
                markerResource = R.drawable.ic_marker_stop;
            }

            Drawable markerDrawable =
                    ResourcesCompat.getDrawable(
                            getResources(),
                            markerResource,
                            requireContext().getTheme()
                    );

            if (markerDrawable != null) {
                marker.setIcon(markerDrawable);
            }

            map.getOverlays().add(marker);
            routeMarkers.add(marker);
        }

        map.invalidate();

        BoundingBox bounds =
                BoundingBox.fromGeoPoints(points);

        map.post(() ->
                map.zoomToBoundingBox(
                        bounds,
                        false,
                        ROUTE_PADDING
                )
        );
    }

    private void clearMap() {
        if (map == null) {
            return;
        }

        // Remove the current route line.
        if (routeLine != null) {
            map.getOverlays().remove(routeLine);
            routeLine = null;
        }

        // Remove every marker belonging to the previous route.
        for (Marker marker : routeMarkers) {
            map.getOverlays().remove(marker);
        }

        routeMarkers.clear();

        map.invalidate();
    }

    private void showEstimate(RouteEstimate estimate) {

        double km =
                estimate.getDistanceMeters() / 1000.0;

        long minutes =
                Math.round(
                        estimate.getDurationSeconds() / 60.0
                );

        VehicleType type =
                (VehicleType)
                        vehicleTypeSpinner
                                .getSelectedItem();

        double basePrice;

        switch (type) {
            case VAN:
                basePrice = 500;
                break;

            case LUXURY:
                basePrice = 800;
                break;

            case STANDARD:
            default:
                basePrice = 300;
                break;
        }

        double price =
                basePrice +
                        km * PRICE_PER_KM;

        distanceText.setText(
                String.format(
                        Locale.US,
                        "Distance: %.1f km",
                        km
                )
        );

        durationText.setText(
                "Time: " + minutes + " min"
        );

        priceText.setText(
                String.format(
                        Locale.US,
                        "Price: %.0f RSD",
                        price
                )
        );
    }

    private void orderRide() {

        if (selectedStart == null) {
            Toast.makeText(
                    requireContext(),
                    "Please select a starting point.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (selectedDestination == null) {
            Toast.makeText(
                    requireContext(),
                    "Please select a destination.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        for (OrderStop stop : stops) {

            if (stop.getPlace() == null) {

                Toast.makeText(
                        requireContext(),
                        "Please select all intermediate stops.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }
        }

        RouteEstimate estimate =
                viewModel.getCurrentRoute();

        if (estimate == null) {

            Toast.makeText(
                    requireContext(),
                    "Please calculate the route first.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        List<LocationRequest> locations =
                new ArrayList<>();

        for (Place place : getOrderedPlaces()) {

            locations.add(
                    new LocationRequest(
                            place.getLat(),
                            place.getLon()
                    )
            );
        }

        double km =
                estimate.getDistanceMeters() / 1000.0;

        VehicleType vehicleType =
                (VehicleType)
                        vehicleTypeSpinner
                                .getSelectedItem();

        double basePrice;

        switch (vehicleType) {
            case VAN:
                basePrice = 500;
                break;

            case LUXURY:
                basePrice = 800;
                break;

            case STANDARD:
            default:
                basePrice = 300;
                break;
        }

        double price =
                basePrice +
                        km * PRICE_PER_KM;

        String scheduledTime =
                getScheduledTime();

        if (scheduleRadio.isChecked() && scheduledTime == null) {
            return;
        }

        setLoading(true);

        viewModel.createRideRequest(
                locations,
                vehicleType,
                babiesCheckBox.isChecked(),
                petsCheckBox.isChecked(),
                scheduledTime,
                new ArrayList<>(passengerEmails),
                km,
                price
        );
    }

    private String getScheduledTime() {

        if (!scheduleRadio.isChecked()) {
            return null;
        }

        int hour =
                scheduleTimePicker.getHour();

        int minute =
                scheduleTimePicker.getMinute();

        LocalDateTime now =
                LocalDateTime.now();

        LocalDateTime scheduled =
                now.withHour(hour)
                        .withMinute(minute)
                        .withSecond(0)
                        .withNano(0);

        /*
         * If the selected time has already passed today,
         * interpret it as tomorrow.
         */
        if (!scheduled.isAfter(now)) {
            scheduled = scheduled.plusDays(1);
        }

        /*
         * Backend itself enforces the five-hour rule.
         * We also check here so the user gets immediate feedback.
         */
        if (scheduled.isAfter(
                now.plusHours(5)
        )) {

            Toast.makeText(
                    requireContext(),
                    "A ride can only be scheduled up to 5 hours ahead.",
                    Toast.LENGTH_LONG
            ).show();

            return null;
        }

        return scheduled.format(
                DateTimeFormatter.ISO_LOCAL_DATE_TIME
        );
    }

    private void setLoading(boolean loading) {

        orderRideButton.setEnabled(!loading);

        progress.setVisibility(
                loading
                        ? View.VISIBLE
                        : View.GONE
        );
    }

    private void loadFavoriteRoute() {

        Bundle args = getArguments();

        if (args == null) {
            return;
        }

        RouteResponse route =
                args.getParcelable(
                        RideOrderActivity.EXTRA_FAVORITE_ROUTE
                );

        if (route == null) {
            return;
        }

        /*
         * A favorite route only contains coordinates.
         * Convert those coordinates into the Place objects
         * expected by the existing ride-ordering UI.
         */
        if (route.getStart() != null) {

            selectedStart =
                    placeFromLocation(
                            route.getStart(),
                            "Favorite start"
                    );

            setInputWithoutSearching(
                    startInput,
                    selectedStart.getLabel()
            );
        }

        /*
         * Recreate all intermediate stops.
         */
        if (route.getMidPoints() != null) {

            for (LocationDto location :
                    route.getMidPoints()) {

                if (location == null ||
                        !location.isValid()) {
                    continue;
                }

                addFavoriteStop(
                        placeFromLocation(
                                location,
                                "Intermediate stop"
                        )
                );
            }
        }

        /*
         * Destination.
         */
        if (route.getDestination() != null) {

            selectedDestination =
                    placeFromLocation(
                            route.getDestination(),
                            "Favorite destination"
                    );

            setInputWithoutSearching(
                    destinationInput,
                    selectedDestination.getLabel()
            );
        }

        /*
         * Now that all points have been restored,
         * calculate the route automatically.
         */
        if (selectedStart != null &&
                selectedDestination != null) {

            calculateRoute();
        }
    }

    private Place placeFromLocation(
            LocationDto location,
            String fallbackLabel
    ) {

        String label =
                String.format(
                        Locale.US,
                        "%.6f, %.6f",
                        location.getLatitude(),
                        location.getLongitude()
                );

        return new Place(
                label,
                location.getLatitude(),
                location.getLongitude()
        );
    }

    private void setInputWithoutSearching(
            AutoCompleteTextView input,
            String text
    ) {

        suppressInputWatcher = true;

        input.setText(
                text,
                false
        );

        input.dismissDropDown();

        suppressInputWatcher = false;
    }

    private void addFavoriteStop(Place place) {

        /*
         * Create the exact same UI as a normal intermediate stop.
         */
        AutoCompleteTextView input =
                new AutoCompleteTextView(requireContext());

        input.setHint("Intermediate stop");
        input.setSingleLine(true);
        input.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT |
                        android.text.InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS
        );

        input.setLayoutParams(
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1
                )
        );

        PlaceSuggestionAdapter adapter =
                new PlaceSuggestionAdapter(requireContext());

        input.setAdapter(adapter);

        OrderStop stop =
                new OrderStop(input);

        /*
         * This is the important difference from addStop():
         * the stop already has a selected Place.
         */
        stop.setPlace(place);

        LinearLayout row =
                new LinearLayout(requireContext());

        row.setOrientation(
                LinearLayout.HORIZONTAL
        );

        row.setGravity(
                android.view.Gravity.CENTER_VERTICAL
        );

        row.setPadding(0, 4, 0, 4);

        Button removeButton =
                new Button(requireContext());

        removeButton.setText("×");

        removeButton.setOnClickListener(v -> {

            stops.remove(stop);

            stopsContainer.removeView(row);

            calculateRoute();
        });

        row.addView(input);
        row.addView(removeButton);

        stopsContainer.addView(row);

        stops.add(stop);

        /*
         * Display the favorite route's coordinate.
         *
         * suppressInputWatcher prevents the existing TextWatcher
         * from treating this as newly typed text and clearing
         * stop.setPlace().
         */
        setInputWithoutSearching(
                input,
                place.getLabel()
        );

        /*
         * Still wire up normal autocomplete functionality so that
         * the user can edit this favorite stop if desired.
         */
        wireStopSearch(stop, adapter);
    }
}