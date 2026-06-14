package com.example.mobile_front_ma.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mobile_front_ma.R;
import com.example.mobile_front_ma.activities.LoginActivity;
import com.example.mobile_front_ma.data.SessionManager;

public class ProfileCardFragment extends Fragment {

    public ProfileCardFragment() {}

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

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

        // Log out: clear the saved session and return to the login screen.
        view.findViewById(R.id.logoutButton).setOnClickListener(v -> logOut());

        return view;
    }

    private void logOut() {
        new SessionManager(requireContext()).clear();
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