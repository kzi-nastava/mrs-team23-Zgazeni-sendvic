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
                                      RideDriverRatingRepository rideDriverRatingRepository) {
        return args -> {
            // Check if data already exists to avoid duplication
            if (accountRepository.count() > 0) {
                System.out.println("Database already populated, skipping data load");
                return;
            }

            System.out.println("Starting database population...");

            // ============ CREATE ACCOUNTS ============
            // Account A (User/Passenger)
            Account accountA = new User();
            accountA.setEmail("accounta@test.com");
            accountA.setPassword(passwordEncoder.encode("password123"));
            accountA.setName("John");
            accountA.setLastName("Doe");
            accountA.setAddress("123 Main St, Test City");
            accountA.setPhoneNumber("1234567890");
            accountA.setConfirmed(true);
            accountA = accountRepository.save(accountA);
            System.out.println("Created Account A: " + accountA.getEmail());

            // Account B (User/Passenger)
            Account accountB = new User();
            accountB.setEmail("accountb@test.com");
            accountB.setPassword(passwordEncoder.encode("password123"));
            accountB.setName("Jane");
            accountB.setLastName("Smith");
            accountB.setAddress("456 Oak Ave, Test City");
            accountB.setPhoneNumber("0987654321");
            accountB.setConfirmed(true);
            accountB = accountRepository.save(accountB);
            System.out.println("Created Account B: " + accountB.getEmail());

            // Admin A (Admin)
            Admin adminA = new Admin();
            adminA.setEmail("admina@test.com");
            adminA.setPassword(passwordEncoder.encode("password123"));
            adminA.setName("Alice");
            adminA.setLastName("Admin");
            adminA.setAddress("789 Admin Rd, Test City");
            adminA.setPhoneNumber("5555555555");
            adminA.setConfirmed(true);
            adminA = accountRepository.save(adminA);
            System.out.println("Created Admin A: " + adminA.getEmail());

            // ============ CREATE VEHICLES ============
            // Vehicle for Driver 1
            Vehicle vehicle1 = new Vehicle(
                    "Toyota Camry",
                    "ABC-123",
                    VehicleType.STANDARD,
                    4,
                    true,
                    true
            );
            vehicle1 = vehicleRepository.save(vehicle1);
            System.out.println("Created Vehicle 1: " + vehicle1.getRegistration());

            // Vehicle for Driver 2
            Vehicle vehicle2 = new Vehicle(
                    "Mercedes S-Class",
                    "LUX-456",
                    VehicleType.LUXURY,
                    4,
                    false,
                    false
            );
            vehicle2 = vehicleRepository.save(vehicle2);
            System.out.println("Created Vehicle 2: " + vehicle2.getRegistration());

            // ============ CREATE DRIVERS ============
            // Driver 1
            Driver driver1 = new Driver(vehicle1);
            driver1.setEmail("driver1@test.com");
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
            System.out.println("Created Driver 1: " + driver1.getEmail());

            // Driver 2
            Driver driver2 = new Driver(vehicle2);
            driver2.setEmail("driver2@test.com");
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
            System.out.println("Created Driver 2: " + driver2.getEmail());

            // ============ CREATE RIDES ============
            // Ride 1: Accounts A & B with Driver 1
            Ride ride1 = new Ride();
            ride1.setDriver(driver1);
            ride1.setCreator(accountA);

            List<Account> ride1Passengers = new ArrayList<>();
            ride1Passengers.add(accountA);
            ride1Passengers.add(accountB);
            ride1.setPassengers(ride1Passengers);

            List<Location> ride1Locations = new ArrayList<>();
            ride1Locations.add(new Location(20.4489, 44.8176)); // Belgrade, Serbia
            ride1Locations.add(new Location(20.3971, 44.9199)); // Another location
            ride1.setLocations(ride1Locations);

            ride1.setTotalPrice(25.50);
            ride1.setStartTime(LocalDateTime.now().plusHours(1));
            ride1.setEndTime(LocalDateTime.now().plusHours(2));
            ride1.setStatus(RideStatus.SCHEDULED);
            ride1.setPanic(false);
            //ride1.setCreationDate(LocalDateTime.now().minusHours(5));
            ride1 = rideRepository.save(ride1);
            System.out.println("Created Ride 1 (Accounts A & B, Driver 1)");

            // Ride 2: Account A with Driver 1
            Ride ride2 = new Ride();
            ride2.setDriver(driver1);
            ride2.setCreator(accountA);

            List<Account> ride2Passengers = new ArrayList<>();
            ride2Passengers.add(accountA);
            ride2.setPassengers(ride2Passengers);

            List<Location> ride2Locations = new ArrayList<>();
            ride2Locations.add(new Location(20.5000, 44.8500)); // Different location
            ride2Locations.add(new Location(20.4500, 44.9000)); // Another location
            ride2.setLocations(ride2Locations);

            ride2.setTotalPrice(15.75);
            ride2.setStartTime(LocalDateTime.now().plusHours(3));
            ride2.setEndTime(LocalDateTime.now().plusHours(4));
            ride2.setStatus(RideStatus.SCHEDULED);
            ride2.setPanic(false);
            //ride2.setCreationDate(LocalDateTime.now().minusHours(20));
            ride2 = rideRepository.save(ride2);
            System.out.println("Created Ride 2 (Account A, Driver 1)");

            // Driver 2 has no rides (already created above)
            System.out.println("Driver 2 has no assigned rides (as intended)");

            // ============ CREATE VEHICLE POSITIONS ============
            VehiclePosition vehiclePos1 = new VehiclePosition(
                    vehicle1.getId().toString(),
                    44.8176,
                    20.4489,
                    "ACTIVE"
            );
            vehiclePos1 = vehiclePositionsRepository.save(vehiclePos1);
            System.out.println("Created Vehicle Position 1 (Driver 1's vehicle)");

            VehiclePosition vehiclePos2 = new VehiclePosition(
                    vehicle1.getId().toString(),
                    44.9199,
                    20.3971,
                    "ACTIVE"
            );
            vehiclePos2 = vehiclePositionsRepository.save(vehiclePos2);
            System.out.println("Created Vehicle Position 2 (Driver 1's vehicle)");

            VehiclePosition vehiclePos3 = new VehiclePosition(
                    vehicle2.getId().toString(),
                    44.8797,
                    20.3995,
                    "ACTIVE"
            );
            vehiclePos3 = vehiclePositionsRepository.save(vehiclePos3);
            System.out.println("Created Vehicle Position 3 (Driver 2's vehicle)");

            VehiclePosition vehiclePos4 = new VehiclePosition(
                    vehicle2.getId().toString(),
                    44.8152,
                    20.4090,
                    "IDLE"
            );
            vehiclePos4 = vehiclePositionsRepository.save(vehiclePos4);
            System.out.println("Created Vehicle Position 4 (Driver 2's vehicle)");

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