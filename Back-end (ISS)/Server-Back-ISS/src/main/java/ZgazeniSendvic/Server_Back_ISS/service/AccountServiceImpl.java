package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.dto.*;
import ZgazeniSendvic.Server_Back_ISS.model.Account;
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
import java.util.regex.Pattern;

@Service
public class AccountServiceImpl implements IAccountService, UserDetailsService {

    @Autowired
    AccountRepository allAccounts;

    @Autowired
    PasswordEncoder passwordEncoder;

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

        return new LoginRequestedDTO("1", "1", new AccountLoginDTO(account));

    }

    public LoginRequestedDTO login(LoginRequestDTO requestDTO){
        //will Have to be reworked probably

        Optional<Account> account = allAccounts.findByEmail(requestDTO.getEmail());
        if(account.isEmpty() || !passwordEncoder.matches(requestDTO.getPassword(), account.get().getPassword())){

            throw new BadCredentialsException("Invalid email or password");


        }

        //otherwise it has been found
        Account found = account.get();

        return new LoginRequestedDTO("1", "1", new AccountLoginDTO(found));

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
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Account> account = allAccounts.findByEmail(email);
        if(account.isPresent()){
            return org.springframework.security.core.userdetails.User
                    .withUsername(email)
                    .password(account.get().getPassword())
                    .roles(account.get().getRoles().toString())
                    .build();

        }else{
            throw new UsernameNotFoundException("No account with such email exists: " + email);
        }

    }
}
