package com.example.mobile_front_ma.data;

import com.example.mobile_front_ma.models.Ride;

import java.util.ArrayList;
import java.util.List;

public class HORRepository {

    public List<Ride> getTestRides() {
        List<Ride> rides = new ArrayList<>();
        rides.add(new Ride("Bulevar Oslobodjenja 68", "Mikole Koscica 2", "1250", "2026-01-20"));
        rides.add(new Ride("Milosa Crnjanskog 6", "Doza Djerdja 43", "800", "2026-01-19"));
        rides.add(new Ride("Branka Bajica 25", "Berislava Berica 16", "2500", "2026-01-18"));
        rides.add(new Ride("Lukijana Musickog 77", "Stevana Branovackog 13", "675", "2026-01-17"));
        return rides;
    }
}
