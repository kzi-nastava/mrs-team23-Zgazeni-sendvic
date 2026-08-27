package com.example.mobile_front_ma.ui.map;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.mobile_front_ma.BuildConfig;
import com.example.mobile_front_ma.R;
import com.example.mobile_front_ma.data.DummyRide;
import com.example.mobile_front_ma.data.GeoRepository;
import com.example.mobile_front_ma.data.RideRepository;
import com.example.mobile_front_ma.data.SessionManager;
import com.example.mobile_front_ma.data.network.ApiCallback;
import com.example.mobile_front_ma.data.realtime.RideTrackingSocketClient;
import com.example.mobile_front_ma.models.dto.PanicResponse;
import com.example.mobile_front_ma.models.dto.RideDriverRatingDTO;
import com.example.mobile_front_ma.models.dto.RideEndDto;
import com.example.mobile_front_ma.models.dto.RideNoteDTO;
import com.example.mobile_front_ma.models.dto.RideTrackingUpdateDto;
import com.google.android.material.card.MaterialCardView;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;


public class RideTrackingFragment extends Fragment implements RideTrackingSocketClient.Listener {

    private static final String TAG = "RideTrackingFragment";

    private MapView map;
    private RideRepository rideRepository;
    private RideTrackingSocketClient socketClient;

    private Polyline routeLine;
    private Marker vehicleMarker;
    private Marker destinationMarker;

    private MaterialCardView etaCard;
    private TextView tvEta;

    private RideTrackingUpdateDto lastUpdate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ride_tracking, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rideRepository = new RideRepository(requireContext());
        SessionManager sessionManager = new SessionManager(requireContext());
        boolean isDriver = "DRIVER".equalsIgnoreCase(sessionManager.getRole());

        map = view.findViewById(R.id.trackingMap);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.getController().setZoom(15.0);
        map.getController().setCenter(
                new GeoPoint(GeoRepository.NOVI_SAD_LAT, GeoRepository.NOVI_SAD_LON));

        etaCard = view.findViewById(R.id.etaCard);
        tvEta = view.findViewById(R.id.tvEta);

        Button btnPanic = view.findViewById(R.id.btnPanic);
        Button btnNote = view.findViewById(R.id.btnNote);
        Button btnRate = view.findViewById(R.id.btnRate);
        Button btnEndRide = view.findViewById(R.id.btnEndRide);

        btnPanic.setOnClickListener(v -> confirmPanic(btnPanic));

        if (isDriver) {
            btnNote.setVisibility(View.GONE);
            btnRate.setVisibility(View.GONE);
            btnEndRide.setVisibility(View.VISIBLE);
            btnEndRide.setOnClickListener(v -> endRide());
        } else {
            btnNote.setVisibility(View.VISIBLE);
            btnRate.setVisibility(View.VISIBLE);
            btnEndRide.setVisibility(View.GONE);
            btnNote.setOnClickListener(v -> showNoteDialog());
            btnRate.setOnClickListener(v -> showRatingDialog());
        }

        initWebSocket();
    }

    private void showNoteDialog() {
        if (lastUpdate == null || lastUpdate.getRideId() == null) {
            Toast.makeText(getContext(), "No active ride to add a note to", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_note, null);
        EditText etNote = dialogView.findViewById(R.id.etNote);

        new AlertDialog.Builder(requireContext())
                .setTitle("Add Note")
                .setView(dialogView)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Note", (dialog, which) -> {
                    String noteText = etNote.getText().toString();
                    if (!noteText.trim().isEmpty()) {
                        submitNote(noteText);
                    } else {
                        Toast.makeText(getContext(), "Note cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void submitNote(String noteText) {
        SessionManager session = new SessionManager(requireContext());
        long userId = session.getUserId();

        RideNoteDTO noteDto = new RideNoteDTO(
                lastUpdate.getRideId(),
                noteText
        );

        rideRepository.addNote(userId, noteDto, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Toast.makeText(getContext(), "Note sent successfully!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(getContext(), "Failed to send note: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showRatingDialog() {
        if (lastUpdate == null || lastUpdate.getRideId() == null) {
            Toast.makeText(getContext(), "No active ride to rate", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_rate_ride, null);
        SeekBar seekVehicle = dialogView.findViewById(R.id.seekVehicleRating);
        TextView tvVehicleValue = dialogView.findViewById(R.id.tvVehicleValue);
        SeekBar seekDriver = dialogView.findViewById(R.id.seekDriverRating);
        TextView tvDriverValue = dialogView.findViewById(R.id.tvDriverValue);
        EditText etComment = dialogView.findViewById(R.id.etComment);

        seekVehicle.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvVehicleValue.setText("Rating: " + (progress + 1));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        seekDriver.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvDriverValue.setText("Rating: " + (progress + 1));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        new AlertDialog.Builder(requireContext())
                .setTitle("Rate Ride")
                .setView(dialogView)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Rate", (dialog, which) -> {
                    int vRating = seekVehicle.getProgress() + 1;
                    int dRating = seekDriver.getProgress() + 1;
                    String comment = etComment.getText().toString();
                    submitRating(vRating, dRating, comment);
                })
                .show();
    }

    private void submitRating(int vehicleRating, int driverRating, String comment) {
        SessionManager session = new SessionManager(requireContext());
        long userId = session.getUserId();
        
        RideDriverRatingDTO ratingDto = new RideDriverRatingDTO(
                userId,
                lastUpdate.getRideId(),
                driverRating,
                vehicleRating,
                comment
        );

        rideRepository.rateRide(userId, ratingDto, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Toast.makeText(getContext(), "Thank you for your rating!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(getContext(), "Rating failed: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void endRide() {
        if (lastUpdate == null) {
            Toast.makeText(getContext(), "Ride data not yet received", Toast.LENGTH_SHORT).show();
            return;
        }

        RideEndDto endDto = new RideEndDto(
                lastUpdate.getRideId(),
                lastUpdate.price != null ? lastUpdate.price : 0.0,
                true,
                true
        );

        rideRepository.endRide(endDto, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Toast.makeText(getContext(), "Ride ended successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(getContext(), "Error ending ride: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initWebSocket() {
        SessionManager session = new SessionManager(requireContext());
        long userId = session.getUserId();
        if (userId == -1) {
            Log.e(TAG, "User ID not found in session, cannot track ride.");
            return;
        }

        socketClient = new RideTrackingSocketClient(requireContext(), BuildConfig.BACKEND_URL, userId, this);
    }

    @Override
    public void onRideUpdate(RideTrackingUpdateDto update) {
        if (getActivity() == null || map == null) return;
        this.lastUpdate = update;
        Log.d(TAG, "Received ride update for ride: " + update.getRideId() + " status: " + update.getStatus());
        drawRide(update);
        updateEta(update);
    }

    private void updateEta(RideTrackingUpdateDto update) {
        if (update.estimatedEndTime != null) {
            String etaStr = update.estimatedEndTime.toString();
            // Expecting ISO string "yyyy-MM-ddTHH:mm:ss"
            if (etaStr.contains("T")) {
                String timePart = etaStr.split("T")[1];
                if (timePart.length() >= 5) {
                    tvEta.setText("Arrival: " + timePart.substring(0, 5));
                    etaCard.setVisibility(View.VISIBLE);
                    return;
                }
            }
            tvEta.setText("Arrival: " + etaStr);
            etaCard.setVisibility(View.VISIBLE);
        } else {
            etaCard.setVisibility(View.GONE);
        }
    }


    private void drawRide(RideTrackingUpdateDto update) {
        if (routeLine != null) map.getOverlays().remove(routeLine);
        if (vehicleMarker != null) map.getOverlays().remove(vehicleMarker);
        if (destinationMarker != null) map.getOverlays().remove(destinationMarker);

        List<RideTrackingUpdateDto.LocationDTO> route = update.getRoute();
        if (route != null && !route.isEmpty()) {
            List<GeoPoint> points = new ArrayList<>();
            for (RideTrackingUpdateDto.LocationDTO loc : route) {
                if (loc.getLatitude() != null && loc.getLongitude() != null) {
                    points.add(new GeoPoint(loc.getLatitude(), loc.getLongitude()));
                }
            }

            if (!points.isEmpty()) {
                routeLine = new Polyline(map);
                routeLine.setPoints(points);
                routeLine.getOutlinePaint().setColor(ContextCompat.getColor(requireContext(), R.color.route_line));
                routeLine.getOutlinePaint().setStrokeWidth(12f);
                map.getOverlays().add(routeLine);

                vehicleMarker = createMarker(points.get(0), R.drawable.ic_marker_end, "Vehicle (Pickup)");
                destinationMarker = createMarker(points.get(points.size() - 1), R.drawable.ic_marker_end, "Destination");
                
                map.getOverlays().add(vehicleMarker);
                map.getOverlays().add(destinationMarker);

                map.getController().animateTo(points.get(0));
            }
        }

        map.invalidate();
    }

    private Marker createMarker(GeoPoint position, int iconRes, String title) {
        Marker marker = new Marker(map);
        marker.setPosition(position);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setIcon(ContextCompat.getDrawable(requireContext(), iconRes));
        marker.setTitle(title);
        return marker;
    }

    private void confirmPanic(Button btnPanic) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.hor_panic_confirm_title)
                .setMessage(R.string.hor_panic_confirm_message)
                .setNegativeButton(R.string.hor_panic_confirm_no, null)
                .setPositiveButton(R.string.hor_panic_confirm_yes, (dialog, which) -> raisePanic(btnPanic))
                .show();
    }

    private void raisePanic(Button btnPanic) {
        btnPanic.setEnabled(false);
        long rideId = lastUpdate != null ? lastUpdate.getRideId() : DummyRide.RIDE_ID;
        rideRepository.panicRide(rideId, new ApiCallback<PanicResponse>() {
            @Override
            public void onSuccess(PanicResponse data) {
                btnPanic.setText(R.string.hor_panic_sent);
                Toast.makeText(getContext(), R.string.hor_panic_success, Toast.LENGTH_LONG).show();
                btnPanic.postDelayed(() -> {
                    if (isAdded()) {
                        btnPanic.setEnabled(true);
                        btnPanic.setText(R.string.hor_panic_button);
                    }
                }, 3000);
            }

            @Override
            public void onError(String message) {
                btnPanic.setEnabled(true);
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map != null) map.onResume();
        if (socketClient != null) socketClient.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (map != null) map.onPause();
        if (socketClient != null) socketClient.disconnect();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (socketClient != null) {
            socketClient.disconnect();
            socketClient = null;
        }
        if (map != null) {
            map.onDetach();
            map = null;
        }
    }

    @Override
    public void onConnected() {
        if (getContext() != null) {
            Toast.makeText(getContext(), "Tracking connected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), "Tracking error: " + message, Toast.LENGTH_SHORT).show();
        }
    }
}
