package com.example.mobile_front_ma.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile_front_ma.R;
import com.example.mobile_front_ma.adapters.FavoriteRouteAdapter;
import com.example.mobile_front_ma.data.RouteRepository;
import com.example.mobile_front_ma.data.network.ApiCallback;
import com.example.mobile_front_ma.models.dto.RouteResponse;

import java.util.ArrayList;
import java.util.List;

public class FavoriteRoutesActivity extends AppCompatActivity
        implements FavoriteRouteAdapter.Listener {

    private RouteRepository repository;
    private FavoriteRouteAdapter adapter;

    private ProgressBar progressBar;
    private TextView emptyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(
                R.layout.activity_favorite_routes
        );

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {

                    Insets bars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    v.setPadding(
                            bars.left,
                            bars.top,
                            bars.right,
                            bars.bottom
                    );

                    return insets;
                }
        );

        repository = new RouteRepository(this);

        progressBar = findViewById(
                R.id.progressBar
        );

        emptyText = findViewById(
                R.id.emptyText
        );

        findViewById(R.id.backButton)
                .setOnClickListener(v -> finish());

        setupRecyclerView();
        loadFavorites();
    }

    private void setupRecyclerView() {

        RecyclerView recyclerView =
                findViewById(R.id.recyclerView);

        adapter = new FavoriteRouteAdapter(this);

        recyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        recyclerView.setAdapter(adapter);
    }

    private void loadFavorites() {

        progressBar.setVisibility(View.VISIBLE);
        emptyText.setVisibility(View.GONE);

        repository.getFavorites(
                new ApiCallback<List<RouteResponse>>() {

                    @Override
                    public void onSuccess(
                            List<RouteResponse> data
                    ) {
                        runOnUiThread(() -> {

                            progressBar.setVisibility(
                                    View.GONE
                            );

                            if (data == null
                                    || data.isEmpty()) {

                                adapter.submitList(
                                        new ArrayList<>()
                                );

                                emptyText.setVisibility(
                                        View.VISIBLE
                                );

                            } else {

                                adapter.submitList(data);

                                emptyText.setVisibility(
                                        View.GONE
                                );
                            }
                        });
                    }

                    @Override
                    public void onError(String message) {

                        runOnUiThread(() -> {

                            progressBar.setVisibility(
                                    View.GONE
                            );

                            Toast.makeText(
                                    FavoriteRoutesActivity.this,
                                    message,
                                    Toast.LENGTH_LONG
                            ).show();
                        });
                    }
                }
        );
    }

    @Override
    public void onDelete(RouteResponse route) {

        if (route.id == null) {
            return;
        }

        repository.deleteFavorite(
                route.id,
                new ApiCallback<Void>() {

                    @Override
                    public void onSuccess(Void ignored) {
                        runOnUiThread(() -> {
                            Toast.makeText(
                                    FavoriteRoutesActivity.this,
                                    "Favorite route deleted.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            loadFavorites();
                        });
                    }

                    @Override
                    public void onError(String message) {
                        runOnUiThread(() ->
                                Toast.makeText(
                                        FavoriteRoutesActivity.this,
                                        message,
                                        Toast.LENGTH_LONG
                                ).show()
                        );
                    }
                }
        );
    }

    @Override
    public void onOrder(RouteResponse route) {

        Intent intent = new Intent(
                this,
                RideOrderActivity.class
        );

        /*
         * Pass the favorite route to the existing ride-ordering
         * screen. RideOrderFragment will consume these extras and
         * populate the existing start/midpoint/destination fields.
         */
        intent.putExtra(
                RideOrderActivity.EXTRA_FAVORITE_ROUTE,
                route
        );

        startActivity(intent);
    }
}