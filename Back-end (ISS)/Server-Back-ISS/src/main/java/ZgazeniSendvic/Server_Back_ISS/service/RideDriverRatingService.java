package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.dto.RideDriverRatingDTO;

public class RideDriverRatingService {
    public static boolean saveRating(RideDriverRatingDTO rideDriverRatingDTO) {
        System.out.println("Saving rating for userId: " + rideDriverRatingDTO.getUserId());
        System.out.println("Ride ID: " + rideDriverRatingDTO.getRideId());
        System.out.println("Driver Rating: " + rideDriverRatingDTO.getDriverRating());
        System.out.println("Vehicle Rating: " + rideDriverRatingDTO.getVehicleRating());
        System.out.println("Comment: " + rideDriverRatingDTO.getComment());
        return true;
    }
}
