package com.example.mobile_front_ma.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.mobile_front_ma.R;
import com.example.mobile_front_ma.viewmodels.RegisterViewModel;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Registration screen (spec 2.2.2). Collects the account details, validates them, and
 * registers via the backend. The profile picture is optional — if none is chosen the
 * server falls back to a default image.
 */
public class RegisterActivity extends AppCompatActivity {

    private RegisterViewModel viewModel;
    private TextInputEditText firstNameInput;
    private TextInputEditText lastNameInput;
    private TextInputEditText emailInput;
    private TextInputEditText addressInput;
    private TextInputEditText phoneInput;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmPasswordInput;
    private Button registerButton;
    private ProgressBar progress;
    private ImageView photoPreview;

    private Uri selectedImageUri;

    // System photo/file picker. The chosen image is previewed and uploaded after registering.
    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    photoPreview.setImageURI(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        applyInsets();

        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        firstNameInput = findViewById(R.id.firstNameInput);
        lastNameInput = findViewById(R.id.lastNameInput);
        emailInput = findViewById(R.id.emailInput);
        addressInput = findViewById(R.id.addressInput);
        phoneInput = findViewById(R.id.phoneInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        registerButton = findViewById(R.id.registerButton);
        progress = findViewById(R.id.registerProgress);
        photoPreview = findViewById(R.id.photoPreview);

        findViewById(R.id.addPhotoButton).setOnClickListener(v -> pickImage.launch("image/*"));
        registerButton.setOnClickListener(v -> submit());
        findViewById(R.id.goToLogin).setOnClickListener(v -> finish());

        observeRegister();
    }

    private void submit() {
        byte[] pictureBytes = null;
        String pictureName = null;
        String pictureType = null;

        if (selectedImageUri != null) {
            try {
                pictureBytes = readBytes(selectedImageUri);
                pictureType = getContentResolver().getType(selectedImageUri);
                pictureName = "profile" + extensionFor(pictureType);
            } catch (IOException e) {
                Toast.makeText(this, "Couldn't read the selected image.", Toast.LENGTH_LONG).show();
                return;
            }
        }

        viewModel.register(
                textOf(emailInput), textOf(passwordInput), textOf(confirmPasswordInput),
                textOf(firstNameInput), textOf(lastNameInput), textOf(addressInput), textOf(phoneInput),
                pictureBytes, pictureName, pictureType);
    }

    private void observeRegister() {
        viewModel.getRegisterResult().observe(this, result -> {
            if (result == null) {
                return;
            }
            switch (result.status) {
                case LOADING:
                    setLoading(true);
                    break;
                case SUCCESS:
                    setLoading(false);
                    String message = result.data != null ? result.data
                            : getString(R.string.register_success);
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    // Account exists but is inactive: send the user to enter the
                    // activation code we just emailed them (email pre-filled).
                    Intent intent = new Intent(this, ConfirmAccountActivity.class);
                    intent.putExtra(ConfirmAccountActivity.EXTRA_EMAIL, textOf(emailInput));
                    startActivity(intent);
                    finish(); // pop registration; back returns to login
                    break;
                case ERROR:
                    setLoading(false);
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        registerButton.setEnabled(!loading);
    }

    private byte[] readBytes(Uri uri) throws IOException {
        try (InputStream is = getContentResolver().openInputStream(uri);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            if (is == null) {
                throw new IOException("Cannot open image stream");
            }
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) != -1) {
                bos.write(buffer, 0, read);
            }
            return bos.toByteArray();
        }
    }

    private String extensionFor(String mime) {
        if (mime == null) {
            return ".jpg";
        }
        if (mime.contains("png")) {
            return ".png";
        }
        if (mime.contains("webp")) {
            return ".webp";
        }
        return ".jpg";
    }

    private String textOf(TextInputEditText input) {
        return input.getText() != null ? input.getText().toString() : "";
    }

    private void applyInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registerRoot), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }
}
