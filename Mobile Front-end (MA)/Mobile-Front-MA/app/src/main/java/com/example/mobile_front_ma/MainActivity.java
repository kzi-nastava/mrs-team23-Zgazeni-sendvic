package com.example.mobile_front_ma;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.mobile_front_ma.activities.HORDriverActivity;
import com.example.mobile_front_ma.ui.map.MapFragment;
import com.example.mobile_front_ma.ui.navbar.NavBarFragment;

public class MainActivity extends AppCompatActivity
        implements NavBarFragment.NavBarListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        configureHORDriverButton();


        if (savedInstanceState == null) {
            // Load default fragments
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mainFragmentContainer, new MapFragment())
                    .replace(R.id.navBarContainer, new NavBarFragment())
                    .commit();
        }
    }

    @Override
    public void onHomeClicked() {
        navigateTo(new com.example.mobile_front_ma.ui.map.MapFragment());
    }

    @Override
    public void onProfileClicked() {
        navigateTo(new com.example.mobile_front_ma.ui.profile.ProfileCardFragment());
    }

    private void navigateTo(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mainFragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
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
