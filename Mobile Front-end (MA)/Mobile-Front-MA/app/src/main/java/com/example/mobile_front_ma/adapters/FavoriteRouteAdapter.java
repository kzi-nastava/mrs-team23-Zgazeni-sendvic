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
import com.example.mobile_front_ma.models.dto.LocationDto;
import com.example.mobile_front_ma.models.dto.RouteDTO;
import com.example.mobile_front_ma.models.dto.RouteResponse;
import com.example.mobile_front_ma.util.HistoryFormat;

import java.util.ArrayList;
import java.util.List;

public class FavoriteRouteAdapter
        extends ListAdapter<RouteResponse, FavoriteRouteAdapter.ViewHolder> {

    public interface Listener {
        void onOrder(RouteResponse route);
        void onDelete(RouteResponse route);
    }

    private final Listener listener;

    public FavoriteRouteAdapter(Listener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<RouteResponse> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<RouteResponse>() {

                @Override
                public boolean areItemsTheSame(
                        @NonNull RouteResponse oldItem,
                        @NonNull RouteResponse newItem
                ) {
                    return oldItem.id != null
                            && oldItem.id.equals(newItem.id);
                }

                @Override
                public boolean areContentsTheSame(
                        @NonNull RouteResponse oldItem,
                        @NonNull RouteResponse newItem
                ) {
                    return true;
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_favorite_route,
                        parent,
                        false
                );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        RouteResponse route = getItem(position);

        LocationDto start = route.getStart();
        LocationDto destination = route.getDestination();

        holder.routeText.setText(
                HistoryFormat.point(start)
                        + "  →  "
                        + HistoryFormat.point(destination)
        );

        List<LocationDto> midPoints = route.getMidPoints();

        if (midPoints != null && !midPoints.isEmpty()) {

            StringBuilder via = new StringBuilder("Via: ");

            for (int i = 0; i < midPoints.size(); i++) {

                if (i > 0) {
                    via.append(" → ");
                }

                via.append(
                        HistoryFormat.point(midPoints.get(i))
                );
            }

            holder.midPointsText.setVisibility(View.VISIBLE);
            holder.midPointsText.setText(via.toString());

        } else {
            holder.midPointsText.setVisibility(View.GONE);
        }

        holder.orderButton.setOnClickListener(
                v -> listener.onOrder(route)
        );

        holder.deleteButton.setOnClickListener(
                v -> listener.onDelete(route)
        );
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView routeText;
        final TextView midPointsText;
        final Button orderButton;
        final Button deleteButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            routeText = itemView.findViewById(
                    R.id.routeTextView
            );

            midPointsText = itemView.findViewById(
                    R.id.midPointsTextView
            );

            orderButton = itemView.findViewById(
                    R.id.orderButton
            );

            deleteButton = itemView.findViewById(
                    R.id.deleteButton
            );
        }
    }
}