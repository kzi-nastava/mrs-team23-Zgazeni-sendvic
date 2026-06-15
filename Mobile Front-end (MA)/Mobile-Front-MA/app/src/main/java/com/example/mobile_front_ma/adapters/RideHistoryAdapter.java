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
import com.example.mobile_front_ma.models.dto.RideHistoryItem;
import com.example.mobile_front_ma.util.HistoryFormat;

/**
 * Renders a ride-history row. The admin payload carries extra fields (price, status, panic)
 * which are shown only when present, so the same adapter serves both 2.9.1 and 2.9.3.
 */
public class RideHistoryAdapter extends ListAdapter<RideHistoryItem, RideHistoryAdapter.RideViewHolder> {

    public interface OnRideClickListener {
        void onRideClick(RideHistoryItem ride);
    }

    private final OnRideClickListener listener;

    public RideHistoryAdapter(OnRideClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<RideHistoryItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<RideHistoryItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull RideHistoryItem o, @NonNull RideHistoryItem n) {
                    return o.rideID != null && o.rideID.equals(n.rideID);
                }

                @Override
                public boolean areContentsTheSame(@NonNull RideHistoryItem o, @NonNull RideHistoryItem n) {
                    return HistoryFormat.safe(o.creationTime).equals(HistoryFormat.safe(n.creationTime))
                            && HistoryFormat.safe(o.status).equals(HistoryFormat.safe(n.status));
                }
            };

    @NonNull
    @Override
    public RideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ride_history, parent, false);
        return new RideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RideViewHolder holder, int position) {
        RideHistoryItem ride = getItem(position);

        holder.routeText.setText(HistoryFormat.point(ride.getStart())
                + "  →  " + HistoryFormat.point(ride.getEnd()));
        holder.createdText.setText(holder.itemView.getContext()
                .getString(R.string.hor_created_prefix, HistoryFormat.dateTime(ride.creationTime)));
        holder.timesText.setText(HistoryFormat.dateTime(ride.beginning)
                + "  –  " + HistoryFormat.dateTime(ride.ending));

        // Admin-only extras
        if (ride.price != null) {
            holder.priceText.setVisibility(View.VISIBLE);
            holder.priceText.setText(Math.round(ride.price) + " RSD");
        } else {
            holder.priceText.setVisibility(View.GONE);
        }

        StringBuilder badges = new StringBuilder();
        if (ride.status != null) {
            badges.append(ride.status);
        }
        if (ride.hasPanic()) {
            if (badges.length() > 0) badges.append("  •  ");
            badges.append(holder.itemView.getContext().getString(R.string.hor_panic_flag));
        }
        if (badges.length() > 0) {
            holder.statusText.setVisibility(View.VISIBLE);
            holder.statusText.setText(badges.toString());
        } else {
            holder.statusText.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRideClick(ride);
            }
        });
    }

    static class RideViewHolder extends RecyclerView.ViewHolder {
        final TextView routeText;
        final TextView createdText;
        final TextView timesText;
        final TextView priceText;
        final TextView statusText;

        RideViewHolder(@NonNull View itemView) {
            super(itemView);
            routeText = itemView.findViewById(R.id.routeTextView);
            createdText = itemView.findViewById(R.id.createdTextView);
            timesText = itemView.findViewById(R.id.timesTextView);
            priceText = itemView.findViewById(R.id.priceTextView);
            statusText = itemView.findViewById(R.id.statusTextView);
        }
    }
}
