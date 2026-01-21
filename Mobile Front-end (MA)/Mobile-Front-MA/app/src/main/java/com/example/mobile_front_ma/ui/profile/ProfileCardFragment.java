package com.example.mobile_front_ma.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mobile_front_ma.R;

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

        return view;
    }
}