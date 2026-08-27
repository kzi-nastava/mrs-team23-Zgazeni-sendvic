package com.example.mobile_front_ma.models.dto;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class RouteResponse implements Parcelable {

    public Long id;
    public LocationDto start;
    public LocationDto destination;
    public List<LocationDto> midPoints;

    public RouteResponse() {
    }

    protected RouteResponse(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }

        start = in.readParcelable(
                LocationDto.class.getClassLoader()
        );

        destination = in.readParcelable(
                LocationDto.class.getClassLoader()
        );

        midPoints = in.createTypedArrayList(
                LocationDto.CREATOR
        );
    }

    public static final Creator<RouteResponse> CREATOR =
            new Creator<RouteResponse>() {

                @Override
                public RouteResponse createFromParcel(Parcel in) {
                    return new RouteResponse(in);
                }

                @Override
                public RouteResponse[] newArray(int size) {
                    return new RouteResponse[size];
                }
            };

    @Override
    public void writeToParcel(
            Parcel dest,
            int flags
    ) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(id);
        }

        dest.writeParcelable(start, flags);
        dest.writeParcelable(destination, flags);

        dest.writeTypedList(midPoints);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public Long getId() {
        return id;
    }

    public LocationDto getStart() {
        return start;
    }

    public LocationDto getDestination() {
        return destination;
    }

    public List<LocationDto> getMidPoints() {
        return midPoints;
    }
}