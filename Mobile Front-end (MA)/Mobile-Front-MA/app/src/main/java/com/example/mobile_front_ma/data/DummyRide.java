package com.example.mobile_front_ma.data;

/**
 * Dummy "current ride" info for the logged-in mobile user.
 *
 * <p>The real in-progress-ride screen that would supply the actual ride id (and driven route)
 * for actions taken during a ride isn't built yet, so the ride-in-progress controls all fall
 * back to this one shared dummy ride:
 * <ul>
 *     <li>Stop a ride in progress (spec 2.6.5) –
 *         {@link com.example.mobile_front_ma.ui.profile.ProfileCardFragment}</li>
 *     <li>Raise the PANIC alarm (spec 2.6.3) –
 *         {@link com.example.mobile_front_ma.MainActivity}</li>
 * </ul>
 *
 * <p>PANIC only needs the ride id: the backend (POST /api/ride-PANIC/{rideID}) takes no body and
 * authorizes the caller (the driver/passenger of an ACTIVE ride) from the JWT, so {@link #RIDE_ID}
 * is everything the panic button needs to fire successfully.
 */
public final class DummyRide {

    /** Id of the (dummy) ride currently in progress for the logged-in user. */
    public static final long RIDE_ID = 1L;

    private DummyRide() {}
}
