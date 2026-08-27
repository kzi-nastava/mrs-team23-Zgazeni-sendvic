package com.example.mobile_front_ma.activities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.mobile_front_ma.R;
import com.example.mobile_front_ma.models.dto.CreateDriverRequest;
import com.example.mobile_front_ma.models.dto.RegisterVehicleRequest;
import com.example.mobile_front_ma.viewmodels.DriverRegistrationViewModel;

public class DriverRegistrationActivity
        extends AppCompatActivity {

    private EditText emailInput;
    private EditText nameInput;
    private EditText lastNameInput;
    private EditText addressInput;
    private EditText phoneInput;

    private EditText modelInput;
    private EditText registrationInput;
    private Spinner vehicleTypeSpinner;
    private EditText seatsInput;

    private CheckBox babiesAllowedCheckBox;
    private CheckBox petsAllowedCheckBox;

    private Button registerDriverButton;
    private Button cancelButton;

    private DriverRegistrationViewModel viewModel;

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_driver_registration
        );

        viewModel =
                new ViewModelProvider(this)
                        .get(
                                DriverRegistrationViewModel.class
                        );

        initializeViews();
        setupVehicleTypeSpinner();
        setupButtons();
        observeRegistration();
    }

    private void initializeViews() {

        emailInput =
                findViewById(R.id.emailInput);

        nameInput =
                findViewById(R.id.nameInput);

        lastNameInput =
                findViewById(R.id.lastNameInput);

        addressInput =
                findViewById(R.id.addressInput);

        phoneInput =
                findViewById(R.id.phoneInput);

        modelInput =
                findViewById(R.id.modelInput);

        registrationInput =
                findViewById(R.id.registrationInput);

        vehicleTypeSpinner =
                findViewById(R.id.vehicleTypeSpinner);

        seatsInput =
                findViewById(R.id.seatsInput);

        babiesAllowedCheckBox =
                findViewById(
                        R.id.babiesAllowedCheckBox
                );

        petsAllowedCheckBox =
                findViewById(
                        R.id.petsAllowedCheckBox
                );

        registerDriverButton =
                findViewById(
                        R.id.registerDriverButton
                );

        cancelButton =
                findViewById(
                        R.id.cancelButton
                );
    }

    private void setupVehicleTypeSpinner() {

        String[] types = {
                "STANDARD",
                "LUXURY",
                "VAN"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        types
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        vehicleTypeSpinner.setAdapter(adapter);
    }

    private void setupButtons() {

        cancelButton.setOnClickListener(
                v -> finish()
        );

        registerDriverButton.setOnClickListener(
                v -> registerDriver()
        );
    }

    private void registerDriver() {

        String email =
                emailInput.getText()
                        .toString()
                        .trim();

        String name =
                nameInput.getText()
                        .toString()
                        .trim();

        String lastName =
                lastNameInput.getText()
                        .toString()
                        .trim();

        String address =
                addressInput.getText()
                        .toString()
                        .trim();

        String phone =
                phoneInput.getText()
                        .toString()
                        .trim();

        String model =
                modelInput.getText()
                        .toString()
                        .trim();

        String registration =
                registrationInput.getText()
                        .toString()
                        .trim()
                        .toUpperCase();

        String vehicleType =
                vehicleTypeSpinner
                        .getSelectedItem()
                        .toString();

        String seatsText =
                seatsInput.getText()
                        .toString()
                        .trim();

        /*
         * Basic validation.
         */

        if (email.isEmpty()) {
            emailInput.setError("Email is required");
            emailInput.requestFocus();
            return;
        }

        if (name.isEmpty()) {
            nameInput.setError("First name is required");
            nameInput.requestFocus();
            return;
        }

        if (lastName.isEmpty()) {
            lastNameInput.setError("Last name is required");
            lastNameInput.requestFocus();
            return;
        }

        if (address.isEmpty()) {
            addressInput.setError("Address is required");
            addressInput.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            phoneInput.setError("Phone number is required");
            phoneInput.requestFocus();
            return;
        }

        if (model.isEmpty()) {
            modelInput.setError("Vehicle model is required");
            modelInput.requestFocus();
            return;
        }

        if (registration.isEmpty()) {
            registrationInput.setError(
                    "Vehicle registration is required"
            );
            registrationInput.requestFocus();
            return;
        }

        if (seatsText.isEmpty()) {
            seatsInput.setError(
                    "Number of seats is required"
            );
            seatsInput.requestFocus();
            return;
        }

        int seats;

        try {
            seats = Integer.parseInt(seatsText);
        } catch (NumberFormatException e) {

            seatsInput.setError(
                    "Invalid number of seats"
            );

            seatsInput.requestFocus();
            return;
        }

        if (seats < 1 || seats > 9) {

            seatsInput.setError(
                    "Number of seats must be between 1 and 9"
            );

            seatsInput.requestFocus();
            return;
        }

        /*
         * Build the vehicle request.
         */
        RegisterVehicleRequest vehicleRequest =
                new RegisterVehicleRequest(
                        model,
                        registration,
                        vehicleType,
                        seats,
                        babiesAllowedCheckBox.isChecked(),
                        petsAllowedCheckBox.isChecked()
                );

        /*
         * Build the driver request.
         *
         * vehicleId is intentionally 0/null here because the
         * ViewModel will insert the actual vehicle ID after
         * the vehicle is successfully created.
         */
        CreateDriverRequest driverRequest =
                new CreateDriverRequest(
                        email,
                        null,
                        name,
                        lastName,
                        address,
                        null,
                        phone,
                        null
                );

        registerDriverButton.setEnabled(false);

        viewModel.registerDriver(
                vehicleRequest,
                driverRequest
        );
    }

    private void observeRegistration() {

        viewModel.getRegistrationResult()
                .observe(
                        this,
                        resource -> {

                            if (resource == null) {
                                return;
                            }

                            switch (resource.status) {

                                case LOADING:

                                    registerDriverButton
                                            .setEnabled(false);

                                    break;

                                case SUCCESS:

                                    registerDriverButton
                                            .setEnabled(true);

                                    Toast.makeText(
                                            this,
                                            "Driver account created successfully. An activation email has been sent.",
                                            Toast.LENGTH_LONG
                                    ).show();

                                    setResult(
                                            Activity.RESULT_OK
                                    );

                                    finish();

                                    break;

                                case ERROR:

                                    registerDriverButton
                                            .setEnabled(true);

                                    Toast.makeText(
                                            this,
                                            resource.message,
                                            Toast.LENGTH_LONG
                                    ).show();

                                    break;
                            }
                        }
                );
    }
}