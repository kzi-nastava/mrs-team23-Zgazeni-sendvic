package com.example.mobile_front_ma.models;

import com.example.mobile_front_ma.models.Place;

import android.widget.AutoCompleteTextView;

public class OrderStop {

    private final AutoCompleteTextView input;
    private Place place;

    public OrderStop(AutoCompleteTextView input) {
        this.input = input;
    }

    public AutoCompleteTextView getInput() {
        return input;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }
}
