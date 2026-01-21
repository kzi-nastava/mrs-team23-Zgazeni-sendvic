package com.example.mobile_front_ma.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile_front_ma.R;
import com.example.mobile_front_ma.models.Ride;

public class RidesAdapter extends ListAdapter<Ride, RidesAdapter.RideViewHolder> {

    public RidesAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<Ride> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull Ride oldItem, @NonNull Ride newItem) {
            return oldItem.getPickup().equals(newItem.getPickup()) && oldItem.getDestination().equals(newItem.getDestination());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Ride oldItem, @NonNull Ride newItem) {
            return oldItem.getFare().equals(newItem.getFare()) && oldItem.getDate().equals(newItem.getDate());
        }
    };

    @NonNull
    @Override
    public RideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hor_ride_layout, parent, false);
        return new RideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RideViewHolder holder, int position) {
        Ride ride = getItem(position);
        holder.pickupText.setText(ride.getPickup());
        holder.destinationText.setText(ride.getDestination());
        holder.fareText.setText(ride.getFare());
        holder.dateText.setText(ride.getDate());
    }

    public static class RideViewHolder extends RecyclerView.ViewHolder {
        TextView pickupText;
        TextView destinationText;
        TextView fareText;
        TextView dateText;

        public RideViewHolder(@NonNull View itemView) {
            super(itemView);
            pickupText = itemView.findViewById(R.id.pickupTextView);
            destinationText = itemView.findViewById(R.id.destinationTextView);
            fareText = itemView.findViewById(R.id.fareTextView);
            dateText = itemView.findViewById(R.id.dateTextView);
        }
    }
}
