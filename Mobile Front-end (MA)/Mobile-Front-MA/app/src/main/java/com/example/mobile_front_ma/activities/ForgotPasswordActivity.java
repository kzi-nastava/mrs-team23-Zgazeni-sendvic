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
import com.example.mobile_front_ma.viewmodels.ForgotPasswordViewModel;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Password reset screen (spec 2.2.1). Two steps on one screen: request a code by email,
 * then enter that code and a new password. The reset section is revealed only after a
 * code has been requested.
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    /** Optional email to pre-fill (passed from the login screen). */
    public static final String EXTRA_EMAIL = "extra_email";

    private ForgotPasswordViewModel viewModel;
    private TextInputEditText emailInput;
    private TextInputEditText codeInput;
    private TextInputEditText newPasswordInput;
    private TextInputEditText confirmPasswordInput;
    private Button sendCodeButton;
    private Button resetButton;
    private View step2;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        applyInsets();

        viewModel = new ViewModelProvider(this).get(ForgotPasswordViewModel.class);

        emailInput = findViewById(R.id.fpEmailInput);
        codeInput = findViewById(R.id.fpCodeInput);
        newPasswordInput = findViewById(R.id.fpNewPasswordInput);
        confirmPasswordInput = findViewById(R.id.fpConfirmPasswordInput);
        sendCodeButton = findViewById(R.id.fpSendCodeButton);
        resetButton = findViewById(R.id.fpResetButton);
        step2 = findViewById(R.id.fpStep2);
        progress = findViewById(R.id.fpProgress);

        String prefill = getIntent().getStringExtra(EXTRA_EMAIL);
        if (prefill != null) {
            emailInput.setText(prefill);
        }

        sendCodeButton.setOnClickListener(v -> viewModel.sendCode(textOf(emailInput)));
        resetButton.setOnClickListener(v -> viewModel.resetPassword(
                textOf(emailInput), textOf(codeInput),
                textOf(newPasswordInput), textOf(confirmPasswordInput)));
        findViewById(R.id.fpBackToLogin).setOnClickListener(v -> finish());

        observe();
    }

    private void observe() {
        viewModel.getSendCodeResult().observe(this, result -> {
            if (result == null) {
                return;
            }
            switch (result.status) {
                case LOADING:
                    setLoading(true);
                    break;
                case SUCCESS:
                    setLoading(false);
                    step2.setVisibility(View.VISIBLE);
                    Toast.makeText(this, R.string.forgot_code_sent, Toast.LENGTH_LONG).show();
                    break;
                case ERROR:
                    setLoading(false);
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show();
                    break;
            }
        });

        viewModel.getResetResult().observe(this, result -> {
            if (result == null) {
                return;
            }
            switch (result.status) {
                case LOADING:
                    setLoading(true);
                    break;
                case SUCCESS:
                    setLoading(false);
                    Toast.makeText(this, R.string.reset_success, Toast.LENGTH_LONG).show();
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
        sendCodeButton.setEnabled(!loading);
        resetButton.setEnabled(!loading);
    }

    private String textOf(TextInputEditText input) {
        return input.getText() != null ? input.getText().toString() : "";
    }

    private void applyInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.forgotRoot), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }
}
