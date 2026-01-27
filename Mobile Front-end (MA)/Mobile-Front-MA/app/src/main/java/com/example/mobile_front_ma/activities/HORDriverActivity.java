package com.example.mobile_front_ma.activities;

import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile_front_ma.R;
import com.example.mobile_front_ma.adapters.RidesAdapter;
import com.example.mobile_front_ma.viewmodels.HORDriverViewModel;

public class HORDriverActivity extends AppCompatActivity {

    private RidesAdapter ridesAdapter;
    private HORDriverViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_hor_driver);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        configureMainButton();
        setupRecyclerView();
        addRidesToList();
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        ridesAdapter = new RidesAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(ridesAdapter);
    }

    private void addRidesToList() {
        viewModel = new ViewModelProvider(this).get(HORDriverViewModel.class);
        viewModel.getRidesLiveData().observe(this, rides -> {
            if (rides != null) {
                ridesAdapter.submitList(rides);
            }
        });
    }

    private void configureMainButton() {
        Button horMainButton = findViewById(R.id.horDriverHomeButton);
        horMainButton.setOnClickListener(v -> {
            finish();
        });
    }
}