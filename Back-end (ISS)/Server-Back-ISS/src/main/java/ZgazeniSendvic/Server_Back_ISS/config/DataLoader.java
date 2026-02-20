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

            System.out.println("✓ Database population completed successfully!");
        };
    }

}

