package com.example.mobile_front_ma.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile_front_ma.R;
import com.example.mobile_front_ma.models.Ride;

public class DriverRidesAdapter
        extends ListAdapter<Ride, DriverRidesAdapter.RideViewHolder> {

    public interface OnStartRideListener {
        void onStartRide(long rideId);
    }

    private final OnStartRideListener listener;

    private static final DiffUtil.ItemCallback<Ride> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Ride>() {

                @Override
                public boolean areItemsTheSame(
                        @NonNull Ride oldItem,
                        @NonNull Ride newItem) {

                    return oldItem.getRideId() != null
                            && oldItem.getRideId().equals(newItem.getRideId());
                }

                @Override
                public boolean areContentsTheSame(
                        @NonNull Ride oldItem,
                        @NonNull Ride newItem) {

                    return oldItem.getRideId().equals(newItem.getRideId())
                            && equals(oldItem.getPickup(), newItem.getPickup())
                            && equals(oldItem.getDestination(), newItem.getDestination())
                            && equals(oldItem.getFare(), newItem.getFare())
                            && equals(oldItem.getDate(), newItem.getDate())
                            && equals(oldItem.getStatus(), newItem.getStatus());
                }

                private boolean equals(Object a, Object b) {
                    return a == null
                            ? b == null
                            : a.equals(b);
                }
            };

    public DriverRidesAdapter(
            OnStartRideListener listener) {

        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public RideViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(
                        R.layout.item_hor_driver_ride,
                        parent,
                        false
                );

        return new RideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull RideViewHolder holder,
            int position) {

        Ride ride = getItem(position);

        // Route
        String pickup = ride.getPickup() != null
                ? ride.getPickup()
                : "";

        String destination = ride.getDestination() != null
                ? ride.getDestination()
                : "";

        holder.routeText.setText(
                pickup + "  →  " + destination
        );

        // Date / departure time
        holder.dateText.setText(
                ride.getDate() != null
                        ? ride.getDate()
                        : ""
        );

        // Fare
        holder.fareText.setText(
                ride.getFare() != null
                        ? ride.getFare() + " RSD"
                        : "0 RSD"
        );

        String status = ride.getStatus();

        if ("ACTIVE".equals(status)) {

            holder.statusText.setVisibility(View.VISIBLE);
            holder.statusText.setText("ACTIVE");

            // Active rides cannot be started again
            holder.startRideButton.setVisibility(View.GONE);

        } else if ("SCHEDULED".equals(status)) {

            holder.statusText.setVisibility(View.VISIBLE);
            holder.statusText.setText("SCHEDULED");

            holder.startRideButton.setVisibility(View.VISIBLE);

            holder.startRideButton.setOnClickListener(v -> {

                if (ride.getRideId() != null) {
                    listener.onStartRide(
                            ride.getRideId()
                    );
                }
            });

        } else {

            holder.statusText.setVisibility(View.VISIBLE);
            holder.statusText.setText(
                    status != null
                            ? status
                            : "UNKNOWN"
            );

            holder.startRideButton.setVisibility(View.GONE);
        }
    }

    public static class RideViewHolder
            extends RecyclerView.ViewHolder {

        TextView routeText;
        TextView dateText;
        TextView fareText;
        TextView statusText;
        Button startRideButton;

        public RideViewHolder(
                @NonNull View itemView) {

            super(itemView);

            routeText =
                    itemView.findViewById(
                            R.id.horRideRouteTextView
                    );

            dateText =
                    itemView.findViewById(
                            R.id.horRideDateTextView
                    );

            fareText =
                    itemView.findViewById(
                            R.id.horRideFareTextView
                    );

            statusText =
                    itemView.findViewById(
                            R.id.horRideStatusTextView
                    );

            startRideButton =
                    itemView.findViewById(
                            R.id.startRideButton
                    );
        }
    }
}