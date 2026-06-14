package com.example.mobile_front_ma.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.mobile_front_ma.R;
import com.example.mobile_front_ma.viewmodels.ConfirmAccountViewModel;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Account activation screen (spec 2.2.2). Reached right after registration (email
 * pre-filled) or from the login screen. The user types the 6-digit code emailed to them
 * to activate the account before signing in.
 */
public class ConfirmAccountActivity extends AppCompatActivity {

    /** Optional email to pre-fill (passed from the registration screen). */
    public static final String EXTRA_EMAIL = "extra_email";

    private ConfirmAccountViewModel viewModel;
    private TextInputEditText emailInput;
    private TextInputEditText codeInput;
    private Button confirmButton;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_account);
        applyInsets();

        viewModel = new ViewModelProvider(this).get(ConfirmAccountViewModel.class);

        emailInput = findViewById(R.id.caEmailInput);
        codeInput = findViewById(R.id.caCodeInput);
        confirmButton = findViewById(R.id.caConfirmButton);
        progress = findViewById(R.id.caProgress);

        String prefill = getIntent().getStringExtra(EXTRA_EMAIL);
        if (prefill != null) {
            emailInput.setText(prefill);
        }

        confirmButton.setOnClickListener(v ->
                viewModel.confirm(textOf(emailInput), textOf(codeInput)));
        findViewById(R.id.caBackToLogin).setOnClickListener(v -> finish());

        observe();
    }

    private void observe() {
        viewModel.getConfirmResult().observe(this, result -> {
            if (result == null) {
                return;
            }
            switch (result.status) {
                case LOADING:
                    setLoading(true);
                    break;
                case SUCCESS:
                    setLoading(false);
                    Toast.makeText(this, R.string.confirm_success, Toast.LENGTH_LONG).show();
                    finish(); // back to the login screen
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
        confirmButton.setEnabled(!loading);
    }

    private String textOf(TextInputEditText input) {
        return input.getText() != null ? input.getText().toString() : "";
    }

    private void applyInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.confirmRoot), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }
}
