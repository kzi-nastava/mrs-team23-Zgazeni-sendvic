package com.example.mobile_front_ma.activities;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.mobile_front_ma.R;
import com.example.mobile_front_ma.models.dto.UpdateAccountDTO;
import com.example.mobile_front_ma.viewmodels.EditProfileViewModel;
import com.example.mobile_front_ma.util.Resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class EditProfileActivity extends AppCompatActivity {

    private EditText nameInput;
    private EditText lastNameInput;
    private EditText emailInput;
    private EditText addressInput;
    private EditText phoneInput;

    private ImageView profileImage;

    private Button changeImageButton;
    private Button backButton;
    private Button saveButton;

    private EditProfileViewModel viewModel;

    private String role;

    /*
     * Contains the newly selected image as raw Base64.
     *
     * null means that the user did not select a new image.
     */
    private String selectedImageBase64;

    /*
     * Modern Android image picker.
     */
    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {

                        if (uri == null) {
                            return;
                        }

                        try {

                            /*
                             * Show the selected image immediately.
                             */
                            profileImage.setImageURI(uri);

                            /*
                             * Convert it to Base64 for the backend.
                             */
                            selectedImageBase64 =
                                    convertImageToBase64(uri);

                            Toast.makeText(
                                    this,
                                    "Profile image selected.",
                                    Toast.LENGTH_SHORT
                            ).show();

                        } catch (Exception e) {

                            selectedImageBase64 = null;

                            Toast.makeText(
                                    this,
                                    "Couldn't load selected image.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
            );

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_edit_profile
        );

        viewModel =
                new ViewModelProvider(this)
                        .get(EditProfileViewModel.class);

        initializeViews();
        loadExistingData();
        setupButtons();
        observeUpdate();
    }

    private void initializeViews() {

        nameInput =
                findViewById(R.id.nameInput);

        lastNameInput =
                findViewById(R.id.lastNameInput);

        emailInput =
                findViewById(R.id.emailInput);

        addressInput =
                findViewById(R.id.addressInput);

        phoneInput =
                findViewById(R.id.phoneInput);

        profileImage =
                findViewById(R.id.editProfileImage);

        changeImageButton =
                findViewById(R.id.changeImageButton);

        backButton =
                findViewById(R.id.backButton);

        saveButton =
                findViewById(R.id.saveProfileButton);
    }

    private void loadExistingData() {

        nameInput.setText(
                getIntent().getStringExtra("name")
        );

        lastNameInput.setText(
                getIntent().getStringExtra("lastName")
        );

        emailInput.setText(
                getIntent().getStringExtra("email")
        );

        addressInput.setText(
                getIntent().getStringExtra("address")
        );

        phoneInput.setText(
                getIntent().getStringExtra("phoneNumber")
        );

        role =
                getIntent().getStringExtra("role");

        /*
         * Load the existing profile image.
         *
         * The backend stores the image as raw Base64,
         * without a data:image/... prefix.
         */
        selectedImageBase64 =
                getIntent().getStringExtra("imgString");

        if (selectedImageBase64 != null &&
                !selectedImageBase64.isBlank()) {

            try {

                byte[] imageBytes =
                        Base64.decode(
                                selectedImageBase64,
                                Base64.DEFAULT
                        );

                Bitmap bitmap =
                        BitmapFactory.decodeByteArray(
                                imageBytes,
                                0,
                                imageBytes.length
                        );

                if (bitmap != null) {

                    profileImage.setImageBitmap(
                            bitmap
                    );
                }

            } catch (IllegalArgumentException e) {

                /*
                 * Invalid Base64.
                 *
                 * Keep the default profile placeholder.
                 */
                profileImage.setImageResource(
                        R.drawable.profile_placeholder
                );
            }
        }
    }

    private void setupButtons() {

        /*
         * Change profile image.
         */
        changeImageButton.setOnClickListener(v ->
                imagePickerLauncher.launch("image/*")
        );

        /*
         * Back without saving.
         */
        backButton.setOnClickListener(v -> {
            finish();
        });

        /*
         * Save profile.
         */
        saveButton.setOnClickListener(v -> {

            String name =
                    nameInput.getText()
                            .toString()
                            .trim();

            String lastName =
                    lastNameInput.getText()
                            .toString()
                            .trim();

            String email =
                    emailInput.getText()
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

            /*
             * Basic validation.
             */
            if (name.isEmpty()) {

                nameInput.setError(
                        "First name is required"
                );

                nameInput.requestFocus();

                return;
            }

            if (lastName.isEmpty()) {

                lastNameInput.setError(
                        "Last name is required"
                );

                lastNameInput.requestFocus();

                return;
            }

            if (address.isEmpty()) {

                addressInput.setError(
                        "Address is required"
                );

                addressInput.requestFocus();

                return;
            }

            if (phone.isEmpty()) {

                phoneInput.setError(
                        "Phone number is required"
                );

                phoneInput.requestFocus();

                return;
            }

            /*
             * selectedImageBase64 is null if the user didn't
             * change the image.
             */
            UpdateAccountDTO request =
                    new UpdateAccountDTO(
                            email,
                            null,
                            name,
                            lastName,
                            address,
                            phone,
                            selectedImageBase64
                    );

            saveButton.setEnabled(false);

            viewModel.updateProfile(request);
        });
    }

    private void observeUpdate() {

        viewModel.getUpdateResult()
                .observe(
                        this,
                        resource -> {

                            if (resource == null) {
                                return;
                            }

                            switch (resource.status) {

                                case LOADING:

                                    break;

                                case SUCCESS:

                                    boolean isDriver =
                                            "DRIVER".equalsIgnoreCase(
                                                    role
                                            );

                                    String message =
                                            isDriver
                                                    ? "Profile changes submitted for admin approval."
                                                    : "Profile updated successfully.";

                                    Toast.makeText(
                                            this,
                                            message,
                                            Toast.LENGTH_LONG
                                    ).show();

                                    /*
                                     * Tell ProfileFragment that
                                     * the profile was successfully
                                     * updated.
                                     */
                                    setResult(
                                            Activity.RESULT_OK
                                    );

                                    /*
                                     * Return to profile.
                                     */
                                    finish();

                                    break;

                                case ERROR:

                                    saveButton.setEnabled(true);

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

    /**
     * Converts the selected image into a resized JPEG and then
     * encodes it as raw Base64.
     *
     * No "data:image/jpeg;base64," prefix is added.
     */
    private String convertImageToBase64(Uri uri)
            throws IOException {

        /*
         * Open the selected image.
         */
        InputStream inputStream =
                getContentResolver()
                        .openInputStream(uri);

        if (inputStream == null) {
            throw new IOException(
                    "Couldn't open selected image."
            );
        }

        /*
         * Decode the image.
         */
        Bitmap original =
                BitmapFactory.decodeStream(
                        inputStream
                );

        inputStream.close();

        if (original == null) {
            throw new IOException(
                    "Couldn't decode selected image."
            );
        }

        /*
         * Resize the image so that large phone photos
         * don't produce enormous HTTP requests.
         */
        Bitmap resized =
                resizeBitmap(
                        original,
                        600
                );

        /*
         * Compress to JPEG.
         *
         * 80 gives a good balance between quality and size
         * for a profile picture.
         */
        ByteArrayOutputStream outputStream =
                new ByteArrayOutputStream();

        resized.compress(
                Bitmap.CompressFormat.JPEG,
                80,
                outputStream
        );

        byte[] imageBytes =
                outputStream.toByteArray();

        /*
         * Raw Base64.
         */
        return Base64.encodeToString(
                imageBytes,
                Base64.NO_WRAP
        );
    }

    /**
     * Keeps the image aspect ratio while making sure
     * neither dimension exceeds maxSize.
     */
    private Bitmap resizeBitmap(
            Bitmap bitmap,
            int maxSize
    ) {

        int width =
                bitmap.getWidth();

        int height =
                bitmap.getHeight();

        /*
         * Already small enough.
         */
        if (width <= maxSize &&
                height <= maxSize) {

            return bitmap;
        }

        float scale =
                Math.min(
                        (float) maxSize / width,
                        (float) maxSize / height
                );

        int newWidth =
                Math.round(
                        width * scale
                );

        int newHeight =
                Math.round(
                        height * scale
                );

        return Bitmap.createScaledBitmap(
                bitmap,
                newWidth,
                newHeight,
                true
        );
    }
}