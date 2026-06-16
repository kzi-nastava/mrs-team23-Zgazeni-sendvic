package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.dto.ActivateDriverDTO;
import ZgazeniSendvic.Server_Back_ISS.dto.CreateDriverDTO;
import ZgazeniSendvic.Server_Back_ISS.dto.DriverStatusChangedDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    @Transactional
    public Driver registerDriver(CreateDriverDTO dto) {

        if (dto.getEmail() == null ||
                dto.getName() == null ||
                dto.getLastName() == null ||
                dto.getPhoneNumber() == null ||
                dto.getVehicleId() == null) {
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

        Vehicle vehicle = vehicleRepository.findById(dto.getVehicleId())
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));

        Driver driver = new Driver();
        driver.setEmail(dto.getEmail().trim().toLowerCase());

        // Set a temporary password (hashed) so Spring Security never stores raw
        // User still cannot login if inactive (see note below)
        driver.setPassword(passwordEncoder.encode("PENDING_ACTIVATION"));

        driver.setName(dto.getName());
        driver.setLastName(dto.getLastName());
        String addr = dto.getAddress();
        if (addr == null || addr.isBlank()) {
            addr = "N/A";
        }
        driver.setAddress(addr);
        driver.setPhoneNumber(dto.getPhoneNumber());
        driver.setImgString(dto.getImgString());

        driver.setVehicle(vehicle);

        // inactive until activation
        driver.setActive(false);

        // token stored in driver table (simple flow)
        String token = UUID.randomUUID().toString();
        driver.setActivationToken(token);

        Driver saved = accountRepository.save(driver);

        String activationLink = frontendUrl + "/activate-driver?token=" + token;

        EmailDetails email = new EmailDetails();
        email.setRecipient(saved.getEmail());
        email.setSubject("Activate your driver account");
        email.setMsgBody(
                "Hello,\n\n" +
                        "An administrator has created a driver account for you.\n\n" +
                        "Please activate your account using the link below:\n\n" +
                        activationLink + "\n\n"
        );
        emailService.sendSimpleMail(email);

        return saved;
    }

    @Override
    @Transactional
    public void activateDriver(String token, String rawPassword) {

        Driver driver = accountRepository.findDriverByActivationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired token"));

        if (driver.isActive()) throw new IllegalStateException("Driver already activated");

        driver.setPassword(passwordEncoder.encode(rawPassword));
        driver.setConfirmed(true);
        driver.setActive(true);

        driver.setActivationToken(null); // invalidate token
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
                accountRepository.save(driver);
        }
            }
                } catch(Exception e){
                    System.out.println(e.getMessage());
                    System.out.println("Faliure shouldn't ever occur here, failure source : ActivateIfDriver");
        }
    }


    public DriverStatusChangedDTO changeAvailableStatus(boolean value){

        // Identify the driver from the JWT, never from a client-supplied email,
        // so one driver can't flip another driver's status.
        Driver driver = getAuthenticatedDriver();

        // Going active is always allowed.
        if (value) {
            driver.setAvailable(true);
            accountRepository.save(driver);
            return new DriverStatusChangedDTO(true, "You are now active.");
        }

        // Going inactive while mid-ride can't take effect immediately, so we record
        // the intent and let the ride finish. The driver stays available until then,
        // so the returned state is still active. This is a normal outcome, not an error.
        if (driver.getDriving()) {
            driver.setAwaitingDeactivation(true);
            accountRepository.save(driver);
            return new DriverStatusChangedDTO(driver.getAvailable(),
                    "You are currently driving. You will be set inactive once the ride ends.");
        }

        // Going inactive while idle.
        driver.setAvailable(false);
        accountRepository.save(driver);
        return new DriverStatusChangedDTO(false, "You are now inactive.");
    }

    /** The driver behind the current JWT, or an error if the principal isn't a driver. */
    private Driver getAuthenticatedDriver() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth instanceof AnonymousAuthenticationToken
                || !(auth.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new IllegalStateException("No authenticated driver in the security context.");
        }
        Account account = userDetails.getAccount();
        if (!(account instanceof Driver driver)) {
            throw new IllegalStateException("The authenticated account is not a driver.");
        }
        return driver;
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

        if (driver.getAvailable()) {
            throw new IllegalStateException("Driver is currently available, and can't log out.");
        }

        //Driver logged out
        driver.setAvailable(false);
        driver.setAwaitingDeactivation(false);
        accountRepository.save(driver);
    }

    public void deactivateDriverIfRequested(boolean status){
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

        //means he wants to be available
        if(status){
            driver.setAvailable(true);
            driver.setAwaitingDeactivation(false);
            accountRepository.save(driver);
            return;
        }

        if (driver.getDriving()) {
            driver.setAwaitingDeactivation(true);
            accountRepository.save(driver);
            throw new IllegalStateException("Driver is currently driving. Marked as awaiting deactivation");
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
    public Page<Account> getAllPaged(String q, String type, Boolean confirmed, Pageable pageable) {
        return null;
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
