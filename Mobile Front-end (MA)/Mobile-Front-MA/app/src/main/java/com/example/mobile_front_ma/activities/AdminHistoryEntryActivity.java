package com.example.mobile_front_ma.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile_front_ma.R;
import com.example.mobile_front_ma.adapters.AccountAdapter;
import com.example.mobile_front_ma.models.dto.AccountListItem;
import com.example.mobile_front_ma.util.Resource;
import com.example.mobile_front_ma.viewmodels.AccountSearchViewModel;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin entry point for ride history (spec 2.9.3): search for the user or driver whose
 * history you want, then open {@link RideHistoryActivity} in admin mode for them.
 */
public class AdminHistoryEntryActivity extends AppCompatActivity
        implements AccountAdapter.OnAccountClickListener {

    private AccountSearchViewModel viewModel;
    private AccountAdapter adapter;
    private ProgressBar progressBar;
    private TextView emptyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_history_entry);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        progressBar = findViewById(R.id.progressBar);
        emptyText = findViewById(R.id.emptyText);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        adapter = new AccountAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        TextInputEditText searchInput = findViewById(R.id.searchInput);
        findViewById(R.id.searchButton).setOnClickListener(v ->
                doSearch(searchInput.getText() == null ? "" : searchInput.getText().toString()));
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                doSearch(searchInput.getText() == null ? "" : searchInput.getText().toString());
                return true;
            }
            return false;
        });

        viewModel = new ViewModelProvider(this).get(AccountSearchViewModel.class);
        viewModel.getAccounts().observe(this, this::render);

        // Show the full directory initially so the admin can browse without typing.
        viewModel.search("");
    }

    private void doSearch(String query) {
        viewModel.search(query.trim());
    }

    private void render(Resource<List<AccountListItem>> resource) {
        if (resource == null) {
            return;
        }
        switch (resource.status) {
            case LOADING:
                progressBar.setVisibility(View.VISIBLE);
                emptyText.setVisibility(View.GONE);
                break;
            case SUCCESS:
                progressBar.setVisibility(View.GONE);
                List<AccountListItem> data = resource.data == null ? new ArrayList<>() : resource.data;
                adapter.submitList(data);
                emptyText.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
                break;
            case ERROR:
                progressBar.setVisibility(View.GONE);
                adapter.submitList(new ArrayList<>());
                emptyText.setVisibility(View.VISIBLE);
                Toast.makeText(this, resource.message, Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    public void onAccountClick(AccountListItem account) {
        if (account.id == null) {
            return;
        }
        Intent intent = new Intent(this, RideHistoryActivity.class);
        intent.putExtra(RideHistoryActivity.EXTRA_MODE, RideHistoryActivity.MODE_ADMIN);
        intent.putExtra(RideHistoryActivity.EXTRA_TARGET_ID, account.id);
        intent.putExtra(RideHistoryActivity.EXTRA_TARGET_NAME, account.fullName());
        startActivity(intent);
    }
}
