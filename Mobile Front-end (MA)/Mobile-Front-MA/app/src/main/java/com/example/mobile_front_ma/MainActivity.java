package com.example.mobile_front_ma;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.mobile_front_ma.activities.AdminHistoryEntryActivity;
import com.example.mobile_front_ma.activities.HORDriverActivity;
import com.example.mobile_front_ma.activities.PanicNotificationsActivity;
import com.example.mobile_front_ma.activities.RideHistoryActivity;
import com.example.mobile_front_ma.data.SessionManager;
import com.example.mobile_front_ma.data.realtime.PanicForegroundService;
import com.example.mobile_front_ma.ui.map.MapFragment;
import com.example.mobile_front_ma.ui.navbar.NavBarFragment;

public class MainActivity extends AppCompatActivity
        implements NavBarFragment.NavBarListener {

    private final ActivityResultLauncher<String> notifPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                // If denied, in-app live updates still work; only the system notifications are lost.
            });

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
        configurePanicButton();


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
        // The "History of Rides" button opens the screen matching the logged-in role:
        //  - registered user -> own history (spec 2.9.1)
        //  - administrator    -> pick whose history to view (spec 2.9.3)
        //  - driver           -> driver history (existing screen)
        Button horDriverButton = findViewById(R.id.horDriverButton);
        horDriverButton.setOnClickListener(v -> startActivity(historyIntentForRole()));
    }

    private void configurePanicButton() {
        // The panic-alerts screen (spec 2.6.3) is for administrators only.
        Button panicButton = findViewById(R.id.panicAlertsButton);
        boolean isAdmin = "ADMIN".equalsIgnoreCase(new SessionManager(this).getRole());
        if (!isAdmin) {
            panicButton.setVisibility(View.GONE);
            return;
        }
        panicButton.setVisibility(View.VISIBLE);
        panicButton.setOnClickListener(v ->
                startActivity(new Intent(this, PanicNotificationsActivity.class)));

        // We raise Android notifications for incoming panics; request the runtime permission (13+).
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }

        // Keep listening for panic alerts in the background too (foreground service).
        PanicForegroundService.start(this);
    }

    private Intent historyIntentForRole() {
        String role = new SessionManager(this).getRole();
        if ("ADMIN".equalsIgnoreCase(role)) {
            return new Intent(this, AdminHistoryEntryActivity.class);
        }
        if ("DRIVER".equalsIgnoreCase(role)) {
            return new Intent(this, HORDriverActivity.class);
        }
        Intent intent = new Intent(this, RideHistoryActivity.class);
        intent.putExtra(RideHistoryActivity.EXTRA_MODE, RideHistoryActivity.MODE_USER);
        return intent;
    }
}
