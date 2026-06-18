package com.example.mobile_front_ma.util;

import androidx.annotation.NonNull;

import com.example.mobile_front_ma.data.network.GeoApiClient;
import com.example.mobile_front_ma.models.dto.LocationDto;
import com.example.mobile_front_ma.models.dto.NominatimPlace;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Turns a ride's start/end coordinate into a human-readable place name (e.g.
 * "Bulevar oslobođenja, Novi Sad") for the ride-history list, instead of showing raw
 * "lat, lon". Uses OpenStreetMap Nominatim reverse geocoding.
 *
 * Results are cached by rounded coordinate so each unique point is fetched only once, and an
 * in-flight guard avoids firing duplicate requests (Nominatim asks callers to be gentle).
 * Lookups are asynchronous; {@link #resolve} calls back only when a name is available, so the
 * caller can show the coordinates as a placeholder until then.
 */
public final class LocationLabeler {

    public interface Callback {
        void onLabel(String label);
    }

    private static final Map<String, String> CACHE = new ConcurrentHashMap<>();
    private static final Set<String> PENDING = Collections.synchronizedSet(new HashSet<>());

    private LocationLabeler() {
    }

    /**
     * Resolve a readable label for {@code point}. If it's already cached, the callback fires
     * immediately (synchronously). Otherwise a network lookup runs and the callback fires once
     * a name is known; on failure it does not fire, leaving the caller's placeholder in place.
     */
    public static void resolve(LocationDto point, Callback callback) {
        if (point == null || !point.isValid()) {
            return;
        }
        final String key = key(point);

        String cached = CACHE.get(key);
        if (cached != null) {
            callback.onLabel(cached);
            return;
        }

        // Only one network request per coordinate; later callers pick it up from the cache.
        if (!PENDING.add(key)) {
            return;
        }

        GeoApiClient.nominatim()
                .reverse(point.getLatitude(), point.getLongitude(), "jsonv2", 16, 1)
                .enqueue(new retrofit2.Callback<NominatimPlace>() {
                    @Override
                    public void onResponse(@NonNull Call<NominatimPlace> call,
                                           @NonNull Response<NominatimPlace> response) {
                        PENDING.remove(key);
                        String label = shorten(response.isSuccessful() ? response.body() : null);
                        if (label != null) {
                            CACHE.put(key, label);
                            callback.onLabel(label);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<NominatimPlace> call, @NonNull Throwable t) {
                        // Don't cache failures, so it can be retried on a later bind.
                        PENDING.remove(key);
                    }
                });
    }

    /** Cache key: coordinates rounded to ~11 m so near-identical points share a lookup. */
    private static String key(LocationDto p) {
        return String.format(Locale.US, "%.4f,%.4f", p.getLatitude(), p.getLongitude());
    }

    /**
     * Shorten Nominatim's long {@code display_name} to the first two meaningful parts, e.g.
     * "Bulevar oslobođenja 1, Novi Sad". Falls back to {@code name}, or null if neither helps.
     */
    private static String shorten(NominatimPlace place) {
        if (place == null) {
            return null;
        }
        String displayName = place.displayName;
        if (displayName == null || displayName.trim().isEmpty()) {
            return (place.name != null && !place.name.trim().isEmpty()) ? place.name.trim() : null;
        }
        StringBuilder sb = new StringBuilder();
        int taken = 0;
        for (String part : displayName.split(",")) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(trimmed);
            if (++taken == 2) {
                break;
            }
        }
        return sb.length() > 0 ? sb.toString() : null;
    }
}
