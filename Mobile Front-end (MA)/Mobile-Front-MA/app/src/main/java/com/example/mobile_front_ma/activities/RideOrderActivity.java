package com.example.mobile_front_ma.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobile_front_ma.R;
import com.example.mobile_front_ma.models.dto.RouteResponse;
import com.example.mobile_front_ma.ui.map.RideOrderFragment;

public class RideOrderActivity extends AppCompatActivity {

    public static final String EXTRA_FAVORITE_ROUTE =
            "favorite_route";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ride_order);

        if (savedInstanceState == null) {

            RideOrderFragment fragment =
                    new RideOrderFragment();

            /*
             * Pass the favorite route to the fragment.
             */
            RouteResponse favoriteRoute =
                    getIntent().getParcelableExtra(
                            EXTRA_FAVORITE_ROUTE
                    );

            if (favoriteRoute != null) {

                Bundle args = new Bundle();

                args.putParcelable(
                        EXTRA_FAVORITE_ROUTE,
                        favoriteRoute
                );

                fragment.setArguments(args);
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(
                            R.id.rideOrderContainer,
                            fragment
                    )
                    .commit();
        }
    }
}