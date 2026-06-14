package ZgazeniSendvic.Server_Back_ISS.config;

import ZgazeniSendvic.Server_Back_ISS.model.*;
import ZgazeniSendvic.Server_Back_ISS.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Configuration
@Profile("!test")
public class DataLoader {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner loadData(AccountRepository accountRepository,
                                      VehicleRepository vehicleRepository,
                                      RideRepository rideRepository,
                                      VehiclePositionsRepository vehiclePositionsRepository,
                                      RideNoteRepository rideNoteRepository,
                                      RideDriverRatingRepository rideDriverRatingRepository,
                                      RouteRepository routeRepository,
                                      RideRequestRepository rideRequestRepository) {

        return args -> {
            // Check if data already exists to avoid duplication
            if (accountRepository.count() > 0) {
                System.out.println("Database already populated, skipping data load");
                return;
            }

            System.out.println("Starting database population...");

            // ============ CREATE ACCOUNTS ============
            Account accountA = new User();
            accountA.setEmail("marko28082004+user1@gmail.com");
            accountA.setPassword(passwordEncoder.encode("password123"));
            accountA.setName("John");
            accountA.setLastName("Doe");
            accountA.setAddress("123 Main St, Test City");
            accountA.setPhoneNumber("1234567890");
            accountA.setConfirmed(true);
            accountA = accountRepository.save(accountA);

            Account accountB = new User();
            accountB.setEmail("marko28082004+user2@gmail.com");
            accountB.setPassword(passwordEncoder.encode("password123"));
            accountB.setName("Jane");
            accountB.setLastName("Smith");
            accountB.setAddress("456 Oak Ave, Test City");
            accountB.setPhoneNumber("0987654321");
            accountB.setConfirmed(true);
            accountB = accountRepository.save(accountB);

            Admin adminA = new Admin();
            adminA.setEmail("marko28082004@gmail.com");
            adminA.setPassword(passwordEncoder.encode("password123"));
            adminA.setName("Alice");
            adminA.setLastName("Admin");
            adminA.setAddress("789 Admin Rd, Test City");
            adminA.setPhoneNumber("5555555555");
            adminA.setConfirmed(true);
            adminA = accountRepository.save(adminA);

            System.out.println("Created accounts: " + accountA.getEmail() + ", " + accountB.getEmail() + ", " + adminA.getEmail());

            // ============ CREATE VEHICLES ============
            Vehicle vehicle1 = new Vehicle(
                    "Toyota Camry",
                    "ABC-123",
                    VehicleType.STANDARD,
                    4,
                    true,
                    true
            );
            vehicle1 = vehicleRepository.save(vehicle1);

            Vehicle vehicle2 = new Vehicle(
                    "Mercedes S-Class",
                    "LUX-456",
                    VehicleType.LUXURY,
                    4,
                    false,
                    false
            );
            vehicle2 = vehicleRepository.save(vehicle2);

            // ============ CREATE DRIVERS ============
            Driver driver1 = new Driver(vehicle1);
            driver1.setEmail("marko28082004+driver1@gmail.com");
            driver1.setPassword(passwordEncoder.encode("password123"));
            driver1.setName("Michael");
            driver1.setLastName("Johnson");
            driver1.setAddress("789 Pine Rd, Test City");
            driver1.setPhoneNumber("5551234567");
            driver1.setConfirmed(true);
            driver1.setAvailable(true);
            driver1.setActive(true);
            driver1.setBusy(false);
            driver1.setWorkedMinutesLast24h(0);
            driver1 = accountRepository.save(driver1);

            Driver driver2 = new Driver(vehicle2);
            driver2.setEmail("marko28082004+driver2@gmail.com");
            driver2.setPassword(passwordEncoder.encode("password123"));
            driver2.setName("Robert");
            driver2.setLastName("Williams");
            driver2.setAddress("321 Elm St, Test City");
            driver2.setPhoneNumber("5559876543");
            driver2.setConfirmed(true);
            driver2.setAvailable(true);
            driver2.setActive(true);
            driver2.setBusy(false);
            driver2.setWorkedMinutesLast24h(0);
            driver2 = accountRepository.save(driver2);

            System.out.println("Created drivers: " + driver1.getEmail() + ", " + driver2.getEmail());

            // ============ FAVORITE ROUTES (PAGED ENDPOINT NEEDS THIS) ============
            // accountA favorites: add a few routes so pagination is visible
            Route fav1 = new Route();
            fav1.setOwner(accountA);
            fav1.setStart(new Location(44.7866, 20.4489));
            fav1.setDestination(new Location(44.8176, 20.4569));
            fav1.setMidPoints(List.of(new Location(44.8125, 20.4612)));
            fav1 = routeRepository.save(fav1);

            Route fav2 = new Route();
            fav2.setOwner(accountA);
            fav2.setStart(new Location(44.8050, 20.4600));
            fav2.setDestination(new Location(44.8200, 20.4300));
            fav2.setMidPoints(List.of());
            fav2 = routeRepository.save(fav2);

            Route fav3 = new Route();
            fav3.setOwner(accountA);
            fav3.setStart(new Location(44.7900, 20.4100));
            fav3.setDestination(new Location(44.8350, 20.4750));
            fav3.setMidPoints(List.of(
                    new Location(44.8100, 20.4400),
                    new Location(44.8200, 20.4550)
            ));
            fav3 = routeRepository.save(fav3);

            // accountB favorites (optional, shows multi-user isolation works)
            Route favB1 = new Route();
            favB1.setOwner(accountB);
            favB1.setStart(new Location(44.8000, 20.5000));
            favB1.setDestination(new Location(44.8600, 20.4700));
            favB1.setMidPoints(List.of());
            routeRepository.save(favB1);

            System.out.println("Created favorite routes for accountA + accountB (good for paging).");

            // ============ RIDE REQUEST (OPTIONAL DEMO DATA) ============
            // Useful for showing that creating requests works / for admin views etc.
            RideRequest rr = new RideRequest();
            rr.setCreator(accountA);
            rr.setLocations(new ArrayList<>(List.of(
                    new Location(44.7866, 20.4489),
                    new Location(44.8125, 20.4612),
                    new Location(44.8176, 20.4569)
            )));
            rr.setVehicleType(VehicleType.STANDARD);
            rr.setBabiesAllowed(false);
            rr.setPetsAllowed(false);
            rr.setScheduledTime(null);
            rr.setInvitedPassengers(new ArrayList<>()); // keep empty for demo
            rr.setEstimatedDistanceKm(6.5);
            rr.setEstimatedPrice(1200.0);
            rr.setStatus(RequestStatus.PENDING); // adjust if your enum differs
            rr.setRejectionReason(null);

            rideRequestRepository.save(rr);
            System.out.println("Created a sample RideRequest.");

            // ============ VEHICLE POSITIONS ============
            vehiclePositionsRepository.save(new VehiclePosition(
                    vehicle1.getId().toString(),
                    44.8176,
                    20.4489,
                    "ACTIVE"
            ));
            vehiclePositionsRepository.save(new VehiclePosition(
                    vehicle1.getId().toString(),
                    44.9199,
                    20.3971,
                    "ACTIVE"
            ));
            vehiclePositionsRepository.save(new VehiclePosition(
                    vehicle2.getId().toString(),
                    44.8797,
                    20.3995,
                    "ACTIVE"
            ));
            vehiclePositionsRepository.save(new VehiclePosition(
                    vehicle2.getId().toString(),
                    44.8152,
                    20.4090,
                    "IDLE"
            ));

            // ============ CREATE ADDITIONAL RIDES WITH NOTES AND RATINGS ============

            // Ride 3: Account A with Driver 1 (created 5 days ago, ACTIVE status)
            Ride ride3 = new Ride();
            ride3.setDriver(driver1);
            ride3.setCreator(accountA);

            List<Account> ride3Passengers = new ArrayList<>();
            ride3Passengers.add(accountA);
            ride3.setPassengers(ride3Passengers);

            List<Location> ride3Locations = new ArrayList<>();
            ride3Locations.add(new Location(20.4600, 44.8300)); // Start location
            ride3Locations.add(new Location(20.4700, 44.8400)); // End location
            ride3.setLocations(ride3Locations);

            ride3.setTotalPrice(18.50);
            LocalDateTime ride3CreationTime = LocalDateTime.now().minusDays(5);
            ride3.setCreationDate(ride3CreationTime);
            ride3.setStartTime(ride3CreationTime.plusMinutes(15));
            ride3.setEndTime(ride3CreationTime.plusMinutes(35));
            ride3.setStatus(RideStatus.ACTIVE);
            ride3.setPanic(false);
            ride3.setStartLatitude(20.4600);
            ride3.setStartLongitude(44.8300);
            ride3.setEndLatitude(20.4700);
            ride3.setEndLongitude(44.8400);
            ride3.setCurrentLatitude(20.4650);
            ride3.setCurrentLongitude(44.8350);
            ride3 = rideRepository.save(ride3);
            System.out.println("Created Ride 3 (Account A, Driver 1, ACTIVE, 5 days ago)");

            // Create RideNote for Ride 3
            RideNote note3 = new RideNote(
                    ride3.getId(),
                    accountA.getId(),
                    "Driver was very professional and punctual.",
                    OffsetDateTime.now().minusDays(5)
            );
            rideNoteRepository.save(note3);
            System.out.println("Created RideNote for Ride 3");

            // Create RideDriverRating for Ride 3
            RideDriverRating rating3 = new RideDriverRating(
                    accountA.getId(),
                    ride3.getId(),
                    9,
                    8,
                    "Excellent ride, clean vehicle, smooth driving."
            );
            rating3.setRecordedAt(OffsetDateTime.now().minusDays(5));
            rideDriverRatingRepository.save(rating3);
            System.out.println("Created RideDriverRating for Ride 3");

            // Ride 4: Accounts A & B with Driver 1 (created 10 days ago, ACTIVE status)
            Ride ride4 = new Ride();
            ride4.setDriver(driver1);
            ride4.setCreator(accountA);

            List<Account> ride4Passengers = new ArrayList<>();
            ride4Passengers.add(accountA);
            ride4Passengers.add(accountB);
            ride4.setPassengers(ride4Passengers);

            List<Location> ride4Locations = new ArrayList<>();
            ride4Locations.add(new Location(20.4800, 44.8600)); // Start location
            ride4Locations.add(new Location(20.4900, 44.8700)); // End location
            ride4.setLocations(ride4Locations);

            ride4.setTotalPrice(22.00);
            LocalDateTime ride4CreationTime = LocalDateTime.now().minusDays(10);
            ride4.setCreationDate(ride4CreationTime);
            ride4.setStartTime(ride4CreationTime.plusMinutes(10));
            ride4.setEndTime(ride4CreationTime.plusMinutes(30));
            ride4.setStatus(RideStatus.ACTIVE);
            ride4.setPanic(false);
            ride4.setStartLatitude(20.4800);
            ride4.setStartLongitude(44.8600);
            ride4.setEndLatitude(20.4900);
            ride4.setEndLongitude(44.8700);
            ride4.setCurrentLatitude(20.4850);
            ride4.setCurrentLongitude(44.8650);
            ride4 = rideRepository.save(ride4);
            System.out.println("Created Ride 4 (Accounts A & B, Driver 1, ACTIVE, 10 days ago)");

            // Create RideNote for Ride 4 (from Account A)
            RideNote note4a = new RideNote(
                    ride4.getId(),
                    accountA.getId(),
                    "Great shared ride with a friend.",
                    OffsetDateTime.now().minusDays(10)
            );
            rideNoteRepository.save(note4a);
            System.out.println("Created RideNote for Ride 4 (Account A)");

            // Create RideNote for Ride 4 (from Account B)
            RideNote note4b = new RideNote(
                    ride4.getId(),
                    accountB.getId(),
                    "Driver was friendly and the ride was comfortable.",
                    OffsetDateTime.now().minusDays(10)
            );
            rideNoteRepository.save(note4b);
            System.out.println("Created RideNote for Ride 4 (Account B)");

            // Create RideDriverRating for Ride 4 (from Account A)
            RideDriverRating rating4a = new RideDriverRating(
                    accountA.getId(),
                    ride4.getId(),
                    10,
                    9,
                    "Perfect ride, highly recommend this driver!"
            );
            rating4a.setRecordedAt(OffsetDateTime.now().minusDays(10));
            rideDriverRatingRepository.save(rating4a);
            System.out.println("Created RideDriverRating for Ride 4 (Account A)");

            // Create RideDriverRating for Ride 4 (from Account B)
            RideDriverRating rating4b = new RideDriverRating(
                    accountB.getId(),
                    ride4.getId(),
                    8,
                    8,
                    "Good experience overall."
            );
            rating4b.setRecordedAt(OffsetDateTime.now().minusDays(10));
            rideDriverRatingRepository.save(rating4b);
            System.out.println("Created RideDriverRating for Ride 4 (Account B)");

            System.out.println("✓ Database population completed successfully!");
        };
    }
}
