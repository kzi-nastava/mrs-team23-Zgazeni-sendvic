package com.example.mobile_front_ma.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile_front_ma.R;
import com.example.mobile_front_ma.data.RideRepository;
import com.example.mobile_front_ma.data.network.ApiCallback;
import com.example.mobile_front_ma.models.dto.ActiveRideDTO;
import com.example.mobile_front_ma.models.dto.RidesOverviewDTO;

import java.util.ArrayList;
import java.util.List;

public class ActiveRidesFragment extends Fragment {

    private RideRepository rideRepository;
    private ActiveRidesAdapter adapter;
    private EditText etSearchDriver;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_active_rides, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rideRepository = new RideRepository(requireContext());
        etSearchDriver = view.findViewById(R.id.etSearchDriver);
        Button btnSearch = view.findViewById(R.id.btnSearch);
        RecyclerView rvActiveRides = view.findViewById(R.id.rvActiveRides);

        rvActiveRides.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ActiveRidesAdapter(new ArrayList<>(), this::showRideDetails);
        rvActiveRides.setAdapter(adapter);

        btnSearch.setOnClickListener(v -> fetchRides(etSearchDriver.getText().toString()));

        // Initial fetch
        fetchRides("");
    }

    private void fetchRides(String driverName) {
        rideRepository.getRidesOverview(driverName, new ApiCallback<RidesOverviewDTO>() {
            @Override
            public void onSuccess(RidesOverviewDTO result) {
                if (result != null && result.activeRides != null) {
                    adapter.setRides(result.activeRides);
                } else {
                    adapter.setRides(new ArrayList<>());
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(getContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showRideDetails(ActiveRideDTO ride) {
        // We could fetch fresh details using getRideById(ride.id) if needed,
        // but the spec says the info is in ActiveRideDTO.
        
        StringBuilder details = new StringBuilder();
        details.append("ID: ").append(ride.id).append("\n");
        details.append("Status: ").append(ride.status).append("\n");
        details.append("Driver: ").append(ride.driverFirstName).append(" (").append(ride.driverEmail).append(")\n");
        details.append("Date: ").append(ride.date).append("\n");
        details.append("Departure: ").append(ride.departureTime).append("\n");
        details.append("Arrival: ").append(ride.arrivalTime).append("\n");
        details.append("Price: ").append(ride.price).append(" RSD\n");
        details.append("Panic: ").append(ride.panic ? "YES" : "No");

        new AlertDialog.Builder(requireContext())
                .setTitle("Ride Details")
                .setMessage(details.toString())
                .setPositiveButton("Close", null)
                .show();
    }

    private static class ActiveRidesAdapter extends RecyclerView.Adapter<ActiveRidesAdapter.ViewHolder> {

        private List<ActiveRideDTO> rides;
        private final OnItemClickListener listener;

        interface OnItemClickListener {
            void onItemClick(ActiveRideDTO ride);
        }

        ActiveRidesAdapter(List<ActiveRideDTO> rides, OnItemClickListener listener) {
            this.rides = rides;
            this.listener = listener;
        }

        void setRides(List<ActiveRideDTO> rides) {
            this.rides = rides;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_active_ride, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ActiveRideDTO ride = rides.get(position);
            holder.tvDriverName.setText("Driver: " + ride.driverFirstName);
            holder.tvRideDate.setText("Date: " + ride.date);
            holder.tvStatus.setText("Status: " + ride.status);
            // Assuming origin location is not null and has a displayable property or just showing coordinates
            if (ride.origin != null) {
                holder.tvStartLocation.setText(String.format("Start: %.4f, %.4f", ride.origin.latitude, ride.origin.longitude));
            }
            holder.itemView.setOnClickListener(v -> listener.onItemClick(ride));
        }

        @Override
        public int getItemCount() {
            return rides.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvDriverName, tvRideDate, tvStartLocation, tvStatus;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvDriverName = itemView.findViewById(R.id.tvDriverName);
                tvRideDate = itemView.findViewById(R.id.tvRideDate);
                tvStartLocation = itemView.findViewById(R.id.tvStartLocation);
                tvStatus = itemView.findViewById(R.id.tvStatus);
            }
        }
    }
}
