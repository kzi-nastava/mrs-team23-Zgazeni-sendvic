package com.example.mobile_front_ma.ui.profile;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

import com.example.mobile_front_ma.R;
import com.example.mobile_front_ma.activities.LoginActivity;
import com.example.mobile_front_ma.data.AuthRepository;
import com.example.mobile_front_ma.data.DriverRepository;
import com.example.mobile_front_ma.data.RideRepository;
import com.example.mobile_front_ma.data.SessionManager;
import com.example.mobile_front_ma.data.network.ApiCallback;
import com.example.mobile_front_ma.models.dto.DriverStatusResponse;
import com.example.mobile_front_ma.models.dto.LocationDto;
import com.example.mobile_front_ma.models.dto.RideStopRequest;
import com.example.mobile_front_ma.models.dto.RideStoppedResponse;

import android.content.res.ColorStateList;

import java.time.LocalDateTime;
import java.util.Collections;

public class ProfileCardFragment extends Fragment {

    // Dummy ride used by the stop-ride button until the in-progress-ride screen exists.
    private static final long DUMMY_RIDE_ID = 1L;

    private SessionManager session;
    private DriverRepository driverRepository;
    private RideRepository rideRepository;
    private AuthRepository authRepository;

    private TextView statusText;
    private View statusDot;
    private Button toggleStatusButton;

    public ProfileCardFragment() {}

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        session = new SessionManager(requireContext());
        driverRepository = new DriverRepository(requireContext());
        rideRepository = new RideRepository(requireContext());
        authRepository = new AuthRepository();

        // Display name
        TextView displayName = view.findViewById(R.id.displayName);
        displayName.setText("John Doe");

        // Driver statistics (can be hidden)
        View driverStats = view.findViewById(R.id.driverStatisticContainer);
        TextView statLabel = view.findViewById(R.id.statisticLabel);
        TextView statValue = view.findViewById(R.id.statisticValue);

        boolean hasDriverStatistic = true;
        if (hasDriverStatistic) {
            statLabel.setText("Rating");
            statValue.setText("4.9");
        } else {
            driverStats.setVisibility(View.GONE);
        }

        // Profile fields
        ((TextView) view.findViewById(R.id.nameValue)).setText("John");
        ((TextView) view.findViewById(R.id.surnameValue)).setText("Doe");
        ((TextView) view.findViewById(R.id.dobValue)).setText("01.01.1990");
        ((TextView) view.findViewById(R.id.emailValue)).setText("john@example.com");
        ((TextView) view.findViewById(R.id.phoneValue)).setText("+381 60 123 456");
        ((TextView) view.findViewById(R.id.passwordValue)).setText("********");

        // Edit button
        ImageButton editButton = view.findViewById(R.id.editProfileButton);
        editButton.setOnClickListener(v -> {
            // Navigate to edit profile screen
        });

        // Action buttons
        view.findViewById(R.id.historyButton).setOnClickListener(v -> {
            // Navigate to ride history
        });

        view.findViewById(R.id.adminButton).setOnClickListener(v -> {
            // Admin action
        });

        setUpDriverControls(view);

        // Log out: ask the backend, then clear the session and return to login.
        view.findViewById(R.id.logoutButton).setOnClickListener(v -> logOut());

        return view;
    }

    /**
     * The active/inactive toggle and the stop-ride button only make sense for drivers,
     * so the whole block stays hidden for users and admins.
     */
    private void setUpDriverControls(View view) {
        View container = view.findViewById(R.id.driverControlsContainer);
        boolean isDriver = "DRIVER".equalsIgnoreCase(session.getRole());
        if (!isDriver) {
            container.setVisibility(View.GONE);
            return;
        }
        container.setVisibility(View.VISIBLE);

        statusText = view.findViewById(R.id.statusText);
        statusDot = view.findViewById(R.id.statusDot);
        toggleStatusButton = view.findViewById(R.id.toggleStatusButton);

        renderStatus(session.isDriverActive());

        toggleStatusButton.setOnClickListener(v -> toggleStatus());
        view.findViewById(R.id.stopRideButton).setOnClickListener(v -> stopRide());
    }

    /** Reflect the current active/inactive state in the dot colour, label and button text. */
    private void renderStatus(boolean active) {
        int green = Color.parseColor("#2E7D32");
        int grey = Color.parseColor("#9E9E9E");
        ViewCompat.setBackgroundTintList(statusDot,
                ColorStateList.valueOf(active ? green : grey));
        statusText.setText(active ? R.string.driver_status_active : R.string.driver_status_inactive);
        toggleStatusButton.setText(active ? R.string.driver_go_inactive : R.string.driver_go_active);
    }

    private void toggleStatus() {
        boolean target = !session.isDriverActive();
        toggleStatusButton.setEnabled(false);
        driverRepository.changeStatus(session.getToken(), session.getEmail(), target,
                new ApiCallback<DriverStatusResponse>() {
                    @Override
                    public void onSuccess(DriverStatusResponse data) {
                        if (!isAdded()) return;
                        // Render the driver's *actual* state from the server, not the
                        // requested one: e.g. asking to go inactive mid-ride leaves the
                        // driver active (deactivation deferred until the ride ends).
                        boolean actualActive = data.isAvailable();
                        session.setDriverActive(actualActive);
                        renderStatus(actualActive);
                        toggleStatusButton.setEnabled(true);
                        String text = data.getMessage() != null ? data.getMessage() : getString(
                                actualActive ? R.string.driver_status_changed_active
                                             : R.string.driver_status_changed_inactive);
                        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String message) {
                        if (!isAdded()) return;
                        toggleStatusButton.setEnabled(true);
                        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Stops a ride in progress (spec 2.6.5). The in-progress-ride screen that would
     * supply the real ride id and driven route isn't built yet, so we send dummy
     * data with ride id 1.
     */
    private void stopRide() {
        RideStopRequest request = new RideStopRequest(
                Collections.singletonList(new LocationDto(45.2671, 19.8335)),
                LocalDateTime.now().toString());

        rideRepository.stopRide(DUMMY_RIDE_ID, request, new ApiCallback<RideStoppedResponse>() {
            @Override
            public void onSuccess(RideStoppedResponse data) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(),
                        getString(R.string.driver_ride_stopped, String.valueOf(data.getNewPrice())),
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void logOut() {
        authRepository.logout(requireContext(), new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                finishLogout();
            }

            @Override
            public void onError(String message) {
                // e.g. an active driver must go inactive before logging out.
                if (!isAdded()) return;
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void finishLogout() {
        if (!isAdded()) return;
        session.clear();
        Toast.makeText(requireContext(), R.string.logout_success, Toast.LENGTH_SHORT).show();

        // Send the user back to login and clear the back stack so they can't return
        // to a logged-in screen with the back button.
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}
