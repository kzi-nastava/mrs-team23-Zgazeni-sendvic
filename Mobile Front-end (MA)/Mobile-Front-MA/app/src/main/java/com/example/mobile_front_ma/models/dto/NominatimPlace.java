package com.example.mobile_front_ma.models.dto;

import com.google.gson.annotations.SerializedName;

/**
 * One search hit from the Nominatim geocoding API (lat/lon arrive as strings).
 */
public class NominatimPlace {

    @SerializedName("display_name")
    public String displayName;

    @SerializedName("name")
    public String name;

    @SerializedName("lat")
    public String lat;

    @SerializedName("lon")
    public String lon;
}
