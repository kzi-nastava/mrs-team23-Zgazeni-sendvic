package ZgazeniSendvic.Server_Back_ISS.service;


import ZgazeniSendvic.Server_Back_ISS.model.Ride;

import java.util.Collection;

public interface IRideService {
    public Collection<Ride> getAll();

    public Ride findRide(Long rideId);
    public Ride insert(Ride Ride);
    public Ride update(Ride Ride);
    public Ride delete(Long rideId);
    public void deleteAll();
}

