package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.dto.CreateDriverDTO;
import ZgazeniSendvic.Server_Back_ISS.dto.RegisterVehicleDTO;
import ZgazeniSendvic.Server_Back_ISS.model.Account;
import ZgazeniSendvic.Server_Back_ISS.model.Driver;
import ZgazeniSendvic.Server_Back_ISS.model.Role;
import ZgazeniSendvic.Server_Back_ISS.model.Vehicle;
import ZgazeniSendvic.Server_Back_ISS.repository.AccountRepository;
import ZgazeniSendvic.Server_Back_ISS.repository.VehicleRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class DriverServiceImpl implements IDriverService {

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    VehicleRepository vehicleRepository;

    private static final Pattern REGISTRATION_PATTERN =
            Pattern.compile("^[A-Z0-9-]{5,15}$");

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\+?[0-9]{8,15}$");

    @Override
    public Driver registerDriver(CreateDriverDTO dto) {

        if (dto.getEmail() == null || dto.getPassword() == null ||
                dto.getName() == null || dto.getLastName() == null ||
                dto.getPhoneNumber() == null) {

            throw new IllegalArgumentException("Required fields must not be null");
        }

        if (!EMAIL_PATTERN.matcher(dto.getEmail()).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }

        if (!PHONE_PATTERN.matcher(dto.getPhoneNumber()).matches()) {
            throw new IllegalArgumentException("Invalid phone number format");
        }

        if (accountRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalStateException("Account with this email already exists");
        }

        Driver driver = new Driver();
        driver.setEmail(dto.getEmail());
        driver.setPassword(dto.getPassword()); // hash later
        driver.setName(dto.getName());
        driver.setLastName(dto.getLastName());
        driver.setAddress(dto.getAddress());
        driver.setPhoneNumber(dto.getPhoneNumber());
        driver.setImgString(dto.getImgString());
        driver.setVehicle(dto.getVehicle());

        return (Driver) accountRepository.save(driver);
    }

    @Override
    public Vehicle registerVehicle(RegisterVehicleDTO dto) {

        if (dto.getRegistration() == null ||
                !REGISTRATION_PATTERN.matcher(dto.getRegistration()).matches()) {
            throw new IllegalArgumentException("Invalid vehicle registration");
        }

        if (vehicleRepository.existsByRegistration(dto.getRegistration())) {
            throw new IllegalStateException("Vehicle already registered");
        }

        if (dto.getNumOfSeats() < 1 || dto.getNumOfSeats() > 9) {
            throw new IllegalArgumentException("Invalid number of seats");
        }

        Vehicle vehicle = new Vehicle(
                dto.getModel(),
                dto.getRegistration(),
                dto.getType(),
                dto.getNumOfSeats(),
                dto.isBabiesAllowed(),
                dto.isPetsAllowed()
        );

        Vehicle savedVehicle = vehicleRepository.save(vehicle);

        return savedVehicle;
    }

    /* ---------------- Status state ---------------- */
    public void ActivateIfDriver(String email){

        try{

        Optional<Account> found = accountRepository.findByEmail(email);
        if (found.isPresent()) {
            if(found.get().hasRole(Role.Driver)){
                Driver driver = (Driver) found.get();
                driver.setAvailable(true);
        }
            }
                } catch(Exception e){
                    System.out.println(e.getMessage());
                    System.out.println("Faliure shouldn't ever occur here, failure source : ActivateIfDriver");
        }
    }


    public void changeAvailableStatus(String email, boolean value){


            Optional<Account> found = accountRepository.findByEmail(email);
            if (found.isPresent()) {
                if(found.get().hasRole(Role.Driver)){
                    Driver driver = (Driver) found.get();
                    //if changing to Active, always allow
                    if(value){
                        driver.setAvailable(true);
                        return;
                    } else{
                        //to Unavailable and driving
                        if(driver.getDriving()){
                            driver.setAwaitingDeactivation(true);
                            throw new IllegalStateException("Driver is currently driving. Marked as awaiting deactivation");
                        }
                        //to Unavailable and not driving
                        driver.setAvailable(false);
                        return;
                    }
                }
                throw new IllegalStateException("Account with email " + email + " is not a driver");
            } // not found

        throw new IllegalArgumentException("Account with email " + email + " not found");

    }

    public boolean isAvailableDriver(String email){
        Optional<Account> found = accountRepository.findByEmail(email);
        //only returns true if isPresent and is Driver and is Available
        return (found.isPresent() && found.get().hasRole(Role.Driver) && (((Driver) found.get()).isAvailable()));
    }


    /* ---------------- IAccountService ---------------- */

    @Override
    public Collection<Account> getAll() {
        return accountRepository.findAll();
    }

    @Override
    public Account findAccount(Long accountId) {
        return accountRepository.findById(accountId).orElse(null);
    }

    @Override
    public Account insert(Account account) {
        return accountRepository.save(account);
    }

    @Override
    public Account update(Account account) {
        return accountRepository.save(account);
    }

    @Override
    public Account delete(Long accountId) {
        Account acc = findAccount(accountId);
        if (acc != null) {
            accountRepository.delete(acc);
        }
        return acc;
    }

    @Override
    public void deleteAll() {
        accountRepository.deleteAll();
    }
}
