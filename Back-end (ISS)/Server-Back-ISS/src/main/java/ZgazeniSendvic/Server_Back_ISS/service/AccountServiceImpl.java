package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.dto.AccountLoginDTO;
import ZgazeniSendvic.Server_Back_ISS.dto.LoginRequestDTO;
import ZgazeniSendvic.Server_Back_ISS.dto.LoginRequestedDTO;
import ZgazeniSendvic.Server_Back_ISS.dto.RegisterRequestDTO;
import ZgazeniSendvic.Server_Back_ISS.model.Account;
import ZgazeniSendvic.Server_Back_ISS.repository.AccountRepository;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class AccountServiceImpl implements IAccountService {

    @Autowired
    AccountRepository allAccounts;

    @Override
    public Collection<Account> getAll() {
        System.out.println(allAccounts.findAll());
        return allAccounts.findAll();
    }

    @Override
    public Account findAccount(Long studentId) {
        return null;
    }

    @Override
    public Account insert(Account account) {
        try {
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
        if(account.isEmpty() || !Objects.equals(requestDTO.getPassword(), account.get().getPassword())){

            throw new BadCredentialsException("Invalid email or password");


        }

        //otherwise equals
        Account found = account.get();

        return new LoginRequestedDTO("1", "1", new AccountLoginDTO(found));

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
}
