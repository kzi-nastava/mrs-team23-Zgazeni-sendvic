package com.example.mobile_front_ma.ui.map;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.mobile_front_ma.R;
import com.example.mobile_front_ma.models.Place;
import com.example.mobile_front_ma.models.RouteEstimate;
import com.example.mobile_front_ma.util.Resource;
import com.example.mobile_front_ma.viewmodels.RideEstimateViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.function.Consumer;

/**
 * The form opened by the "Estimate ride" button (spec 2.1.2). Lets the user type a start
 * and destination (autocompleted to Novi Sad addresses); once both are picked and "Show
 * route" is pressed, it asks for the estimate and hands the result to the host MapFragment
 * to draw on the map.
 */
public class RideEstimateBottomSheetFragment extends BottomSheetDialogFragment {

    /** Implemented by the host (MapFragment) to draw the computed route on the map. */
    public interface Listener {
        void onRouteEstimated(Place start, Place destination, RouteEstimate estimate);
    }

    private static final long SEARCH_DEBOUNCE_MS = 300L;
    private static final int MIN_QUERY_LENGTH = 2;

    private RideEstimateViewModel viewModel;
    private Listener listener;

    private AutoCompleteTextView startInput;
    private AutoCompleteTextView destinationInput;
    private PlaceSuggestionAdapter startAdapter;
    private PlaceSuggestionAdapter destinationAdapter;
    private Button showRouteButton;
    private ProgressBar progress;

    private Place selectedStart;
    private Place selectedDestination;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable startSearch = () ->
            searchIfLongEnough(startInput, viewModel::searchStart);
    private final Runnable destinationSearch = () ->
            searchIfLongEnough(destinationInput, viewModel::searchDestination);

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Fragment parent = getParentFragment();
        if (parent instanceof Listener) {
            listener = (Listener) parent;
        } else if (context instanceof Listener) {
            listener = (Listener) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sheet_ride_estimate, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(RideEstimateViewModel.class);

        startInput = view.findViewById(R.id.startInput);
        destinationInput = view.findViewById(R.id.destinationInput);
        showRouteButton = view.findViewById(R.id.showRouteButton);
        progress = view.findViewById(R.id.estimateProgress);

        startAdapter = new PlaceSuggestionAdapter(requireContext());
        destinationAdapter = new PlaceSuggestionAdapter(requireContext());
        startInput.setAdapter(startAdapter);
        destinationInput.setAdapter(destinationAdapter);

        wireField(startInput, startSearch, place -> selectedStart = place);
        wireField(destinationInput, destinationSearch, place -> selectedDestination = place);

        showRouteButton.setOnClickListener(v -> onShowRoute());

        observeViewModel();
    }

    @Override
    @SuppressWarnings("deprecation") // ADJUST_RESIZE is deprecated but still the simplest
    public void onStart() {           // way to keep the sheet's inputs above the keyboard.
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }

    private void wireField(AutoCompleteTextView field, Runnable search, Consumer<Place> onPicked) {
        field.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Editing the field invalidates any previously picked suggestion.
                onPicked.accept(null);
                handler.removeCallbacks(search);
                handler.postDelayed(search, SEARCH_DEBOUNCE_MS);
            }
        });
        field.setOnItemClickListener((parent, v, position, id) -> {
            // The selection already set the text (which scheduled a search) - cancel it.
            handler.removeCallbacks(search);
            PlaceSuggestionAdapter adapter = (PlaceSuggestionAdapter) parent.getAdapter();
            onPicked.accept(adapter.getItem(position));
        });
    }

    private void searchIfLongEnough(AutoCompleteTextView field, Consumer<String> search) {
        String query = field.getText().toString().trim();
        if (query.length() >= MIN_QUERY_LENGTH) {
            search.accept(query);
        }
    }

    private void observeViewModel() {
        viewModel.getStartSuggestions().observe(getViewLifecycleOwner(), places -> {
            startAdapter.setData(places);
            if (!places.isEmpty() && startInput.hasFocus()) {
                startInput.showDropDown();
            }
        });
        viewModel.getDestinationSuggestions().observe(getViewLifecycleOwner(), places -> {
            destinationAdapter.setData(places);
            if (!places.isEmpty() && destinationInput.hasFocus()) {
                destinationInput.showDropDown();
            }
        });
        viewModel.getRouteEstimate().observe(getViewLifecycleOwner(), this::handleEstimate);
    }

    private void onShowRoute() {
        if (selectedStart == null || selectedDestination == null) {
            Toast.makeText(requireContext(), R.string.estimate_error_pick, Toast.LENGTH_SHORT).show();
            return;
        }
        hideKeyboard();
        viewModel.estimate(selectedStart, selectedDestination);
    }

    private void handleEstimate(Resource<RouteEstimate> resource) {
        if (resource == null) {
            return;
        }
        switch (resource.status) {
            case LOADING:
                setLoading(true);
                break;
            case SUCCESS:
                setLoading(false);
                if (listener != null && resource.data != null) {
                    listener.onRouteEstimated(selectedStart, selectedDestination, resource.data);
                }
                dismiss();
                break;
            case ERROR:
                setLoading(false);
                Toast.makeText(requireContext(), resource.message, Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        showRouteButton.setEnabled(!loading);
        showRouteButton.setText(loading ? "" : getString(R.string.action_show_route));
    }

    private void hideKeyboard() {
        View focused = getDialog() != null ? getDialog().getCurrentFocus() : null;
        if (focused != null) {
            InputMethodManager imm = (InputMethodManager)
                    requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(focused.getWindowToken(), 0);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
    }
}
