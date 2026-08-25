package com.example.mobile_front_ma.ui.profile;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.mobile_front_ma.R;
import com.example.mobile_front_ma.activities.EditProfileActivity;
import com.example.mobile_front_ma.activities.LoginActivity;
import com.example.mobile_front_ma.activities.ReportsActivity;
import com.example.mobile_front_ma.data.AuthRepository;
import com.example.mobile_front_ma.data.DriverRepository;
import com.example.mobile_front_ma.data.DummyRide;
import com.example.mobile_front_ma.data.RideRepository;
import com.example.mobile_front_ma.data.SessionManager;
import com.example.mobile_front_ma.data.network.ApiCallback;
import com.example.mobile_front_ma.data.realtime.PanicForegroundService;
import com.example.mobile_front_ma.models.dto.DriverStatusResponse;
import com.example.mobile_front_ma.models.dto.GetAccountDTO;
import com.example.mobile_front_ma.models.dto.LocationDto;
import com.example.mobile_front_ma.models.dto.RideStopRequest;
import com.example.mobile_front_ma.models.dto.RideStoppedResponse;
import com.example.mobile_front_ma.viewmodels.ProfileViewModel;

import android.content.res.ColorStateList;

import java.time.LocalDateTime;
import java.util.Arrays;

public class ProfileFragment extends Fragment {

    private SessionManager session;
    private DriverRepository driverRepository;
    private RideRepository rideRepository;
    private AuthRepository authRepository;

    private ProfileViewModel profileViewModel;

    // Profile UI
    private TextView displayName;
    private TextView nameValue;
    private TextView surnameValue;
    private TextView addressValue;
    private TextView emailValue;
    private TextView phoneValue;
    private TextView passwordValue;
    private ImageView profileImage;

    // Driver status UI
    private TextView statusText;
    private View statusDot;
    private Button toggleStatusButton;

    private GetAccountDTO currentAccount;

    private final ActivityResultLauncher<Intent> editProfileLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {

                        if (result.getResultCode() == Activity.RESULT_OK) {
                            profileViewModel.loadProfile();
                        }
                    }
            );

    public ProfileFragment() {}

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(
                R.layout.fragment_profile,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        // Existing repositories/session
        session = new SessionManager(requireContext());
        driverRepository = new DriverRepository(requireContext());
        rideRepository = new RideRepository(requireContext());
        authRepository = new AuthRepository();

        // Profile ViewModel
        profileViewModel = new ViewModelProvider(this)
                .get(ProfileViewModel.class);

        // Find profile views
        initializeProfileViews(view);

        // Observe profile data from the ViewModel
        observeProfile();

        // Existing functionality
        setupEditButton(view);
        setUpDriverControls(view);

        // Logout
        view.findViewById(R.id.logoutButton)
                .setOnClickListener(v -> logOut());

        // Reports
        view.findViewById(R.id.reportsButton)
                .setOnClickListener(v -> {
                    Intent intent =
                            new Intent(
                                    requireContext(),
                                    ReportsActivity.class
                            );

                    startActivity(intent);
                });

        // Finally request the profile from the backend
        profileViewModel.loadProfile();
    }

    /**
     * Finds all views that display information belonging to the
     * currently logged-in account.
     */
    private void initializeProfileViews(View view) {

        displayName = view.findViewById(R.id.displayName);

        nameValue = view.findViewById(R.id.nameValue);
        surnameValue = view.findViewById(R.id.surnameValue);
        addressValue = view.findViewById(R.id.addressValue);
        emailValue = view.findViewById(R.id.emailValue);
        phoneValue = view.findViewById(R.id.phoneValue);
        passwordValue = view.findViewById(R.id.passwordValue);
        profileImage = view.findViewById(R.id.profileImage);
    }

    /**
     * Observes the ProfileViewModel and updates the UI whenever
     * the profile request changes state.
     */
    private void observeProfile() {

        profileViewModel.getProfile().observe(
                getViewLifecycleOwner(),
                resource -> {

                    if (resource == null) {
                        return;
                    }

                    switch (resource.status) {

                        case LOADING:
                            // We can add a ProgressBar later if desired.
                            break;

                        case SUCCESS:

                            if (resource.data != null) {
                                currentAccount = resource.data;
                                displayProfile(resource.data);
                            }

                            break;

                        case ERROR:

                            if (!isAdded()) {
                                return;
                            }

                            Toast.makeText(
                                    requireContext(),
                                    resource.message,
                                    Toast.LENGTH_LONG
                            ).show();

                            break;
                    }
                }
        );
    }

    /**
     * Displays the profile returned by GET /api/account/me.
     */
    private void displayProfile(GetAccountDTO account) {

        currentAccount = account;

        String firstName = safeString(account.getName());
        String lastName = safeString(account.getLastName());

        // Full name at the top of the profile
        String fullName = (firstName + " " + lastName).trim();

        displayName.setText(
                fullName.isEmpty()
                        ? safeString(account.getEmail())
                        : fullName
        );

        // Individual fields
        nameValue.setText(firstName);
        surnameValue.setText(lastName);
        emailValue.setText(safeString(account.getEmail()));
        phoneValue.setText(safeString(account.getPhoneNumber()));
        addressValue.setText(
                safeString(account.getAddress())
        );
        displayProfileImage(account.getImgString());

        /*
         * Password must never be displayed from the backend.
         * Keep the existing masked representation.
         */
        passwordValue.setText("********");

        updateDriverStatistics(account);
    }

    private void displayProfileImage(String imageString) {

        if (imageString == null ||
                imageString.trim().isEmpty()) {

            profileImage.setImageResource(
                    R.drawable.profile_placeholder
            );

            return;
        }

        try {

            String cleanBase64 = imageString;

            if (cleanBase64.contains(",")) {
                cleanBase64 =
                        cleanBase64.substring(
                                cleanBase64.indexOf(",") + 1
                        );
            }

            byte[] bytes =
                    Base64.decode(
                            cleanBase64,
                            Base64.DEFAULT
                    );

            Bitmap bitmap =
                    BitmapFactory.decodeByteArray(
                            bytes,
                            0,
                            bytes.length
                    );

            if (bitmap != null) {
                profileImage.setImageBitmap(bitmap);
            } else {
                profileImage.setImageResource(
                        R.drawable.profile_placeholder
                );
            }

        } catch (Exception e) {

            profileImage.setImageResource(
                    R.drawable.profile_placeholder
            );
        }
    }

    /**
     * Displays information that is specific to drivers.
     *
     * GetAccountDTO currently provides totalDrivingHours, which
     * can be displayed here.
     */
    private void updateDriverStatistics(GetAccountDTO account) {

        View driverStats = requireView()
                .findViewById(R.id.driverStatisticContainer);

        TextView statLabel = requireView()
                .findViewById(R.id.statisticLabel);

        TextView statValue = requireView()
                .findViewById(R.id.statisticValue);

        boolean isDriver =
                "DRIVER".equalsIgnoreCase(account.getRole());

        if (!isDriver) {
            driverStats.setVisibility(View.GONE);
            return;
        }

        driverStats.setVisibility(View.VISIBLE);

        statLabel.setText("Driving hours");

        Integer hours = account.getTotalDrivingHours();

        statValue.setText(
                hours == null
                        ? "0"
                        : String.valueOf(hours)
        );
    }

    /**
     * Converts null strings into an empty string so that
     * TextViews never receive null.
     */
    private String safeString(String value) {
        return value == null ? "" : value;
    }

    /**
     * Profile editing will be implemented in the next stage.
     */
    private void setupEditButton(View view) {

        ImageButton editButton =
                view.findViewById(
                        R.id.editProfileButton
                );

        editButton.setOnClickListener(v -> {

            if (currentAccount == null) {
                Toast.makeText(
                        requireContext(),
                        "Profile data is still loading.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            Intent intent =
                    new Intent(
                            requireContext(),
                            EditProfileActivity.class
                    );

            intent.putExtra(
                    "email",
                    currentAccount.getEmail()
            );

            intent.putExtra(
                    "name",
                    currentAccount.getName()
            );

            intent.putExtra(
                    "lastName",
                    currentAccount.getLastName()
            );

            intent.putExtra(
                    "address",
                    currentAccount.getAddress()
            );

            intent.putExtra(
                    "phoneNumber",
                    currentAccount.getPhoneNumber()
            );

            intent.putExtra(
                    "role",
                    currentAccount.getRole()
            );

            intent.putExtra(
                    "imgString",
                    currentAccount.getImgString()
            );

            editProfileLauncher.launch(intent);
        });
    }

    /**
     * The active/inactive toggle and the stop-ride button only make sense
     * for drivers, so the whole block stays hidden for users and admins.
     */
    private void setUpDriverControls(View view) {

        View container =
                view.findViewById(R.id.driverControlsContainer);

        boolean isDriver =
                "DRIVER".equalsIgnoreCase(session.getRole());

        if (!isDriver) {
            container.setVisibility(View.GONE);
            return;
        }

        container.setVisibility(View.VISIBLE);

        statusText = view.findViewById(R.id.statusText);
        statusDot = view.findViewById(R.id.statusDot);
        toggleStatusButton =
                view.findViewById(R.id.toggleStatusButton);

        renderStatus(session.isDriverActive());

        toggleStatusButton.setOnClickListener(
                v -> toggleStatus()
        );

        view.findViewById(R.id.stopRideButton)
                .setOnClickListener(v -> stopRide());
    }

    /**
     * Reflect the current active/inactive state in the dot colour,
     * label and button text.
     */
    private void renderStatus(boolean active) {

        int green = Color.parseColor("#2E7D32");
        int grey = Color.parseColor("#9E9E9E");

        ViewCompat.setBackgroundTintList(
                statusDot,
                ColorStateList.valueOf(
                        active ? green : grey
                )
        );

        statusText.setText(
                active
                        ? R.string.driver_status_active
                        : R.string.driver_status_inactive
        );

        toggleStatusButton.setText(
                active
                        ? R.string.driver_go_inactive
                        : R.string.driver_go_active
        );
    }

    private void toggleStatus() {

        boolean target = !session.isDriverActive();

        toggleStatusButton.setEnabled(false);

        driverRepository.changeStatus(
                session.getToken(),
                session.getEmail(),
                target,
                new ApiCallback<DriverStatusResponse>() {

                    @Override
                    public void onSuccess(
                            DriverStatusResponse data
                    ) {

                        if (!isAdded()) {
                            return;
                        }

                        /*
                         * Use the state returned by the server rather than
                         * assuming that the requested state was accepted.
                         */
                        boolean actualActive =
                                data.isAvailable();

                        session.setDriverActive(
                                actualActive
                        );

                        renderStatus(actualActive);

                        toggleStatusButton.setEnabled(true);

                        String text =
                                data.getMessage() != null
                                        ? data.getMessage()
                                        : getString(
                                        actualActive
                                                ? R.string.driver_status_changed_active
                                                : R.string.driver_status_changed_inactive
                                );

                        Toast.makeText(
                                requireContext(),
                                text,
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onError(String message) {

                        if (!isAdded()) {
                            return;
                        }

                        toggleStatusButton.setEnabled(true);

                        Toast.makeText(
                                requireContext(),
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    /**
     * Stops a ride in progress (spec 2.6.5).
     *
     * The real in-progress ride screen is not implemented yet, so
     * the existing temporary DummyRide implementation remains untouched.
     */
    private void stopRide() {

        RideStopRequest request =
                new RideStopRequest(
                        Arrays.asList(
                                new LocationDto(
                                        45.2671,
                                        19.8335
                                ),
                                new LocationDto(
                                        45.2550,
                                        19.8450
                                )
                        ),
                        LocalDateTime.now().toString()
                );

        rideRepository.stopRide(
                DummyRide.RIDE_ID,
                request,
                new ApiCallback<RideStoppedResponse>() {

                    @Override
                    public void onSuccess(
                            RideStoppedResponse data
                    ) {

                        if (!isAdded()) {
                            return;
                        }

                        Toast.makeText(
                                requireContext(),
                                getString(
                                        R.string.driver_ride_stopped,
                                        String.valueOf(
                                                data.getNewPrice()
                                        )
                                ),
                                Toast.LENGTH_LONG
                        ).show();
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        if (!isAdded()) {
                            return;
                        }

                        Toast.makeText(
                                requireContext(),
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    /**
     * Ask the backend to log the user out, then clear the local session.
     */
    private void logOut() {

        authRepository.logout(
                requireContext(),
                new ApiCallback<Void>() {

                    @Override
                    public void onSuccess(Void data) {
                        finishLogout();
                    }

                    @Override
                    public void onError(String message) {

                        if (!isAdded()) {
                            return;
                        }

                        Toast.makeText(
                                requireContext(),
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void finishLogout() {

        if (!isAdded()) {
            return;
        }

        session.clear();

        // Stop the background panic listener.
        PanicForegroundService.stop(
                requireContext()
        );

        Toast.makeText(
                requireContext(),
                R.string.logout_success,
                Toast.LENGTH_SHORT
        ).show();

        // Return to login and clear the back stack.
        Intent intent =
                new Intent(
                        requireContext(),
                        LoginActivity.class
                );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);

        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (profileViewModel != null) {
            profileViewModel.loadProfile();
        }
    }
}