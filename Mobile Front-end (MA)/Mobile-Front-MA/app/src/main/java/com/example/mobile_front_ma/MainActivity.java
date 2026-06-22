package com.example.mobile_front_ma;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
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
import com.example.mobile_front_ma.data.DummyRide;
import com.example.mobile_front_ma.data.RideRepository;
import com.example.mobile_front_ma.data.SessionManager;
import com.example.mobile_front_ma.data.network.ApiCallback;
import com.example.mobile_front_ma.data.realtime.PanicForegroundService;
import com.example.mobile_front_ma.models.dto.PanicResponse;
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
        configureRaisePanicButton();


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

    private void configureRaisePanicButton() {
        // PANIC (spec 2.6.3) is raised by a ride participant (driver/passenger), not an admin —
        // admins handle incoming alerts on the panic-alerts screen instead, so hide it for them.
        Button raisePanicButton = findViewById(R.id.raisePanicButton);
        boolean isAdmin = "ADMIN".equalsIgnoreCase(new SessionManager(this).getRole());
        if (isAdmin) {
            raisePanicButton.setVisibility(View.GONE);
            return;
        }
        raisePanicButton.setVisibility(View.VISIBLE);
        raisePanicButton.setOnClickListener(v -> confirmPanic(raisePanicButton));
    }

    /** Ask for confirmation before raising the alarm, to avoid accidental panics (spec 2.6.3). */
    private void confirmPanic(Button raisePanicButton) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.hor_panic_confirm_title)
                .setMessage(R.string.hor_panic_confirm_message)
                .setNegativeButton(R.string.hor_panic_confirm_no, null)
                .setPositiveButton(R.string.hor_panic_confirm_yes, (dialog, which) -> raisePanic(raisePanicButton))
                .show();
    }

    /**
     * Raise the PANIC alarm on the ride in progress. The real in-progress-ride screen isn't built
     * yet, so the ride id is drawn from the shared {@link DummyRide} — the same dummy info the
     * stop-ride control uses. PANIC needs nothing more: the call carries no body and the backend
     * authorizes the caller from the JWT.
     */
    private void raisePanic(Button raisePanicButton) {
        // Block a double-tap while the alarm is being raised.
        raisePanicButton.setEnabled(false);
        new RideRepository(this).panicRide(DummyRide.RIDE_ID, new ApiCallback<PanicResponse>() {
            @Override
            public void onSuccess(PanicResponse data) {
                // Leave a persistent confirmation in place of the button so it can't be pressed twice.
                raisePanicButton.setText(R.string.hor_panic_sent);
                Toast.makeText(MainActivity.this, R.string.hor_panic_success, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(String message) {
                // Failed (already raised, not active anymore, network, ...): let the user retry.
                raisePanicButton.setEnabled(true);
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
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
