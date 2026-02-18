package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.dto.*;
import ZgazeniSendvic.Server_Back_ISS.model.*;
//import ZgazeniSendvic.Server_Back_ISS.model.EmailDetails; WRONG IMPORT
import ZgazeniSendvic.Server_Back_ISS.repository.AccountRepository;

import ZgazeniSendvic.Server_Back_ISS.repository.ProfileChangeRequestRepository;
import ZgazeniSendvic.Server_Back_ISS.security.CustomUserDetails;
import ZgazeniSendvic.Server_Back_ISS.security.EmailDetails;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.context.annotation.Primary;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    @Autowired
    ProfileChangeRequestRepository changeRequestRepository;


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

        Account account = new Account(requestDTO);
        insert(account);
        sendConfirmationLink(account.getEmail(),account);

        return new LoginRequestedDTO("1", 1, new AccountLoginDTO(account));

    }

    public void sendConfirmationLink(String email, Account account){
        String rawToken = resetTokenService.createConfirmationToken(account);

        EmailDetails emailDetails = new EmailDetails();
        emailDetails.setRecipient(email); // sender is automatically set
        emailDetails.setSubject("Confirm your DriveBy account");
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
        return allAccounts.save(account);
    }

    public Account updateAccount(UpdateAccountDTO dto) {

        // fetch the logged-in user from JWT
        Account account = getCurrentAccount();

        if (account instanceof Driver) {

            ProfileChangeRequest request = new ProfileChangeRequest();
            request.setAccountId(account.getId());
            request.setNewName(dto.getName());
            request.setNewLastName(dto.getLastName());
            request.setNewAddress(dto.getAddress());
            request.setNewPhoneNumber(dto.getPhoneNumber());
            request.setNewImgString(dto.getImgString());
            request.setStatus(RequestStatus.PENDING);

            ProfileChangeRequest savedRequest = changeRequestRepository.save(request);

            String approveLink = "http://localhost:4200/approve?id=" + savedRequest.getId();

            String message =
                    "Driver requested profile changes.\n\n" +
                            "Driver: " + account.getEmail() + "\n" +
                            "Name: " + dto.getName() + "\n" +
                            "Last Name: " + dto.getLastName() + "\n" +
                            "Address: " + dto.getAddress() + "\n" +
                            "Phone: " + dto.getPhoneNumber() + "\n\n" +
                            "Approve changes here:\n" +
                            approveLink;

            EmailDetails details = new EmailDetails();
            details.setRecipient("admin@email.com"); // better than hardcoded personal mail
            details.setSubject("Driver requested changes");
            details.setMsgBody(message);

            emailService.sendSimpleMail(details);

            return account; // return unchanged account

        } else {
            // Immediate update for USER and ADMIN
            account.setName(dto.getName());
            account.setLastName(dto.getLastName());
            account.setAddress(dto.getAddress());
            account.setPhoneNumber(dto.getPhoneNumber());
            account.setImgString(dto.getImgString());

            return allAccounts.save(account);
        }
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

    public Account getCurrentAccount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authenticated user");
        }

        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        return findAccount(userDetails.getId()); // uses existing findAccount(Long id)
    }

    public Account findByEmail(String email) {
        Optional<Account> account = allAccounts.findByEmail(email);
        if(account.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account with given email was not found");
        }
        return account.get();
    }

    public List<Account> resolveAccountsByEmails(List<String> emails, Account creator) {
        if (emails == null || emails.isEmpty()) return List.of();

        List<String> normalized = emails.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(String::toLowerCase)
                .distinct()
                .filter(e -> creator == null || !e.equalsIgnoreCase(creator.getEmail()))
                .toList();

        if (normalized.isEmpty()) return List.of();

        // Fetch all accounts in one DB call
        List<Account> accounts = allAccounts.findByEmailIn(normalized);

        // Strict: fail if any missing
        if (accounts.size() != normalized.size()) {
            Set<String> found = accounts.stream()
                    .map(a -> a.getEmail().toLowerCase())
                    .collect(Collectors.toSet());

            List<String> missing = normalized.stream()
                    .filter(e -> !found.contains(e))
                    .toList();

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Some invited passengers were not found: " + String.join(", ", missing)
            );
        }

        // Optional: require confirmed
        List<Account> notConfirmed = accounts.stream()
                .filter(a -> !a.isConfirmed())
                .toList();

        if (!notConfirmed.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Some invited passengers are not confirmed: " +
                            notConfirmed.stream().map(Account::getEmail).collect(Collectors.joining(", "))
            );
        }

        return accounts;
    }
}

