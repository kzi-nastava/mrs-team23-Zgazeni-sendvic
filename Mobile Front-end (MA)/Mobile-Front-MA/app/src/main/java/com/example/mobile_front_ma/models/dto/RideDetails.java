package com.example.mobile_front_ma.models.dto;

import java.util.List;

/**
 * Detailed view of a single ride.
 *
 * Covers both backend shapes:
 *  - user  (GET /api/HOR/user/detailed/{id}):  driver, rideNotes, rideDriverRatings
 *  - admin (GET /api/HOR/admin/detailed/{id}): the above plus the passenger list
 *
 * {@code passengers} stays null for the user view.
 */
public class RideDetails {

    public AccountDetails driver;
    public List<AccountDetails> passengers;     // admin payload only
    public List<RideNoteDto> rideNotes;
    public List<RideRatingDto> rideDriverRatings;
}
