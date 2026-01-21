package com.example.mobile_front_ma.ui.navbar;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mobile_front_ma.R;

public class NavBarFragment extends Fragment {

    public interface NavBarListener {
        void onHomeClicked();
        void onProfileClicked();
    }

    private NavBarListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (NavBarListener) context;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_nav_bar, container, false);

        Button homeButton = view.findViewById(R.id.btnHome);
        Button profileButton = view.findViewById(R.id.btnProfile);

        homeButton.setOnClickListener(v -> listener.onHomeClicked());
        profileButton.setOnClickListener(v -> listener.onProfileClicked());

        return view;
    }
}
