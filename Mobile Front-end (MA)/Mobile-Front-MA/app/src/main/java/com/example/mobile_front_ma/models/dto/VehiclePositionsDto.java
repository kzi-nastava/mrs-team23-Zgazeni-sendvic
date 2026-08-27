package com.example.mobile_front_ma.models.dto;

import java.util.List;

public class VehiclePositionsDto {
    private List<VehiclePositionDto> vehiclePositions;

    public VehiclePositionsDto() {}

    public List<VehiclePositionDto> getVehiclePositions() {
        return vehiclePositions;
    }

    public void setVehiclePositions(List<VehiclePositionDto> vehiclePositions) {
        this.vehiclePositions = vehiclePositions;
    }
}
