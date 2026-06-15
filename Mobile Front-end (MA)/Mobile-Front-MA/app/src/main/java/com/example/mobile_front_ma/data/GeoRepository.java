package com.example.mobile_front_ma.data;

import androidx.annotation.NonNull;

import com.example.mobile_front_ma.data.network.ApiCallback;
import com.example.mobile_front_ma.data.network.GeoApiClient;
import com.example.mobile_front_ma.data.network.NominatimApi;
import com.example.mobile_front_ma.data.network.OsrmApi;
import com.example.mobile_front_ma.models.LatLng;
import com.example.mobile_front_ma.models.Place;
import com.example.mobile_front_ma.models.RouteEstimate;
import com.example.mobile_front_ma.models.dto.NominatimPlace;
import com.example.mobile_front_ma.models.dto.OsrmRouteResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Ride-estimation data layer: turns addresses into Novi Sad places (geocoding) and a
 * start/destination pair into a driving route with an estimated time (routing). All calls
 * are async (enqueue) and report back through {@link ApiCallback}, keeping OpenStreetMap
 * types out of the UI layer.
 */
public class GeoRepository {

    /** The service runs in Novi Sad, so the whole app is centred there by default. */
    public static final double NOVI_SAD_LAT = 45.2671;
    public static final double NOVI_SAD_LON = 19.8335;

    /**
     * Bounding box around Novi Sad in Nominatim viewbox order: west,north,east,south.
     * Combined with bounded=1 it restricts every suggestion to the city.
     */
    private static final String NOVI_SAD_VIEWBOX = "19.74,45.34,19.95,45.18";

    private final NominatimApi nominatimApi = GeoApiClient.nominatim();
    private final OsrmApi osrmApi = GeoApiClient.osrm();

    /** Autocomplete: addresses in Novi Sad matching the typed text. */
    public void searchPlaces(String query, ApiCallback<List<Place>> callback) {
        nominatimApi.search(query, "jsonv2", 1, 6, "rs",
                        NOVI_SAD_VIEWBOX, 1, "sr,en")
                .enqueue(new Callback<List<NominatimPlace>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<NominatimPlace>> call,
                                           @NonNull Response<List<NominatimPlace>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(toPlaces(response.body()));
                        } else {
                            callback.onError("Search failed (" + response.code() + ").");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<NominatimPlace>> call,
                                          @NonNull Throwable t) {
                        callback.onError(networkErrorMessage());
                    }
                });
    }

    /** Estimation: driving route + time/distance between the two chosen points. */
    public void estimateRoute(Place start, Place destination,
                              ApiCallback<RouteEstimate> callback) {
        // OSRM expects lon,lat;lon,lat
        String coordinates = String.format(Locale.US, "%f,%f;%f,%f",
                start.getLon(), start.getLat(),
                destination.getLon(), destination.getLat());

        osrmApi.route(coordinates, "full", "geojson")
                .enqueue(new Callback<OsrmRouteResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<OsrmRouteResponse> call,
                                           @NonNull Response<OsrmRouteResponse> response) {
                        OsrmRouteResponse body = response.body();
                        if (response.isSuccessful() && body != null
                                && body.routes != null && !body.routes.isEmpty()) {
                            callback.onSuccess(toEstimate(body.routes.get(0)));
                        } else {
                            callback.onError("Couldn't calculate a route. Try different points.");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<OsrmRouteResponse> call,
                                          @NonNull Throwable t) {
                        callback.onError(networkErrorMessage());
                    }
                });
    }

    private List<Place> toPlaces(List<NominatimPlace> raw) {
        List<Place> places = new ArrayList<>();
        for (NominatimPlace p : raw) {
            if (p.lat == null || p.lon == null) {
                continue;
            }
            try {
                double lat = Double.parseDouble(p.lat);
                double lon = Double.parseDouble(p.lon);
                places.add(new Place(shortLabel(p.displayName), lat, lon));
            } catch (NumberFormatException ignored) {
                // Skip malformed entries rather than failing the whole search.
            }
        }
        return places;
    }

    private RouteEstimate toEstimate(OsrmRouteResponse.Route route) {
        List<LatLng> geometry = new ArrayList<>();
        if (route.geometry != null && route.geometry.coordinates != null) {
            for (List<Double> point : route.geometry.coordinates) {
                // GeoJSON order is [lon, lat]
                if (point.size() >= 2) {
                    geometry.add(new LatLng(point.get(1), point.get(0)));
                }
            }
        }
        return new RouteEstimate(route.distance, route.duration, geometry);
    }

    /** Nominatim display names are long; keep the first few parts for a readable label. */
    private String shortLabel(String displayName) {
        if (displayName == null) {
            return "";
        }
        String[] parts = displayName.split(",");
        StringBuilder sb = new StringBuilder();
        int count = Math.min(3, parts.length);
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(parts[i].trim());
        }
        return sb.toString();
    }

    private String networkErrorMessage() {
        return "Cannot reach the map service. Check your internet connection.";
    }
}
