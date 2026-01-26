package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.dto.*;
import ZgazeniSendvic.Server_Back_ISS.model.Account;
import ZgazeniSendvic.Server_Back_ISS.model.AccountConfirmationToken;
import ZgazeniSendvic.Server_Back_ISS.model.EmailDetails;
import ZgazeniSendvic.Server_Back_ISS.repository.AccountRepository;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

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


    @Override
    public Collection<Account> getAll() {
        System.out.println(allAccounts.findAll());
        return allAccounts.findAll();
    }

    @Override
    public Account findAccount(Long studentId) {
        return null;
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

    public LoginRequestedDTO  registerAccount(RegisterRequestDTO requestDTO){

        if(allAccounts.existsByEmail(requestDTO.getEmail())){
            throw new IllegalStateException("Email already in use");
        }

        Account account = new Account(requestDTO);
        insert(account);
        sendConfirmationLink(account.getEmail(),account);

        return new LoginRequestedDTO("1", 1, new AccountLoginDTO(account));

    }

    public void sendConfirmationLink(String email, Account account){
        String rawToken = resetTokenService.createConfirmationToken(account);

        EmailDetails emailDetails = new EmailDetails();
        emailDetails.setRecipient(email); // sender is automatically set
        emailDetails.setSubject("Confirm ypir DriveBy account");
        emailDetails.setMsgBody("http://localhost:8080/api/auth/confirm-account?token=" + rawToken);
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
        return null;
    }

    @Override
    public Account delete(Long accountId) {
        return null;
    }

    @Override
    public void deleteAll() {

    }

    //The get roles here are the exact Roles the account possesses.
    //annotations
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Account> account = allAccounts.findByEmail(email);
        if(account.isPresent()){
            if(!account.get().isConfirmed()){
                throw new UsernameNotFoundException("Account not confirmed:");
            }
            return org.springframework.security.core.userdetails.User
                    .withUsername(email)
                    .password(account.get().getPassword())
                    .roles(account.get().getRolesList().toArray(new String[0]))
                    .build();

        }else{
            throw new UsernameNotFoundException("No account with such email exists: " + email);
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
        emailDetails.setMsgBody("http://localhost:8080/api/auth/reset-password?token=" + rawToken);
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
