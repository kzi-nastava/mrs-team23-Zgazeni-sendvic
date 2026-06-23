package com.example.mobile_front_ma.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile_front_ma.R;
import com.example.mobile_front_ma.models.dto.PanicResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Rows for the admin panic-notifications list (spec 2.6.3). Each row shows the caller, the
 * ride, when it was raised, and a status; unresolved alerts get a Resolve button.
 */
public class PanicNotificationAdapter
        extends RecyclerView.Adapter<PanicNotificationAdapter.ViewHolder> {

    public interface OnResolveListener {
        void onResolve(PanicResponse panic);
    }

    private final List<PanicResponse> items = new ArrayList<>();
    private final OnResolveListener listener;

    public PanicNotificationAdapter(OnResolveListener listener) {
        this.listener = listener;
    }

    public void submit(List<PanicResponse> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_panic_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView callerText;
        private final TextView statusText;
        private final TextView rideText;
        private final TextView timeText;
        private final Button resolveButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            callerText = itemView.findViewById(R.id.callerText);
            statusText = itemView.findViewById(R.id.statusText);
            rideText = itemView.findViewById(R.id.rideText);
            timeText = itemView.findViewById(R.id.timeText);
            resolveButton = itemView.findViewById(R.id.resolveButton);
        }

        void bind(PanicResponse panic) {
            callerText.setText(panic.callerName == null || panic.callerName.trim().isEmpty()
                    ? itemView.getContext().getString(R.string.panic_caller_unknown)
                    : panic.callerName);
            rideText.setText(itemView.getContext().getString(R.string.panic_ride,
                    panic.rideId == null ? 0 : panic.rideId));
            timeText.setText(formatTime(panic.createdAt));

            if (panic.resolved) {
                statusText.setText(R.string.panic_status_resolved);
                statusText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.greelow));
                resolveButton.setVisibility(View.GONE);
                resolveButton.setOnClickListener(null);
            } else {
                statusText.setText(R.string.panic_status_active);
                statusText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.panicRed));
                resolveButton.setVisibility(View.VISIBLE);
                resolveButton.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onResolve(panic);
                    }
                });
            }
        }

        /** "2026-06-22T10:15:30.123" -> "2026-06-22 10:15". */
        private String formatTime(String iso) {
            if (iso == null || iso.isEmpty()) {
                return "";
            }
            String s = iso.replace('T', ' ');
            int dot = s.indexOf('.');
            if (dot > 0) {
                s = s.substring(0, dot);
            }
            return s.length() >= 16 ? s.substring(0, 16) : s;
        }
    }
}
