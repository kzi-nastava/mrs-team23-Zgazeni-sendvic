package com.example.mobile_front_ma.models.dto;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * A single map point as returned by the backend
 * ({@code Location}: latitude/longitude).
 */
public class LocationDto implements Parcelable {

    public Double latitude;
    public Double longitude;

    public LocationDto() {
    }

    public LocationDto(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    protected LocationDto(Parcel in) {

        if (in.readByte() == 0) {
            latitude = null;
        } else {
            latitude = in.readDouble();
        }

        if (in.readByte() == 0) {
            longitude = null;
        } else {
            longitude = in.readDouble();
        }
    }

    public static final Creator<LocationDto> CREATOR =
            new Creator<LocationDto>() {

                @Override
                public LocationDto createFromParcel(Parcel in) {
                    return new LocationDto(in);
                }

                @Override
                public LocationDto[] newArray(int size) {
                    return new LocationDto[size];
                }
            };

    @Override
    public void writeToParcel(
            Parcel dest,
            int flags
    ) {

        if (latitude == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(latitude);
        }

        if (longitude == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(longitude);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public boolean isValid() {
        return latitude != null && longitude != null;
    }
}