package com.example.mobile_front_ma.models.dto;

import com.example.mobile_front_ma.models.dto.LocationDto;

import java.util.ArrayList;
import java.util.List;

public class RouteDTO {

    public Long id;
    public LocationDto start;
    public LocationDto destination;
    public List<LocationDto> midPoints;

    public List<LocationDto> getAllPoints() {
        List<LocationDto> points = new ArrayList<>();

        if (start != null && start.isValid()) {
            points.add(start);
        }

        if (midPoints != null) {
            for (LocationDto point : midPoints) {
                if (point != null && point.isValid()) {
                    points.add(point);
                }
            }
        }

        if (destination != null && destination.isValid()) {
            points.add(destination);
        }

        return points;
    }
}