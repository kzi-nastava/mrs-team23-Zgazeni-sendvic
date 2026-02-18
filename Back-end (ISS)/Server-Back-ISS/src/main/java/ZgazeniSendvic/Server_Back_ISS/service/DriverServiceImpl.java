package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.dto.ActivateDriverDTO;
import ZgazeniSendvic.Server_Back_ISS.dto.CreateDriverDTO;
import ZgazeniSendvic.Server_Back_ISS.dto.RegisterVehicleDTO;
import ZgazeniSendvic.Server_Back_ISS.model.Account;
import ZgazeniSendvic.Server_Back_ISS.model.Driver;
//import ZgazeniSendvic.Server_Back_ISS.model.Role;
import ZgazeniSendvic.Server_Back_ISS.model.Vehicle;
import ZgazeniSendvic.Server_Back_ISS.repository.AccountRepository;
import ZgazeniSendvic.Server_Back_ISS.repository.VehicleRepository;

import ZgazeniSendvic.Server_Back_ISS.security.EmailDetails;
import ZgazeniSendvic.Server_Back_ISS.security.CustomUserDetails;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class DriverServiceImpl implements IDriverService {

    @Autowired
    AccountRepository accountRepository;
    @Autowired
    VehicleRepository vehicleRepository;
    @Autowired
    EmailService emailService;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    PasswordResetTokenRepositoryService passwordResetTokenRepositoryService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

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
        driver.setActive(false);

        String token = UUID.randomUUID().toString();
        driver.setActivationToken(token);

        Driver saved = accountRepository.save(driver);

        String activationLink =
                frontendUrl + "/activate-driver/" + token;

        EmailDetails email = new EmailDetails();
        email.setRecipient(saved.getEmail());
        email.setSubject("Activate your driver account");
        email.setMsgBody(
                "Hello,\n\n" +
                        "An administrator has created a driver account for you.\n\n" +
                        "Please activate your account using the link below (valid for 24 hours):\n\n" +
                        activationLink + "\n\n" +
                        "If you did not expect this email, please ignore it."
        );

        emailService.sendSimpleMail(email);

        return saved;
    }

    @Override
    @Transactional
    public void activateDriver(String token, String rawPassword) {

        Account account = passwordResetTokenRepositoryService
                .validateResetToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired token"));

        if (!(account instanceof Driver driver)) {
            throw new IllegalStateException("Account is not a driver");
        }

        if (driver.isActive()) {
            throw new IllegalStateException("Driver already activated");
        }

        driver.setPassword(passwordEncoder.encode(rawPassword));
        driver.setActive(true);

        passwordResetTokenRepositoryService.markAsUsed(token);

        accountRepository.save(driver);
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
            if(found.get().getRole().equals("DRIVER")){ // first fix
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
                if(found.get().getRole().equals("DRIVER")){ //second fix
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

    public void ThrowIfNotAllowedToLogOut(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            return;
        }

        Object principal = auth.getPrincipal();
        if (!(principal instanceof CustomUserDetails userDetails)) {
            return;
        }

        Account account = userDetails.getAccount();
        if (!(account instanceof Driver driver)) {
            return;
        }

        if (driver.getDriving()) {
            throw new IllegalStateException("Driver is currently driving. Marked as awaiting deactivation");
        }

        //Driver logged out
        driver.setAvailable(false);
        driver.setAwaitingDeactivation(false);
        accountRepository.save(driver);
    }

    public void deactivateDriverIfRequested(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            return;
        }

        Object principal = auth.getPrincipal();
        if (!(principal instanceof CustomUserDetails userDetails)) {
            return;
        }

        Account account = userDetails.getAccount();
        if (!(account instanceof Driver driver)) {
            return;
        }

        if (driver.getDriving()) {
            driver.setAwaitingDeactivation(true);
            accountRepository.save(driver);
            return;
        }

        driver.setAvailable(false);
        driver.setAwaitingDeactivation(false);
        accountRepository.save(driver);
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
