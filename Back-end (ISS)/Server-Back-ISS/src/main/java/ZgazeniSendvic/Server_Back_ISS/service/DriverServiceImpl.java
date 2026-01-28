package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.dto.CreateDriverDTO;
import ZgazeniSendvic.Server_Back_ISS.dto.RegisterVehicleDTO;
import ZgazeniSendvic.Server_Back_ISS.model.Account;
import ZgazeniSendvic.Server_Back_ISS.model.Driver;
import ZgazeniSendvic.Server_Back_ISS.model.Vehicle;
import ZgazeniSendvic.Server_Back_ISS.repository.AccountRepository;
import ZgazeniSendvic.Server_Back_ISS.repository.VehicleRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
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
