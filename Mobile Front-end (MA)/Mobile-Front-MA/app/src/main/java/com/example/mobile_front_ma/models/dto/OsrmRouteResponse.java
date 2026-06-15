package com.example.mobile_front_ma.models.dto;

import java.util.List;

/**
 * Response from the OSRM routing API (/route/v1/driving) when requested with
 * geometries=geojson. Coordinates are GeoJSON order: [longitude, latitude].
 */
public class OsrmRouteResponse {

    public String code;
    public List<Route> routes;

    public static class Route {
        public double distance;  // meters
        public double duration;  // seconds
        public Geometry geometry;
    }

    public static class Geometry {
        public List<List<Double>> coordinates;
    }
}
