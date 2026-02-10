package ZgazeniSendvic.Server_Back_ISS.config;

import ZgazeniSendvic.Server_Back_ISS.model.*;
import ZgazeniSendvic.Server_Back_ISS.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class DataLoader {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner loadData(AccountRepository accountRepository,
                                      VehicleRepository vehicleRepository,
                                      RideRepository rideRepository) {
        return args -> {
            // Check if data already exists to avoid duplication
            if (accountRepository.count() > 0) {
                System.out.println("Database already populated, skipping data load");
                return;
            }

            System.out.println("Starting database population...");

            // ============ CREATE ACCOUNTS ============
            // Account A (User/Passenger)
            Account accountA = new Account();
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
            Account accountB = new Account();
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

            ride1.setPrice(25.50);
            ride1.setStartTime(LocalDateTime.now().plusHours(1));
            ride1.setEndTime(LocalDateTime.now().plusHours(2));
            ride1.setStatus(RideStatus.SCHEDULED);
            ride1.setPanic(false);
            ride1.setCreationDate(LocalDateTime.now().minusHours(5));
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

            ride2.setPrice(15.75);
            ride2.setStartTime(LocalDateTime.now().plusHours(3));
            ride2.setEndTime(LocalDateTime.now().plusHours(4));
            ride2.setStatus(RideStatus.SCHEDULED);
            ride2.setPanic(false);
            ride2.setCreationDate(LocalDateTime.now().minusHours(20));
            ride2 = rideRepository.save(ride2);
            System.out.println("Created Ride 2 (Account A, Driver 1)");

            // Driver 2 has no rides (already created above)
            System.out.println("Driver 2 has no assigned rides (as intended)");

            System.out.println("✓ Database population completed successfully!");
        };
    }
}

