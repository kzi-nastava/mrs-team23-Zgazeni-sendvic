package com.example.mobile_front_ma.activities;

import android.content.Intent;
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

import com.example.mobile_front_ma.MainActivity;
import com.example.mobile_front_ma.R;
import com.example.mobile_front_ma.viewmodels.LoginViewModel;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Login screen (spec 2.2.1). Entry point of the app: if a session already exists the
 * user is sent straight to the home screen, otherwise they sign in or go to registration.
 */
public class LoginActivity extends AppCompatActivity {

    private LoginViewModel viewModel;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private Button loginButton;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Already signed in? Skip the login screen.
        if (viewModel.isAlreadyLoggedIn()) {
            goToHome();
            return;
        }

        setContentView(R.layout.activity_login);
        applyInsets();

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        progress = findViewById(R.id.loginProgress);

        loginButton.setOnClickListener(v ->
                viewModel.login(textOf(emailInput), textOf(passwordInput)));

        findViewById(R.id.goToRegister).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        observeLogin();
    }

    private void observeLogin() {
        viewModel.getLoginResult().observe(this, result -> {
            if (result == null) {
                return;
            }
            switch (result.status) {
                case LOADING:
                    setLoading(true);
                    break;
                case SUCCESS:
                    setLoading(false);
                    Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show();
                    goToHome();
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
        loginButton.setEnabled(!loading);
    }

    private void goToHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private String textOf(TextInputEditText input) {
        return input.getText() != null ? input.getText().toString() : "";
    }

    private void applyInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginRoot), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }
}
