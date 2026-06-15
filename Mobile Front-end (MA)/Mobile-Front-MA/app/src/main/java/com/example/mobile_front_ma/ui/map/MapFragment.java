package com.example.mobile_front_ma.ui.map;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.mobile_front_ma.R;
import com.example.mobile_front_ma.data.GeoRepository;
import com.example.mobile_front_ma.models.LatLng;
import com.example.mobile_front_ma.models.Place;
import com.example.mobile_front_ma.models.RouteEstimate;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Home screen map (centred on Novi Sad, where the service runs). Hosts the "Estimate ride"
 * button that opens the start/destination form and, once a route comes back, draws it with
 * start/end markers and shows the estimated time/distance (spec 2.1.2).
 */
public class MapFragment extends Fragment
        implements RideEstimateBottomSheetFragment.Listener {

    private static final double DEFAULT_ZOOM = 13.5;
    private static final int ROUTE_PADDING_PX = 100;
    // Per-kilometre component of the fare formula from spec 2.4.1 (cena + km * 120).
    private static final double PRICE_PER_KM = 120.0;

    private MapView map;
    private ExtendedFloatingActionButton estimateFab;
    private MaterialCardView resultCard;
    private TextView resultEndpoints;
    private TextView resultTime;
    private TextView resultDistance;
    private TextView resultPrice;

    private Polyline routeLine;
    private Marker startMarker;
    private Marker endMarker;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // osmdroid needs an identifying user agent set before any tile/map request.
        Context ctx = requireContext().getApplicationContext();
        Configuration.getInstance().load(ctx,
                ctx.getSharedPreferences("osmdroid", Context.MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue(ctx.getPackageName());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        map = view.findViewById(R.id.map);
        resultCard = view.findViewById(R.id.resultCard);
        resultEndpoints = view.findViewById(R.id.resultEndpoints);
        resultTime = view.findViewById(R.id.resultTime);
        resultDistance = view.findViewById(R.id.resultDistance);
        resultPrice = view.findViewById(R.id.resultPrice);

        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.getController().setZoom(DEFAULT_ZOOM);
        map.getController().setCenter(
                new GeoPoint(GeoRepository.NOVI_SAD_LAT, GeoRepository.NOVI_SAD_LON));

        estimateFab = view.findViewById(R.id.estimateFab);
        estimateFab.setOnClickListener(v -> openEstimateForm());
        view.findViewById(R.id.resultClose).setOnClickListener(v -> clearRoute());
    }

    private void openEstimateForm() {
        new RideEstimateBottomSheetFragment()
                .show(getChildFragmentManager(), "ride_estimate");
    }

    @Override
    public void onRouteEstimated(Place start, Place destination, RouteEstimate estimate) {
        drawRoute(start, destination, estimate);
        showResultCard(start, destination, estimate);
    }

    private void drawRoute(Place start, Place destination, RouteEstimate estimate) {
        clearOverlays();

        List<GeoPoint> points = new ArrayList<>();
        for (LatLng point : estimate.getGeometry()) {
            points.add(new GeoPoint(point.getLat(), point.getLon()));
        }
        // Fall back to a straight line if the router returned no geometry.
        if (points.isEmpty()) {
            points.add(new GeoPoint(start.getLat(), start.getLon()));
            points.add(new GeoPoint(destination.getLat(), destination.getLon()));
        }

        routeLine = new Polyline(map);
        routeLine.setPoints(points);
        routeLine.getOutlinePaint().setColor(
                ContextCompat.getColor(requireContext(), R.color.route_line));
        routeLine.getOutlinePaint().setStrokeWidth(12f);
        map.getOverlays().add(routeLine);

        startMarker = createMarker(new GeoPoint(start.getLat(), start.getLon()),
                R.drawable.ic_marker_start, start.getLabel());
        endMarker = createMarker(new GeoPoint(destination.getLat(), destination.getLon()),
                R.drawable.ic_marker_end, destination.getLabel());
        map.getOverlays().add(startMarker);
        map.getOverlays().add(endMarker);

        map.invalidate();

        // Zoom to fit once the map has a measured size.
        final BoundingBox bounds = BoundingBox.fromGeoPoints(points);
        map.post(() -> map.zoomToBoundingBox(bounds, false, ROUTE_PADDING_PX));
    }

    private Marker createMarker(GeoPoint position, int iconRes, String title) {
        Marker marker = new Marker(map);
        marker.setPosition(position);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setIcon(ContextCompat.getDrawable(requireContext(), iconRes));
        marker.setTitle(title);
        return marker;
    }

    private void showResultCard(Place start, Place destination, RouteEstimate estimate) {
        resultEndpoints.setText(start.getLabel()
                + " " + getString(R.string.estimate_route_arrow) + " " + destination.getLabel());

        long minutes = Math.round(estimate.getDurationSeconds() / 60.0);
        resultTime.setText(minutes < 1 ? "< 1 min" : minutes + " min");

        double km = estimate.getDistanceMeters() / 1000.0;
        resultDistance.setText(km >= 1
                ? String.format(Locale.US, "%.1f km", km)
                : Math.round(estimate.getDistanceMeters()) + " m");

        long price = Math.round(km * PRICE_PER_KM);
        resultPrice.setText(price + " RSD");

        resultCard.setVisibility(View.VISIBLE);
        // Hide the FAB so it doesn't overlap the summary card; it returns on clear.
        estimateFab.hide();
    }

    private void clearRoute() {
        clearOverlays();
        if (map != null) {
            map.invalidate();
        }
        resultCard.setVisibility(View.GONE);
        estimateFab.show();
    }

    private void clearOverlays() {
        if (map == null) {
            return;
        }
        if (routeLine != null) {
            map.getOverlays().remove(routeLine);
            routeLine = null;
        }
        if (startMarker != null) {
            map.getOverlays().remove(startMarker);
            startMarker = null;
        }
        if (endMarker != null) {
            map.getOverlays().remove(endMarker);
            endMarker = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map != null) {
            map.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (map != null) {
            map.onPause();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (map != null) {
            map.onDetach();
            map = null;
        }
    }
}
