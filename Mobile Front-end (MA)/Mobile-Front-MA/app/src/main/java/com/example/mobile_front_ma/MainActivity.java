package com.example.mobile_front_ma;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mobile_front_ma.activities.HORDriverActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        configureHORDriverButton();
    }

    private void configureHORDriverButton() {
        // Implementation for configuring the HOR Driver button
        Button horDriverButton = findViewById(R.id.horDriverButton);
        horDriverButton.setOnClickListener(v -> {
            // Handle button click
            startActivity(new Intent(MainActivity.this, HORDriverActivity.class));
        });
    }
}