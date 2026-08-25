package com.example.mobile_front_ma.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.mobile_front_ma.R;
import com.example.mobile_front_ma.viewmodels.BanAccountViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class BanAccountActivity extends AppCompatActivity {

    public static final String EXTRA_ACCOUNT_ID = "account_id";
    public static final String EXTRA_ACCOUNT_NAME = "account_name";
    public static final String EXTRA_ACCOUNT_EMAIL = "account_email";

    private BanAccountViewModel viewModel;

    private TextView accountNameText;
    private TextView accountEmailText;
    private TextInputLayout reasonLayout;
    private TextInputEditText reasonInput;

    private Button cancelButton;
    private Button banButton;
    private ProgressBar progress;

    private Long accountId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ban_account);
        applyInsets();

        viewModel = new ViewModelProvider(this)
                .get(BanAccountViewModel.class);

        accountId = getIntent().getLongExtra(
                EXTRA_ACCOUNT_ID,
                -1
        );

        String accountName = getIntent().getStringExtra(
                EXTRA_ACCOUNT_NAME
        );

        String accountEmail = getIntent().getStringExtra(
                EXTRA_ACCOUNT_EMAIL
        );

        if (accountId == null || accountId == -1) {
            Toast.makeText(
                    this,
                    "Invalid account.",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }

        accountNameText = findViewById(R.id.accountNameText);
        accountEmailText = findViewById(R.id.accountEmailText);
        reasonLayout = findViewById(R.id.reasonLayout);
        reasonInput = findViewById(R.id.reasonInput);

        cancelButton = findViewById(R.id.cancelButton);
        banButton = findViewById(R.id.banButton);
        progress = findViewById(R.id.banProgress);

        accountNameText.setText(
                accountName != null ? accountName : "Unknown account"
        );

        accountEmailText.setText(
                accountEmail != null ? accountEmail : ""
        );

        cancelButton.setOnClickListener(v -> finish());

        banButton.setOnClickListener(v -> submitBan());

        observeBanResult();
    }

    private void submitBan() {

        String reason = reasonInput.getText() != null
                ? reasonInput.getText().toString().trim()
                : "";

        reasonLayout.setError(null);

        if (reason.isEmpty()) {
            reasonLayout.setError(
                    "Please provide a reason for the ban."
            );
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Ban account?")
                .setMessage(
                        "Are you sure you want to ban "
                                + accountNameText.getText()
                                + "?\n\n"
                                + "Reason:\n"
                                + reason
                )
                .setNegativeButton("Cancel", null)
                .setPositiveButton(
                        "Ban Account",
                        (dialog, which) ->
                                viewModel.banAccount(
                                        accountId,
                                        reason
                                )
                )
                .show();
    }

    private void observeBanResult() {

        viewModel.getBanResult().observe(
                this,
                result -> {

                    if (result == null) {
                        return;
                    }

                    switch (result.status) {

                        case LOADING:
                            setLoading(true);
                            break;

                        case SUCCESS:
                            setLoading(false);

                            Toast.makeText(
                                    this,
                                    result.message != null
                                            ? result.message
                                            : "Account banned.",
                                    Toast.LENGTH_LONG
                            ).show();

                            setResult(RESULT_OK);
                            finish();
                            break;

                        case ERROR:
                            setLoading(false);

                            Toast.makeText(
                                    this,
                                    result.message,
                                    Toast.LENGTH_LONG
                            ).show();
                            break;
                    }
                }
        );
    }

    private void setLoading(boolean loading) {

        progress.setVisibility(
                loading ? View.VISIBLE : View.GONE
        );

        banButton.setEnabled(!loading);
        cancelButton.setEnabled(!loading);
        reasonInput.setEnabled(!loading);
    }

    private void applyInsets() {

        View root = findViewById(R.id.banAccountRoot);

        ViewCompat.setOnApplyWindowInsetsListener(
                root,
                (v, insets) -> {

                    Insets bars =
                            insets.getInsets(
                                    WindowInsetsCompat.Type.systemBars()
                            );

                    v.setPadding(
                            bars.left + 24,
                            bars.top + 24,
                            bars.right + 24,
                            bars.bottom + 24
                    );

                    return insets;
                }
        );
    }
}