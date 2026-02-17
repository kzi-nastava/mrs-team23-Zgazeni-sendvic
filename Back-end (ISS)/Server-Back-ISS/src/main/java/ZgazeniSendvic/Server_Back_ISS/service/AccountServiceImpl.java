package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.dto.*;
import ZgazeniSendvic.Server_Back_ISS.model.Account;
import ZgazeniSendvic.Server_Back_ISS.model.Driver;
import ZgazeniSendvic.Server_Back_ISS.model.AccountConfirmationToken;
//import ZgazeniSendvic.Server_Back_ISS.model.EmailDetails; WRONG IMPORT
import ZgazeniSendvic.Server_Back_ISS.model.User;
import ZgazeniSendvic.Server_Back_ISS.repository.AccountRepository;

import ZgazeniSendvic.Server_Back_ISS.security.CustomUserDetails;
import ZgazeniSendvic.Server_Back_ISS.security.EmailDetails;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.context.annotation.Primary;

import java.util.*;
import java.util.regex.Pattern;

@Primary
@Service
public class AccountServiceImpl implements IAccountService, UserDetailsService {

    @Autowired
    AccountRepository allAccounts;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    EmailService emailService;

    @Autowired
    PasswordResetTokenRepositoryService resetTokenService;


    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\+?[0-9]{8,15}$");

    @Override
    public Collection<Account> getAll() {
        System.out.println(allAccounts.findAll());
        return allAccounts.findAll();
    }

    @Override
    public Account findAccount(Long accountId) {
        return allAccounts.findById(accountId).orElse(null);
    }

    public Account findAccountByEmail(String email) {
        return allAccounts.findByEmail(email).get();
    }

    @Override
    public Account insert(Account account) {
        try {
            //password encoding
            account.setPassword(passwordEncoder.encode(account.getPassword()));
            allAccounts.save(account);
            allAccounts.flush();
            return account;
        }
        catch(ConstraintViolationException ex){
            Set<ConstraintViolation<?>> errors = ex.getConstraintViolations();

        StringBuilder sb = new StringBuilder(1000);
        for(ConstraintViolation<?> error: errors){
            sb.append(error.getMessage() + "\n");
        }
        throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, sb.toString());
        }
    }

    public LoginRequestedDTO registerAccount(RegisterRequestDTO requestDTO){

        if(allAccounts.existsByEmail(requestDTO.getEmail())){
            throw new IllegalStateException("Email already in use");
        }

        //should be changed to user
        Account account = new User();
        account.setEmail(requestDTO.getEmail());
        account.setPassword(requestDTO.getPassword());
        account.setName(requestDTO.getFirstName());
        account.setLastName(requestDTO.getLastName());
        account.setPhoneNumber(requestDTO.getPhoneNum());
        account.setAddress(requestDTO.getAddress());


        insert(account);
        sendConfirmationLink(account.getEmail(),account);

        return new LoginRequestedDTO("1", 1, new AccountLoginDTO(account));

    }

    public void sendConfirmationLink(String email, Account account){
        String rawToken = resetTokenService.createConfirmationToken(account);

        EmailDetails emailDetails = new EmailDetails();
        emailDetails.setRecipient(email); // sender is automatically set
        emailDetails.setSubject("Confirm your DriveBy account");
        emailDetails.setMsgBody("http://localhost:4200/api/auth/confirm-account?token=" + rawToken);
        emailService.sendSimpleMail(emailDetails);

    }

    public LoginRequestedDTO login(LoginRequestDTO requestDTO){
        //will Have to be reworked probably

        Optional<Account> account = allAccounts.findByEmail(requestDTO.getEmail());
        if(account.isEmpty() || !passwordEncoder.matches(requestDTO.getPassword(), account.get().getPassword())){

            throw new BadCredentialsException("Invalid email or password");


        }

        //otherwise it has been found
        Account found = account.get();

        return new LoginRequestedDTO("1", 1, new AccountLoginDTO(found));

    }

    @Override
    public Account update(Account account) {
        return allAccounts.save(account);
    }

    public Account updateAccount(Long id, UpdateAccountDTO dto) {

        Account account = findAccount(id);
        if (account == null) {
            throw new IllegalArgumentException("Account not found");
        }

        if (dto.getEmail() != null) {
            if (!EMAIL_PATTERN.matcher(dto.getEmail()).matches()) {
                throw new IllegalArgumentException("Invalid email format");
            }
            account.setEmail(dto.getEmail());
        }

        if (dto.getPhoneNumber() != null) {
            if (!PHONE_PATTERN.matcher(dto.getPhoneNumber()).matches()) {
                throw new IllegalArgumentException("Invalid phone number format");
            }
            account.setPhoneNumber(dto.getPhoneNumber());
        }

        if (dto.getPassword() != null) {
            account.setPassword(dto.getPassword()); // hash later
        }

        account.setName(dto.getName());
        account.setLastName(dto.getLastName());
        account.setAddress(dto.getAddress());
        account.setImgString(dto.getImgString());

        return allAccounts.save(account);
    }

    @Override
    public Account delete(Long accountId) {
        return null;
    }

    @Override
    public void deleteAll() {

    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        Account acc = allAccounts.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if(!acc.isConfirmed()){
            throw new UsernameNotFoundException("Account not confirmed:");
        }

        return new CustomUserDetails(acc);
    }

    public void findAccountById(Long userId) {
        Optional<Account> account = allAccounts.findById(userId);
        if(account.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account with given ID was not found");
        }
    }

    public void forgotPassword(String email){
        Optional<Account> account =  allAccounts.findByEmail(email);
        if(account.isEmpty()){
            return; // email doesn't match, do not send an email
        }

        //generate token
        String rawToken = resetTokenService.createResetToken(account.get());
        // Matches; send the email
        EmailDetails emailDetails = new EmailDetails();
        emailDetails.setRecipient(email); // sender is automatically set
        emailDetails.setSubject("Reset your DriveBy password");
        emailDetails.setMsgBody("http://localhost:4200/reset-password?token=" + rawToken);
        emailService.sendSimpleMail(emailDetails);

    }

    public void resetPassword(PasswordResetConfirmedRequestDTO resetRequestDTO){

        String rawToken = resetRequestDTO.getToken();
        String newPass = resetRequestDTO.getNewPassword();

        if(!resetTokenService.isReset(rawToken)){
            throw new BadCredentialsException("Is not a reset token");
        }

        Optional<Account> foundAccount = resetTokenService.validateResetToken(rawToken);
        if(foundAccount.isEmpty()){
            throw new BadCredentialsException("Invalid reset token");
            //This doesnt work because I have a global handler that "misuses" it
        }
        //token is proper
        Account account = foundAccount.get();
        account.setPassword(passwordEncoder.encode(newPass));
        allAccounts.save(account);
        allAccounts.flush();

        resetTokenService.markAsUsed(rawToken);


    }

    public void confirmAccount(AccountConfirmationDTO confirmationDTO){

        String rawToken = confirmationDTO.getRawToken();
        if(resetTokenService.isReset(rawToken)){
            throw new BadCredentialsException("Is not a confirmation token");
        }

        Optional<Account> foundAccount = resetTokenService.validateResetToken(rawToken);
        if(foundAccount.isEmpty()){
            throw new BadCredentialsException("Invalid confirmation token");

        }
        //token is proper
        Account account = foundAccount.get();
        account.setConfirmed(true);
        allAccounts.save(account);
        allAccounts.flush();

        resetTokenService.markAsUsed(rawToken);

    }
    }

