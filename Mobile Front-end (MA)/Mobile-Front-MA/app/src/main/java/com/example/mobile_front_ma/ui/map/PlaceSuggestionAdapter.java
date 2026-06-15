package com.example.mobile_front_ma.ui.map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.mobile_front_ma.models.Place;

import java.util.ArrayList;
import java.util.List;

/**
 * Feeds Novi Sad geocoding suggestions into an AutoCompleteTextView dropdown. Results
 * already come filtered from the server, so the widget's built-in filter is a pass-through
 * that simply re-publishes whatever {@link #setData(List)} last supplied.
 */
public class PlaceSuggestionAdapter extends BaseAdapter implements Filterable {

    private final LayoutInflater inflater;
    private List<Place> items = new ArrayList<>();

    private final Filter passThrough = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            results.values = items;
            results.count = items.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            notifyDataSetChanged();
        }
    };

    public PlaceSuggestionAdapter(@NonNull Context context) {
        this.inflater = LayoutInflater.from(context);
    }

    public void setData(List<Place> places) {
        this.items = places != null ? places : new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Place getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        TextView view = (TextView) convertView;
        if (view == null) {
            view = (TextView) inflater.inflate(
                    android.R.layout.simple_dropdown_item_1line, parent, false);
        }
        view.setText(getItem(position).getLabel());
        return view;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return passThrough;
    }
}
